package net.minecraft.network.play.server;

import java.io.IOException;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.fserver.server.network.NetUtils;


public class SPacketDisconnect implements Packet<INetHandlerPlayClient> {
    private ITextComponent reason;

    public SPacketDisconnect() {}

    public SPacketDisconnect(ITextComponent comp) {
        this.reason = comp;
    }


    @Override
    public void readPacketData(PacketBuffer buf) throws IOException {
        this.reason = buf.readTextComponent();
    }


    @Deprecated
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeTextComponent(this.reason);
    }


    @Override
    public void writePacketData(int id, ByteBuf buf) throws IOException {
        NetUtils.writeVarInt(buf, id);

        NetUtils.writeTextComponent(buf, this.reason);
    }


    @Override
    public void processPacket(INetHandlerPlayClient client) {
        client.handleDisconnect(this);
    }


    @SideOnly(Side.CLIENT)
    public ITextComponent getReason() {
        return this.reason;
    }
}