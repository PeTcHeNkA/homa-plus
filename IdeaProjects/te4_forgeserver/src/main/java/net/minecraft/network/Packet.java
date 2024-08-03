package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import plus.fserver.server.network.NetUtils;
import java.io.IOException;


public interface Packet<T extends INetHandler> {
    void readPacketData(PacketBuffer buf) throws IOException;


    void writePacketData(PacketBuffer buf) throws IOException;


    void processPacket(T proc);


    default void writePacketData(int id, ByteBuf buf) throws IOException {
        NetUtils.writeVarInt(buf, id);
        writePacketData(new PacketBuffer(buf));
    }
}
