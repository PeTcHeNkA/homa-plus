package net.minecraft.network.play.server;

import java.io.IOException;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.fserver.server.network.NetUtils;
import plus.fserver.server.network.packet.SNodePacketPlayBase;


public class SPacketTimeUpdate extends SNodePacketPlayBase {
    private long totalWorldTime;
    private long worldTime;

    public SPacketTimeUpdate() {}


    public SPacketTimeUpdate(long total, long worldTime, boolean flag) {
        this.totalWorldTime = total;
        this.worldTime = worldTime;
        if (!flag) {
            this.worldTime = -this.worldTime;
            if (this.worldTime == 0L) {
                this.worldTime = -1L;
            }
        }
    }


    @Deprecated
    public void readPacketData(PacketBuffer buf) throws IOException {
        this.totalWorldTime = buf.readLong();
        this.worldTime = buf.readLong();
    }


    @Deprecated
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeLong(this.totalWorldTime);
        buf.writeLong(this.worldTime);
    }


    @Override
    public void writePacketData(int id, ByteBuf buf) throws IOException {
        NetUtils.writeVarInt(buf, id);

        buf.writeLong(this.totalWorldTime);
        buf.writeLong(this.worldTime);
    }


    @Override
    public void processPacket(INetHandlerPlayClient client) {
        client.handleTimeUpdate(this);
    }


    @SideOnly(Side.CLIENT)
    public long getTotalWorldTime() {
        return this.totalWorldTime;
    }


    @SideOnly(Side.CLIENT)
    public long getWorldTime() {
        return this.worldTime;
    }
}