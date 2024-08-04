package net.minecraft.network.play.client;

import java.io.IOException;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


public class CPacketHeldItemChange implements Packet<INetHandlerPlayServer> {
    private int slotId;

    public CPacketHeldItemChange() {}


    @SideOnly(Side.CLIENT)
    public CPacketHeldItemChange(int slotId) {
        this.slotId = slotId;
    }


    @Deprecated
    public void readPacketData(PacketBuffer buf) throws IOException {
        this.slotId = buf.readShort();
    }


    @Override
    public void readPacketData(ByteBuf buf) throws IOException {
        this.slotId = buf.readShort();
    }


    @Override
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeShort(this.slotId);
    }


    @Override
    public void processPacket(INetHandlerPlayServer proc) {
        proc.processHeldItemChange(this);
    }


    public int getSlotId() {
        return this.slotId;
    }
}