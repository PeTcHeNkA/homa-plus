package net.minecraft.network.play.server;

import java.io.IOException;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.fserver.server.network.NetUtils;
import plus.fserver.server.network.packet.SPacketPlayBase;


public class SPacketWindowProperty extends SPacketPlayBase {
    private int windowId;
    private int property;
    private int value;

    public SPacketWindowProperty() {}


    public SPacketWindowProperty(int id, int property, int value) {
        this.windowId = id;
        this.property = property;
        this.value = value;
    }


    @Override
    public void processPacket(INetHandlerPlayClient client) {
        client.handleWindowProperty(this);
    }


    @Override
    public void readPacketData(PacketBuffer buf) throws IOException {
        this.windowId = buf.readUnsignedByte();
        this.property = buf.readShort();
        this.value = buf.readShort();
    }


    @Deprecated
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeByte(this.windowId);
        buf.writeShort(this.property);
        buf.writeShort(this.value);
    }


    @Override
    public void writePacketData(int id, ByteBuf buf) throws IOException {
        NetUtils.writeVarInt(buf, id);

        buf.writeByte(this.windowId);
        buf.writeShort(this.property);
        buf.writeShort(this.value);
    }


    @SideOnly(Side.CLIENT)
    public int getWindowId() {
        return this.windowId;
    }


    @SideOnly(Side.CLIENT)
    public int getProperty() {
        return this.property;
    }


    @SideOnly(Side.CLIENT)
    public int getValue() {
        return this.value;
    }
}
