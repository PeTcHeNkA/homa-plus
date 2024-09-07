package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.handler.codec.MessageToByteEncoder;
import plus.fserver.server.network.NetUtils;


@Sharable
public class NettyVarint21FrameEncoder extends MessageToByteEncoder<ByteBuf> {
    public static NettyVarint21FrameEncoder INSTANCE = new NettyVarint21FrameEncoder();
    public NettyVarint21FrameEncoder() {}

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf buf, ByteBuf buf2) throws Exception {
        int readable = buf.readableBytes();
        int varIntSize = PacketBuffer.getVarIntSize(readable);
        if (varIntSize > 3) {
            throw new IllegalArgumentException("unable to fit " + readable + " into " + 3);
        } else {
            buf2.ensureWritable(varIntSize + readable);
            NetUtils.writeVarInt(buf2, readable);
            buf2.writeBytes(buf, buf.readerIndex(), readable);
        }
    }


    @Override
    public boolean isSharable() {
        return true;
    }
}
