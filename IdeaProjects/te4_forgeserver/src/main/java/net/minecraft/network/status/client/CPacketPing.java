package net.minecraft.network.status.client;

import java.io.IOException;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.status.INetHandlerStatusServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


public class CPacketPing implements Packet<INetHandlerStatusServer> {
    private long clientTime;

    public CPacketPing() {}

    @SideOnly(Side.CLIENT)
    public CPacketPing(long time) {
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


    @Override
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeLong(this.clientTime);
    }


    @Override
    public void processPacket(INetHandlerStatusServer proc) {
        proc.processPing(this);
    }


    public long getClientTime() {
        return this.clientTime;
    }
}