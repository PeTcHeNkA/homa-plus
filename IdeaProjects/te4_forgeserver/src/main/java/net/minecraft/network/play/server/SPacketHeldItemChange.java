package net.minecraft.network.play.server;

import java.io.IOException;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.fserver.server.network.NetUtils;
import plus.fserver.server.network.packet.SPacketPlayBase;


public class SPacketHeldItemChange extends SPacketPlayBase {
    private int heldItemHotbarIndex;

    public SPacketHeldItemChange() {}


    public SPacketHeldItemChange(int index) {
        this.heldItemHotbarIndex = index;
    }


    @Override
    public void readPacketData(PacketBuffer buf) throws IOException {
        this.heldItemHotbarIndex = buf.readByte();
    }


    @Deprecated
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeByte(this.heldItemHotbarIndex);
    }


    @Override
    public void writePacketData(int id, ByteBuf buf) throws IOException {
        NetUtils.writeVarInt(buf, id);

        buf.writeByte(this.heldItemHotbarIndex);
    }


    @Override
    public void processPacket(INetHandlerPlayClient client) {
        client.handleHeldItemChange(this);
    }


    @SideOnly(Side.CLIENT)
    public int getHeldItemHotbarIndex() {
        return this.heldItemHotbarIndex;
    }
}