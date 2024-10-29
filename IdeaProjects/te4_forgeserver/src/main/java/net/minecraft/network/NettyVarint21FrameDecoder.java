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
        byte[] bytes = new byte[3];

        for(int i = 0; i < bytes.length; ++i) {
            if (!buf.isReadable()) {
                buf.resetReaderIndex();
                return;
            }

            bytes[i] = buf.readByte();
            if (bytes[i] >= 0) {
                int size = NetUtils.readVarInt(bytes);
                if (buf.readableBytes() < size) {
                    buf.resetReaderIndex();
                    return;
                }

                msg.add(buf.readBytes(size));
                return;
            }
        }

        throw new CorruptedFrameException("length wider than 21-bit");
    }
}