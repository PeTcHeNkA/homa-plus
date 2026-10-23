package plus.fserver.server.zstd;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import net.minecraft.network.PacketBuffer;


public class NettyZSTDCompressionEncoder extends MessageToByteEncoder<ByteBuf> {
    private final ZStdCoder coder;
//    private final byte[] buffer = new byte[8192];
//    private final Deflater deflater;
    private int threshold;

    public NettyZSTDCompressionEncoder(int thresholdIn, ZStdCoder coder) {
//        this.threshold = thresholdIn;
//        this.deflater = new Deflater();
        this.coder = coder;
    }


    protected void encode(ChannelHandlerContext ctx, ByteBuf buf, ByteBuf buf2) throws Exception {
        int i = buf.readableBytes();
        PacketBuffer packetbuffer = new PacketBuffer(buf2);

        if (i < this.threshold) {
            packetbuffer.writeVarInt(0);
            packetbuffer.writeBytes(buf);
        }
        else {
            byte[] count = new byte[i];
            buf.readBytes(count);
            packetbuffer.writeVarInt(count.length);



            packetbuffer.writeBytes(coder.decompress(count));

            //coder.decompress(count);

//            this.deflater.setInput(count, 0, i);
//            this.deflater.finish();

//            while (!this.deflater.finished())
//            {
//                int j = this.deflater.deflate(this.buffer);
//                packetbuffer.writeBytes(this.buffer, 0, j);
//            }
//
//            this.deflater.reset();
        }
    }


    public void setCompressionThreshold(int thresholdIn)
    {
        this.threshold = thresholdIn;
    }
}