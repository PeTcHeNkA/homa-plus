package plus.fserver.server.network;

import com.velocitypowered.natives.compression.VelocityCompressor;
import com.velocitypowered.natives.util.MoreByteBufUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.PacketBuffer;
import java.util.List;
import java.util.zip.DataFormatException;


public class NativeCompressionDecoder extends ByteToMessageDecoder {
    private final VelocityCompressor compressor;
    private int threshold;

    public NativeCompressionDecoder(VelocityCompressor compressor, int thresholdIn) {
        this.threshold = thresholdIn;
        this.compressor = compressor;
    }


    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws DataFormatException, Exception {
        if (in.readableBytes() != 0) {
            PacketBuffer packetbuffer = new PacketBuffer(in);
            int size = packetbuffer.readVarInt();

            if (size == 0) {
                out.add(packetbuffer.readBytes(packetbuffer.readableBytes()));
            } else {
                if (size < this.threshold) {
                    throw new DecoderException("Badly compressed packet - size of " + size + " is below server threshold of " + this.threshold);
                }
                if (size > 2097152) {
                    throw new DecoderException("Badly compressed packet - size of " + size + " is larger than protocol maximum of " + 2097152);
                }

                ByteBuf compatibleIn = MoreByteBufUtils.ensureCompatible(ctx.alloc(), this.compressor, in);
                ByteBuf uncompressed = MoreByteBufUtils.preferredBuffer(ctx.alloc(), this.compressor, size);

                try {
                    this.compressor.inflate(compatibleIn, uncompressed, size);
                    out.add(uncompressed);
                } catch (Exception e) {
                    uncompressed.release();
                    throw e;
                } finally {
                    compatibleIn.release();
                }
            }
        }
    }


    public void setCompressionThreshold(int thresholdIn) {
        this.threshold = thresholdIn;
    }
}