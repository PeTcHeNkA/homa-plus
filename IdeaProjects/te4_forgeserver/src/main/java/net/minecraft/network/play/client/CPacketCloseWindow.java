package net.minecraft.network.play.client;

import java.io.IOException;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


public class CPacketCloseWindow implements Packet<INetHandlerPlayServer> {
    private int windowId;

    public CPacketCloseWindow() {}

    @SideOnly(Side.CLIENT)
    public CPacketCloseWindow(int windowId) {
        this.windowId = windowId;
    }


    @Override
    public void processPacket(INetHandlerPlayServer proc) {
        proc.processCloseWindow(this);
    }


    @Deprecated
    public void readPacketData(PacketBuffer buf) throws IOException {
        this.windowId = buf.readByte();
    }


    @Override
    public void readPacketData(ByteBuf buf) throws IOException {
        this.windowId = buf.readByte();
    }


    @Override
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeByte(this.windowId);
    }
}