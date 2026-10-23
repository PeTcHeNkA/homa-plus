package plus.fserver.server.zstd;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.PacketBuffer;
import java.util.List;
import java.util.zip.DataFormatException;


public class NettyZSTDCompressionDecoder extends ByteToMessageDecoder {
    private final ZStdCoder coder;
    private int threshold;

    public NettyZSTDCompressionDecoder(ZStdCoder coder) {
        this.coder = coder;
    }


//    public NettyZSTDCompressionDecoder(int keySize, int lvl, byte[] key) {
//        coder = new ZStdCoder(keySize, lvl).loadKey(key);
//    }


    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> objects) throws DataFormatException, Exception {
        if (buf.readableBytes() != 0) {
            PacketBuffer packetbuffer = new PacketBuffer(buf);
            int i = packetbuffer.readVarInt();

            if (i == 0) {
                objects.add(packetbuffer.readBytes(packetbuffer.readableBytes()));
            } else {
                if (i < this.threshold) {
                    throw new DecoderException("Badly compressed packet - size of " + i + " is below server threshold of " + this.threshold);
                }

                if (i > 2097152) {
                    throw new DecoderException("Badly compressed packet - size of " + i + " is larger than protocol maximum of " + 2097152);
                }

                byte[] inputBytes = new byte[packetbuffer.readableBytes()];
                packetbuffer.readBytes(inputBytes);

                objects.add(Unpooled.wrappedBuffer(coder.decompress(inputBytes)));
            }
        }
    }


    public void setCompressionThreshold(int thresholdIn) {
        //this.threshold = thresholdIn;
    }
}