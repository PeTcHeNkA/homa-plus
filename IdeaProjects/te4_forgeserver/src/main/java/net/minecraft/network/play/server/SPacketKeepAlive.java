package net.minecraft.network.play.server;

import java.io.IOException;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.fserver.server.network.NetUtils;


public class SPacketKeepAlive implements Packet<INetHandlerPlayClient> {
    private long id;

    public SPacketKeepAlive() {}


    public SPacketKeepAlive(long id) {
        this.id = id;
    }


    @Override
    public void processPacket(INetHandlerPlayClient client) {
        client.handleKeepAlive(this);
    }


    @Override
    public void readPacketData(PacketBuffer buf) throws IOException {
        this.id = buf.readLong();
    }


    @Deprecated
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeLong(this.id);
    }


    @Override
    public void writePacketData(int id, ByteBuf buf) throws IOException {
        NetUtils.writeVarInt(buf, id);

        buf.writeLong(this.id);
    }


    @SideOnly(Side.CLIENT)
    public long getId() {
        return this.id;
    }
}