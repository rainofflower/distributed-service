package com.yanghui.distributed.rpc.client;

import com.yanghui.distributed.rpc.bootstrap.ConsumerBootstrap;
import com.yanghui.distributed.rpc.client.router.Router;
import com.yanghui.distributed.rpc.common.RpcConstants;
import com.yanghui.distributed.rpc.common.util.CommonUtils;
import com.yanghui.distributed.rpc.common.util.MethodSignBuilder;
import com.yanghui.distributed.rpc.config.ConsumerConfig;
import com.yanghui.distributed.rpc.config.ConsumerMethodConfig;
import com.yanghui.distributed.rpc.context.RpcInvokeContext;
import com.yanghui.distributed.rpc.core.Request;
import com.yanghui.distributed.rpc.core.Response;
import com.yanghui.distributed.rpc.core.ResponseStatus;
import com.yanghui.distributed.rpc.core.exception.ErrorType;
import com.yanghui.distributed.rpc.core.exception.RouteException;
import com.yanghui.distributed.rpc.core.exception.RpcException;
import com.yanghui.distributed.rpc.future.ClientCallbackExecutor;
import com.yanghui.distributed.rpc.future.InvokeFuture;
import com.yanghui.distributed.rpc.future.Listener;
import com.yanghui.distributed.rpc.invoke.Invoker;
import com.yanghui.distributed.rpc.listener.MethodProviderListener;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.Executor;

import static com.yanghui.distributed.rpc.common.RpcConfigs.getBooleanValue;
import static com.yanghui.distributed.rpc.common.RpcOptions.CONSUMER_CONNECTION_REUSE;

/**
 * 消费者 集群容错、服务路由
 * @author YangHui
 */
public abstract class Cluster implements Invoker, MethodProviderListener {

    /**
     * 服务端消费者启动器
     */
    protected final ConsumerBootstrap consumerBootstrap;

    /**
     * 配置
     */
    protected final ConsumerConfig consumerConfig;


    protected ConsumerConnectionHolder consumerConnectionHolder;

    protected Router router;

    /**
     * 构造函数
     *
     * @param consumerBootstrap 服务端消费者启动器
     */
    public Cluster(ConsumerBootstrap consumerBootstrap) {
        this.consumerBootstrap = consumerBootstrap;
        this.consumerConfig = consumerBootstrap.getConsumerConfig();
    }

    public void init(){
        boolean reuse = getBooleanValue(CONSUMER_CONNECTION_REUSE);
        if(reuse){
            consumerConnectionHolder = new ReuseConsumerConnectionHolder(consumerBootstrap);
        }else{
            consumerConnectionHolder = new AloneConsumerConnectionHolder(consumerBootstrap);
        }
        Map<MethodInfo, List<MethodProviderInfo>> all = consumerBootstrap.subscribe();
        if(CommonUtils.isNotEmpty(all)){
            Set<Map.Entry<MethodInfo, List<MethodProviderInfo>>> entries = all.entrySet();
            for(Map.Entry<MethodInfo, List<MethodProviderInfo>> entry : entries){
                MethodInfo methodInfo = entry.getKey();
                List<MethodProviderInfo> methodProviders = entry.getValue();
                if(CommonUtils.isNotEmpty(methodProviders)){
                    updateMethodProviders(methodInfo, methodProviders);
                }
            }
        }
    }

    /**
     * 初始化请求和上下文
     * 创建 InvokeFuture，设置requestId
     * @param request
     * @return
     * @throws RpcException
     */
    public Response invoke(Request request) throws RpcException{
        String invokeType = consumerConfig.getInvokeType();
        request.setInvokeType(invokeType);
        if(!RpcConstants.INVOKER_TYPE_ONEWAY.equals(invokeType)){
            RpcInvokeContext context = RpcInvokeContext.getContext();
            InvokeFuture invokeFuture = new InvokeFuture(request.getId());
            context.setInvokeFuture(invokeFuture);
            context.setTimeout(consumerConfig.getTimeout());
        }
        return doInvoke(request);
    }

    /**
     * 子类实现各自的逻辑
     * 如路由，失败转移，快速失败等
     * @param request
     * @return response
     * @throws RpcException
     */
    protected abstract Response doInvoke(Request request) throws RpcException;


    /**
     * 往指定提供者发送消息
     * @param methodProviderInfo 提供者信息
     * @param request 请求
     * @return
     * @throws RpcException
     */
    public Response sendMsg(MethodProviderInfo methodProviderInfo, Request request) throws RpcException{
        RpcInvokeContext invokeContext = RpcInvokeContext.getContext();
        long timeout = invokeContext.getTimeout();
        Connection connection = consumerConnectionHolder.getConnection(methodProviderInfo);
        try {
            String invokeType = request.getInvokeType();
            Response response;
            //sync 同步调用
            if (RpcConstants.INVOKER_TYPE_SYNC.equals(invokeType)) {
                response = connection.syncSend(request, timeout);
                short status = response.getStatus().getValue();
                if (status != ResponseStatus.SUCCESS.getValue()) {
                    switch (response.getStatus()) {
                        case TIMEOUT:
                            throw new RpcException(ErrorType.CLIENT_TIMEOUT, "获取结果超时");
                        case ERROR:

                    }
                    throw new RpcException(ErrorType.UNKNOWN, "处理失败");
                } else {
                    return response;
                }
            }
            //future
            else if (RpcConstants.INVOKER_TYPE_FUTURE.equals(invokeType)) {
                connection.asyncSend(request, timeout);
                return buildEmptyResponse();
            }
            //callback
            else if(RpcConstants.INVOKER_TYPE_CALLBACK.equals(invokeType)){
                InvokeFuture invokeFuture = invokeContext.getInvokeFuture();
                Listener responseListener = null;
                Executor executor = null;
                //获取方法级别的listener
                Map<Method, ConsumerMethodConfig> methodConfigs = consumerConfig.getMethodConfigs();
                if(methodConfigs != null){
                    ConsumerMethodConfig consumerMethodConfig = methodConfigs.get(request.getMethod());
                    if(consumerMethodConfig != null){
                        responseListener = consumerMethodConfig.getResponseListener();
                        executor = consumerMethodConfig.getExecutor();
                    }
                }
                //未设置方法级别的listener再获取接口级别的listener
                if(responseListener == null){
                    responseListener = consumerConfig.getResponseListener();
                }
                invokeFuture.addListener(responseListener);
                //用户未设置方法级回调线程池，设置为默认的线程池
                if(executor == null){
                    invokeFuture.setCallbackExecutor(ClientCallbackExecutor.getInstance().getExecutor());
                }else{
                    //使用用户自己配置的回调线程池
                    invokeFuture.setCallbackExecutor(executor);
                }
                connection.asyncSend(request, timeout);
                return buildEmptyResponse();
            }
            //oneway 单向调用
            else if(RpcConstants.INVOKER_TYPE_ONEWAY.equals(invokeType)){
                connection.oneWaySend(request);
                return buildEmptyResponse();
            }
            else{
                throw new RpcException(ErrorType.CLIENT_UNDECLARED_ERROR,"客户端未定义执行类型,invokeType:"+invokeType);
            }
        }catch (RpcException e){
            throw e;
        }catch (Throwable e){
            throw new RpcException(ErrorType.CLIENT_UNDECLARED_ERROR, e);
        }
    }

    public Response buildEmptyResponse(){
        return new Response()
                .setStatus(ResponseStatus.SUCCESS);
    }

    /**
     * 负载均衡
     * @param request 将要发送的请求
     * @return 将要调用的服务提供者
     */
    public MethodProviderInfo select(Request request){
        Method method = request.getMethod();
        String protocol = consumerConfig.getProtocol();
        String interfaceName = consumerConfig.getInterfaceName();
        String methodSign = MethodSignBuilder.buildMethodSign(method);

        /**
         * 由以上参数，进行路由选择
         */
        MethodInfo methodInfo = new MethodInfo().setInterfaceName(interfaceName)
                .setMethodSign(methodSign).setProtocol(protocol)
                .setGroup(RpcConstants.ADDRESS_DEFAULT_GROUP)
                .setVersion(RpcConstants.DEFAULT_VERSION);
        Set<MethodProviderInfo> providerSet = consumerConnectionHolder.currentMethodProviderList(methodInfo);
        if(CommonUtils.isEmpty(providerSet)){
            throw new RouteException("服务接口："+consumerConfig.getInterfaceName()+" 路由失败");
        }
        List<MethodProviderInfo> providerList = new ArrayList<>(providerSet);
        MethodProviderInfo methodProviderInfo = providerList.get(0);
        return methodProviderInfo;
    }


    public void addMethodProvider(MethodProviderInfo methodProviderInfo){
        consumerConnectionHolder.addMethodProvider(methodProviderInfo);
    }

    public void addMethodProviders(List<MethodProviderInfo> methodProviderInfoList){
        consumerConnectionHolder.addMethodProviders(methodProviderInfoList);
    }

    public void removeMethodProvider(MethodProviderInfo methodProviderInfo){
        consumerConnectionHolder.removeMethodProvider(methodProviderInfo);
    }

    public void updateMethodProviders(MethodInfo methodInfo, List<MethodProviderInfo> methodProviderInfoList) {
        consumerConnectionHolder.updateMethodProviders(methodInfo, methodProviderInfoList);
    }

    public ConsumerBootstrap getConsumerBootstrap() {
        return consumerBootstrap;
    }

    public ConsumerConfig getConsumerConfig() {
        return consumerConfig;
    }

    public ConsumerConnectionHolder getConsumerConnectionHolder() {
        return consumerConnectionHolder;
    }

    public void setConsumerConnectionHolder(ConsumerConnectionHolder consumerConnectionHolder) {
        this.consumerConnectionHolder = consumerConnectionHolder;
    }

}
