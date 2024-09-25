package net.minecraft.network.play.server;

import java.io.IOException;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.fserver.server.network.NetUtils;
import plus.fserver.server.network.packet.SNodePacketPlayBase;


public class SPacketSetSlot extends SNodePacketPlayBase {
    private int windowId;
    private int slot;
    private ItemStack item;

    public SPacketSetSlot() {
        this.item = ItemStack.EMPTY;
    }

    public SPacketSetSlot(int id, int slot, ItemStack stack) {
        this.item = ItemStack.EMPTY;
        this.windowId = id;
        this.slot = slot;
        this.item = stack.copyForPacket();
    }


    @Override
    public void processPacket(INetHandlerPlayClient client) {
        client.handleSetSlot(this);
    }


    @Override
    public void readPacketData(PacketBuffer buf) throws IOException {
        this.windowId = buf.readByte();
        this.slot = buf.readShort();
        this.item = buf.readItemStack();
    }


    @Deprecated
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeByte(this.windowId);
        buf.writeShort(this.slot);
        buf.writeItemStack(this.item);
    }


    @Override
    public void writePacketData(int id, ByteBuf buf) throws IOException {
        NetUtils.writeVarInt(buf, id);

        buf.writeByte(this.windowId);
        buf.writeShort(this.slot);
        NetUtils.writeItemStack(buf, this.item);
    }


    @SideOnly(Side.CLIENT)
    public int getWindowId() {
        return this.windowId;
    }


    @SideOnly(Side.CLIENT)
    public int getSlot() {
        return this.slot;
    }


    @SideOnly(Side.CLIENT)
    public ItemStack getStack() {
        return this.item;
    }
}
