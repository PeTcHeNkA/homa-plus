package net.minecraft.network.play.client;

import java.io.IOException;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


public class CPacketKeepAlive implements Packet<INetHandlerPlayServer> {
    private long key;

    public CPacketKeepAlive() {}


    @SideOnly(Side.CLIENT)
    public CPacketKeepAlive(long key) {
        this.key = key;
    }


    @Override
    public void processPacket(INetHandlerPlayServer proc) {
        proc.processKeepAlive(this);
    }


    @Deprecated
    public void readPacketData(PacketBuffer buf) throws IOException {
        this.key = buf.readLong();
    }


    @Override
    public void readPacketData(ByteBuf buf) throws IOException {
        this.key = buf.readLong();
    }


    @Override
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeLong(this.key);
    }


    public long getKey() {
        return this.key;
    }
}