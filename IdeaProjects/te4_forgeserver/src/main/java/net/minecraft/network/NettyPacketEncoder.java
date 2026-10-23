package net.minecraft.network;

import io.netty.handler.codec.*;
import io.netty.channel.*;
import io.netty.buffer.*;
import io.netty.util.*;
import java.io.*;
import org.apache.logging.log4j.*;
import plus.fserver.ElcaCV;


@ChannelHandler.Sharable
public class NettyPacketEncoder extends MessageToByteEncoder<Packet<?>> {
    private static final Logger LOGGER;
    private static final Marker RECEIVED_PACKET_MARKER;
    private final int direction;

    public NettyPacketEncoder(final EnumPacketDirection direction) {
        this.direction = direction.id;
    }


    @Override
    protected void encode(final ChannelHandlerContext ctx, final Packet<?> packet, final ByteBuf buf) throws IOException, Exception {
        final EnumConnectionState state = (EnumConnectionState)ctx.channel().attr((AttributeKey)NetworkManager.PROTOCOL_ATTRIBUTE_KEY).get();
        if (state == null) {
            throw new RuntimeException("ConnectionProtocol unknown: " + packet);
        }
        final Integer integer = state.getPacketIdF(this.direction, packet);
        if(ElcaCV.ENABLE_PACKET_DEBUG){
            if (NettyPacketEncoder.LOGGER.isDebugEnabled()) {
                NettyPacketEncoder.LOGGER.debug(NettyPacketEncoder.RECEIVED_PACKET_MARKER, "OUT: [{}:{}] {}", ctx.channel().attr((AttributeKey)NetworkManager.PROTOCOL_ATTRIBUTE_KEY).get(), (Object)integer, (Object)packet.getClass().getName());
            }
        }
        if (integer == null) {
            throw new IOException("Can't serialize unregistered packet");
        }
        try {
            packet.writePacketData(integer, buf);
        } catch (Throwable throwable) {
            throw throwable;
        }
    }


    @Override
    public boolean isSharable() {
        return true;
    }


    static {
        LOGGER = LogManager.getLogger();
        RECEIVED_PACKET_MARKER = MarkerManager.getMarker("PACKET_SENT", NetworkManager.NETWORK_PACKETS_MARKER);
    }
}