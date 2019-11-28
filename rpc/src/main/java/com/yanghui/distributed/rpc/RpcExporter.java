package com.yanghui.distributed.rpc;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * 服务发布者
 *
 * 监听客户端TCP连接，
 * 将客户端发送的码流反序列化成对象，反射调用服务端实现者，获取执行结果，
 * 将执行结果序列化，通过socket发送给客户端。
 * 远程服务调用完成之后释放资源。
 *
 * Created by YangHui on 2019/11/22
 */
public class RpcExporter {

    private static ServiceRegistry serviceRegistry = ServiceRegistry.instance;

    public static void exporter(String host, int port) throws Exception {
        serviceRegistry.service.put(EchoService.class, EchoServiceImpl.instance);
        ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        ServerSocket serverSocket = new ServerSocket();
        serverSocket.bind(new InetSocketAddress(host, port));
        try {
            while (true) {
                pool.execute(new ExporterTask(serverSocket.accept()));
            }
        }finally {
            serverSocket.close();
        }
    }

    private static class ExporterTask implements Runnable{

        Socket socket;

        ExporterTask(Socket socket){
            this.socket = socket;
        }

        @Override
        public void run() {
            ObjectInputStream input = null;
            ObjectOutputStream output = null;
            try {
                input = new ObjectInputStream(socket.getInputStream());
                String interfaceName = input.readUTF();
                Class<?> service = Class.forName(interfaceName);
                String methodName = input.readUTF();
                Class<?>[] parameterTypes = (Class<?>[]) input.readObject();
                Object[] parameters = (Object[]) input.readObject();
                Method method = service.getMethod(methodName, parameterTypes);
                Object result = method.invoke(serviceRegistry.service.get(service), parameters);
                output = new ObjectOutputStream(socket.getOutputStream());
                output.writeObject(result);
            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                if(output != null){
                    try {
                        output.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if(input != null){
                    try {
                        input.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if(socket != null){
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
