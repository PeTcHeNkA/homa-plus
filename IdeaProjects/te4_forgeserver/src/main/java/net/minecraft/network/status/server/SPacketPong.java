package net.minecraft.network.status.server;

import java.io.IOException;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.status.INetHandlerStatusClient;
import plus.fserver.server.network.NetUtils;


public class SPacketPong implements Packet<INetHandlerStatusClient> {
    private long clientTime;

    public SPacketPong() {}

    public SPacketPong(long time) {
        this.clientTime = time;
    }


    @Deprecated
    public void readPacketData(PacketBuffer buf) throws IOException {
        this.clientTime = buf.readLong();
    }


    @Override
    public void readPacketData(ByteBuf buf) throws IOException {
        this.clientTime = buf.readLong();
    }


    @Deprecated
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeLong(this.clientTime);
    }


    @Override
    public void writePacketData(int id, ByteBuf buf) throws IOException {
        NetUtils.writeVarInt(buf, id);

        buf.writeLong(this.clientTime);
    }


    @Override
    public void processPacket(INetHandlerStatusClient proc) {
        proc.handlePong(this);
    }
}