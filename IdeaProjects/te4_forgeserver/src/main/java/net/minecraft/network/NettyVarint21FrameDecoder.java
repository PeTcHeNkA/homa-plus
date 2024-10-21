package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.CorruptedFrameException;
import java.util.List;
import plus.fserver.server.network.NetUtils;


public class NettyVarint21FrameDecoder extends ByteToMessageDecoder {
    public NettyVarint21FrameDecoder() {}

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> msg) throws Exception {
        buf.markReaderIndex();

        byte b1, b2, b3;
        if (!buf.isReadable()) {
            buf.resetReaderIndex();
            return;
        }
        b1 = buf.readByte();
        if(b1 >= 0){
            encodeMsg(buf, msg, NetUtils.readVarInt(b1, (byte) 0, (byte) 0));
            return;
        }

        if (!buf.isReadable()) {
            buf.resetReaderIndex();
            return;
        }
        b2 = buf.readByte();
        if(b2 >= 0){
            encodeMsg(buf, msg, NetUtils.readVarInt(b1, b2, (byte) 0));
            return;
        }

        if (!buf.isReadable()) {
            buf.resetReaderIndex();
            return;
        }
        b3 = buf.readByte();
        if(b3 >= 0){
            encodeMsg(buf, msg, NetUtils.readVarInt(b1, b2, b3));
            return;
        }
        throw new CorruptedFrameException("length wider than 21-bit");
    }


    private static void encodeMsg(ByteBuf buf, List<Object> msg, int size){
        if (buf.readableBytes() < size) {
            buf.resetReaderIndex();
            return;
        }
        msg.add(buf.readBytes(size));
    }
}