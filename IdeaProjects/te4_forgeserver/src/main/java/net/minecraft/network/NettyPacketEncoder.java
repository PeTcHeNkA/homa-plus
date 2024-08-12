package net.minecraft.network;

import io.netty.handler.codec.*;
import io.netty.channel.*;
import io.netty.buffer.*;
import io.netty.util.*;
import java.io.*;
import org.apache.logging.log4j.*;


public class NettyPacketEncoder extends MessageToByteEncoder<Packet<?>> {
    private static final Logger LOGGER;
    private static final Marker RECEIVED_PACKET_MARKER;
    private final EnumPacketDirection direction;

    public NettyPacketEncoder(final EnumPacketDirection direction) {
        this.direction = direction;
    }


    @Override
    protected void encode(final ChannelHandlerContext ctx, final Packet<?> packet, final ByteBuf buf) throws IOException, Exception {
        final EnumConnectionState state = (EnumConnectionState)ctx.channel().attr((AttributeKey)NetworkManager.PROTOCOL_ATTRIBUTE_KEY).get();
        if (state == null) {
            throw new RuntimeException("ConnectionProtocol unknown: " + packet);
        }
        final Integer integer = state.getPacketId(this.direction, packet);
        if (NettyPacketEncoder.LOGGER.isDebugEnabled()) {
            NettyPacketEncoder.LOGGER.debug(NettyPacketEncoder.RECEIVED_PACKET_MARKER, "OUT: [{}:{}] {}", ctx.channel().attr((AttributeKey)NetworkManager.PROTOCOL_ATTRIBUTE_KEY).get(), (Object)integer, (Object)packet.getClass().getName());
        }
        if (integer == null) {
            throw new IOException("Can't serialize unregistered packet");
        }
        //final PacketBuffer packetbuffer = new PacketBuffer(buf);
        //packetbuffer.writeVarInt(integer);
        try {
            packet.writePacketData(integer, buf);
            //packet.writePacketData(packetbuffer);
        }
        catch (Throwable throwable) {
            throw throwable;
        }
    }

    static {
        LOGGER = LogManager.getLogger();
        RECEIVED_PACKET_MARKER = MarkerManager.getMarker("PACKET_SENT", NetworkManager.NETWORK_PACKETS_MARKER);
    }
}