package net.minecraft.network.status.client;

import java.io.IOException;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.status.INetHandlerStatusServer;
import plus.fserver.server.network.NetUtils;


public class CPacketServerQuery implements Packet<INetHandlerStatusServer> {
    public CPacketServerQuery() {}

    @Deprecated
    public void readPacketData(PacketBuffer buf) throws IOException {}


    @Override
    public void readPacketData(ByteBuf buf) throws IOException {}


    @Deprecated
    public void writePacketData(PacketBuffer buf) throws IOException {}


    @Override
    public void writePacketData(int id, ByteBuf buf) throws IOException {
        NetUtils.writeVarInt(buf, id);
    }


    @Override
    public void processPacket(INetHandlerStatusServer proc) {
        proc.processServerQuery(this);
    }
}