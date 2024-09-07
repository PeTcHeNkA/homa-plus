package net.minecraft.network.play.server;

import java.io.IOException;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.fserver.server.network.NetUtils;
import plus.fserver.server.network.packet.SPacketPlayBase;


public class SPacketConfirmTransaction extends SPacketPlayBase {
    private int windowId;
    private short actionNumber;
    private boolean accepted;

    public SPacketConfirmTransaction() {}


    public SPacketConfirmTransaction(int id, short num, boolean accepted) {
        this.windowId = id;
        this.actionNumber = num;
        this.accepted = accepted;
    }


    @Override
    public void processPacket(INetHandlerPlayClient client) {
        client.handleConfirmTransaction(this);
    }


    @Override
    public void readPacketData(PacketBuffer buf) throws IOException {
        this.windowId = buf.readUnsignedByte();
        this.actionNumber = buf.readShort();
        this.accepted = buf.readBoolean();
    }


    @Deprecated
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeByte(this.windowId);
        buf.writeShort(this.actionNumber);
        buf.writeBoolean(this.accepted);
    }


    @Override
    public void writePacketData(int id, ByteBuf buf) throws IOException {
        NetUtils.writeVarInt(buf, id);

        buf.writeByte(this.windowId);
        buf.writeShort(this.actionNumber);
        buf.writeBoolean(this.accepted);
    }


    @SideOnly(Side.CLIENT)
    public int getWindowId() {
        return this.windowId;
    }


    @SideOnly(Side.CLIENT)
    public short getActionNumber() {
        return this.actionNumber;
    }


    @SideOnly(Side.CLIENT)
    public boolean wasAccepted() {
        return this.accepted;
    }
}