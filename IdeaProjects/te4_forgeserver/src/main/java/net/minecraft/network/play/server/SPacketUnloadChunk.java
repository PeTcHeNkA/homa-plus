package net.minecraft.network.play.server;

import java.io.IOException;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.fserver.server.network.NetUtils;
import plus.fserver.server.network.packet.SNodePacketPlayBase;


public class SPacketUnloadChunk extends SNodePacketPlayBase {
    private int x;
    private int z;

    public SPacketUnloadChunk() {}


    public SPacketUnloadChunk(int x, int z) {
        this.x = x;
        this.z = z;
    }


    @Override
    public void readPacketData(PacketBuffer buf) throws IOException {
        this.x = buf.readInt();
        this.z = buf.readInt();
    }


    @Deprecated
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeInt(this.x);
        buf.writeInt(this.z);
    }


    @Override
    public void writePacketData(int id, ByteBuf buf) throws IOException {
        NetUtils.writeVarInt(buf, id);

        buf.writeInt(this.x);
        buf.writeInt(this.z);
    }


    @Override
    public void processPacket(INetHandlerPlayClient client) {
        client.processChunkUnload(this);
    }


    @SideOnly(Side.CLIENT)
    public int getX() {
        return this.x;
    }


    @SideOnly(Side.CLIENT)
    public int getZ() {
        return this.z;
    }
}