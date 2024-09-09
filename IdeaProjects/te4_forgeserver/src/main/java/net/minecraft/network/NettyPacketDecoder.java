package net.minecraft.network;

import io.netty.handler.codec.*;
import io.netty.channel.*;
import io.netty.buffer.*;
import java.util.*;
import io.netty.util.*;
import java.io.*;
import org.apache.logging.log4j.*;
import plus.fserver.ElcaCV;
import plus.fserver.server.network.NetUtils;


public class NettyPacketDecoder extends ByteToMessageDecoder {
    private static final Logger LOGGER;
    private static final Marker RECEIVED_PACKET_MARKER;
    private final int direction;

    public NettyPacketDecoder(final EnumPacketDirection direction) {
        this.direction = direction.id;
    }


    @Override
    protected void decode(final ChannelHandlerContext ctx, final ByteBuf buf, final List<Object> msg) throws IOException, Exception, IllegalAccessException, InstantiationException {
        if (buf.readableBytes() == 0) return;

        //final PacketBuffer pBuffer = new PacketBuffer(buf);
        final int id = NetUtils.readVarInt(buf);
        final Packet<?> packet = ((EnumConnectionState)ctx.channel().attr((AttributeKey)NetworkManager.PROTOCOL_ATTRIBUTE_KEY).get()).getPacketF(this.direction, id);
        if (packet == null) {
            throw new IOException("Bad packet id " + id);
        }
        packet.readPacketData(buf);
        if (buf.readableBytes() > 0) {
            throw new IOException("Packet " + ((EnumConnectionState)ctx.channel().attr((AttributeKey)NetworkManager.PROTOCOL_ATTRIBUTE_KEY).get()).getId() + "/" + id + " (" + packet.getClass().getSimpleName() + ") was larger than I expected, found " + buf.readableBytes() + " bytes extra whilst reading packet " + id);
        }
        msg.add(packet);
        if(ElcaCV.ENABLE_PACKET_DEBUG) {
            if (NettyPacketDecoder.LOGGER.isDebugEnabled()) {
                NettyPacketDecoder.LOGGER.debug(NettyPacketDecoder.RECEIVED_PACKET_MARKER, " IN: [{}:{}] {}", ctx.channel().attr((AttributeKey) NetworkManager.PROTOCOL_ATTRIBUTE_KEY).get(), id, packet.getClass().getName());
            }
        }
    }


    static {
        LOGGER = LogManager.getLogger();
        RECEIVED_PACKET_MARKER = MarkerManager.getMarker("PACKET_RECEIVED", NetworkManager.NETWORK_PACKETS_MARKER);
    }
}