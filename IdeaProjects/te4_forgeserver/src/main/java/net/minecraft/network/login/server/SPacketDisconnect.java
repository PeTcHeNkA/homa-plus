package net.minecraft.network.login.server;

import java.io.IOException;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.login.INetHandlerLoginClient;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextComponent.Serializer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.fserver.server.network.NetUtils;


public class SPacketDisconnect implements Packet<INetHandlerLoginClient> {
    private ITextComponent reason;

    public SPacketDisconnect() {}


    public SPacketDisconnect(ITextComponent reason) {
        this.reason = reason;
    }


    @Override
    public void readPacketData(PacketBuffer buf) throws IOException {
        this.reason = Serializer.fromJsonLenient(buf.readString(32767));
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
    public void processPacket(INetHandlerLoginClient proc) {
        proc.handleDisconnect(this);
    }


    @SideOnly(Side.CLIENT)
    public ITextComponent getReason() {
        return this.reason;
    }
}