package com.yanghui.distributed.rpc.codec;

import com.yanghui.distributed.rpc.core.exception.RpcRuntimeException;
import com.yanghui.distributed.rpc.protocol.rainofflower.Rainofflower;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

import static com.yanghui.distributed.rpc.protocol.rainofflower.RainofflowerVersion.*;

/**
 * Rainofflower协议解码器
 *
 * @author YangHui
 */
public class RainofflowerProtocolDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if(in.readableBytes() < 8){
            return;
        }
        in.markReaderIndex();
        //读取crcPrefix
        short crcPrefix = in.readShort();
        if(CRC_PREFIX != crcPrefix){
            throw new RpcRuntimeException("crc校验失败，rainofflower协议前缀错误！读取到crcPrefix内容："+crcPrefix);
        }
        //读取majorVersion
        byte majorVersion = in.readByte();
        if(MAJOR_VERSION != majorVersion){
            throw new RpcRuntimeException("crc校验失败，rainofflower主版本号错误！读取到majorVersion内容："+majorVersion);
        }
        //读取minorVersion
        byte minorVersion = in.readByte();
        if(MINOR_VERSION != minorVersion){
            throw new RpcRuntimeException("crc校验失败，rainofflower次版本号错误！读取到minorVersion内容："+minorVersion);
        }
        int length = in.readInt();
        if(in.readableBytes() < length){
            in.resetReaderIndex();
            return;
        }
        byte[] data;
        if(in.hasArray()){
            ByteBuf slice = in.slice();
            data = slice.array();
        }else{
            data = new byte[length];
            in.readBytes(data, 0, length);
        }
        Rainofflower.Message message = Rainofflower.Message.parseFrom(data);
        if(message != null){
            out.add(message);
        }
    }
}
