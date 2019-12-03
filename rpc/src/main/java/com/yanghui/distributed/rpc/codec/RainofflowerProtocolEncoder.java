package com.yanghui.distributed.rpc.codec;

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.ProtocolStringList;
import com.yanghui.distributed.rpc.common.RpcConstants;
import com.yanghui.distributed.rpc.common.util.ClassTypeUtils;
import com.yanghui.distributed.rpc.core.exception.ErrorType;
import com.yanghui.distributed.rpc.core.exception.RpcException;
import com.yanghui.distributed.rpc.protocol.rainofflower.Rainofflower;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.yanghui.distributed.rpc.protocol.rainofflower.RainofflowerVersion.*;


/**
 * Rainofflower协议编码器
 *
 * @author YangHui
 */
public class RainofflowerProtocolEncoder extends MessageToByteEncoder<Rainofflower.Message> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Rainofflower.Message msg, ByteBuf out) throws Exception {
        //Header

//        Rainofflower.Header header = msg.getHeader();

        //crcCode 长度4字节，32 bit
        out.writeShort(CRC_PREFIX);
        out.writeByte(MAJOR_VERSION);
        out.writeByte(MINOR_VERSION);
//        if(msg.getHeader().getType().getNumber() == 0){
//            //rpc请求消息
//            Rainofflower.BizRequest bizRequest = msg.getBizRequest();
//            List<Any> argsList = bizRequest.getArgsList();
//            ProtocolStringList paramTypesList = bizRequest.getParamTypesList();
//            for(String paramType : paramTypesList){
//                Class clazz = ClassTypeUtils.getClass(paramType);
//                argsList.get(0).
//            }
//        }
        byte[] bytes;
        try{
            bytes = msg.toByteArray();
        }catch (Exception e){
            throw new RpcException(ErrorType.CLIENT_SERIALIZE,"消息内容序列化失败",e);
        }
        //除crcCode和length所有数据长度
        out.writeInt(bytes.length);
        out.writeBytes(bytes);

//        //interfaceName 接口名，先 2 字节 长度，再是 接口名
//        ByteString interfaceNameBytes = header.getInterfaceNameBytes();
//        int interfaceLength = interfaceNameBytes.size();
//        byte[] interfaceBytes = interfaceNameBytes.toByteArray();
//
//        //method 方法名，先 2 字节长度，再是 方法名
//        ByteString methodNameBytes = header.getMethodNameBytes();
//        int methodLength = methodNameBytes.size();
//        byte[] methodBytes = methodNameBytes.toByteArray();
//
//        //attachment 扩展字段，先是个数 1 字节，
//        int attachmentCount = header.getAttachmentCount();
//
//        int attachmentLength = 1;
//        List<Integer> keyLengthList = null;
//        List<Integer> valueLengthList = null;
//        List<byte[]> keyList = null;
//        List<byte[]> valueList = null;
//        if(attachmentCount > 0){
//            keyLengthList = new ArrayList(attachmentCount);
//            valueLengthList = new ArrayList(attachmentCount);
//            keyList = new ArrayList(attachmentCount);
//            valueList = new ArrayList(attachmentCount);
//            Map<String, String> attachmentMap = header.getAttachmentMap();
//            for(Map.Entry<String, String> item : attachmentMap.entrySet()){
//                String key = item.getKey();
//                String value = item.getValue();
//                byte[] keyBytes = key.getBytes(RpcConstants.DEFAULT_CHARSET);
//                //key 长度 2 字节， key 内容
//                int keyLength = keyBytes.length;
//                keyLengthList.add(keyLength);
//                keyList.add(keyBytes);
//                byte[] valueBytes = value.getBytes(RpcConstants.DEFAULT_CHARSET);
//                //value 长度 2 字节，value 内容
//                int valueLength = valueBytes.length;
//                valueLengthList.add(valueLength);
//                valueList.add(valueBytes);
//                attachmentLength += 2 + keyLength + 2 + valueLength;
//            }
//        }
//
//        //Body 4 字节 长度，后面是byte数组
//        Rainofflower.Body body = msg.getBody();
//        ByteString contentBytes = body.getContentBytes();
//        int contentLength = contentBytes.size();
//
//        //length 消息长度，包括消息头和消息体，4字节，32 bit
//        int length = 10 + 2 + interfaceLength + 2 + methodLength + attachmentLength + contentLength;
//        out.writeInt(length);
//
//        //type 消息类型，长度1字节，8 bit
//        int type = header.getType().getNumber();
//        out.writeByte(1);
//
//        //priority 消息优先级，1字节，8 bit
//        out.writeByte(type);
//
//        out.writeShort(interfaceLength);
//        out.writeBytes(interfaceBytes);
//
//        out.writeShort(methodLength);
//        out.writeBytes(methodBytes);
//
//        out.writeByte(attachmentCount);
//        if(attachmentCount > 0){
//            for(int i = 0; i<attachmentCount ; i++){
//                out.writeShort(keyLengthList.get(i));
//                out.writeBytes(keyList.get(i));
//                out.writeShort(valueLengthList.get(i));
//                out.writeBytes(valueList.get(i));
//            }
//        }
//
//        out.writeBytes(contentBytes.toByteArray());
    }
}
