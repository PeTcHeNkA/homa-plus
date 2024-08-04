package net.minecraft.network.login.server;

import java.io.IOException;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.login.INetHandlerLoginClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.fserver.server.network.NetUtils;


public class SPacketEnableCompression implements Packet<INetHandlerLoginClient> {
    private int compressionThreshold;

    public SPacketEnableCompression() {}

    public SPacketEnableCompression(int threshold) {
        this.compressionThreshold = threshold;
    }


    @Override
    public void readPacketData(PacketBuffer buf) throws IOException {
        this.compressionThreshold = buf.readVarInt();
    }


    @Override
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeVarInt(this.compressionThreshold);
    }


    @Override
    public void writePacketData(int id, ByteBuf buf) throws IOException {
        NetUtils.writeVarInt(buf, id);

        NetUtils.writeVarInt(buf, this.compressionThreshold);
    }


    @Override
    public void processPacket(INetHandlerLoginClient proc) {
        proc.handleEnableCompression(this);
    }


    @SideOnly(Side.CLIENT)
    public int getCompressionThreshold() {
        return this.compressionThreshold;
    }
}