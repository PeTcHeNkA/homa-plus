package net.minecraft.network.play.server;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.fserver.server.network.NetUtils;
import plus.fserver.server.network.packet.SNodePacketPlayBase;


public class SPacketWindowItems extends SNodePacketPlayBase {
    private int windowId;
    private List<ItemStack> itemStacks;

    public SPacketWindowItems() {}

    public SPacketWindowItems(int id, NonNullList<ItemStack> stacks) {
        this.windowId = id;
        List<ItemStack> itemStacks = this.itemStacks =
                NonNullList.withSize(stacks.size(), ItemStack.EMPTY);

        for(int i = 0; i < itemStacks.size(); ++i) {
            ItemStack stack = stacks.get(i);
            itemStacks.set(i, stack.copyForPacket());
        }
    }


    @Override
    public void readPacketData(PacketBuffer buf) throws IOException {
        this.windowId = buf.readUnsignedByte();
        int size = buf.readShort();
        this.itemStacks = NonNullList.withSize(size, ItemStack.EMPTY);

        for(int i = 0; i < size; ++i) {
            this.itemStacks.set(i, buf.readItemStack());
        }
    }


    @Deprecated
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeByte(this.windowId);
        buf.writeShort(this.itemStacks.size());
        Iterator var2 = this.itemStacks.iterator();

        while(var2.hasNext()) {
            ItemStack stack = (ItemStack)var2.next();
            buf.writeItemStack(stack);
        }
    }


    @Override
    public void writePacketData(int id, ByteBuf buf) throws IOException {
        NetUtils.writeVarInt(buf, id);

        buf.writeByte(this.windowId);
        buf.writeShort(this.itemStacks.size());
        Iterator var2 = this.itemStacks.iterator();

        while(var2.hasNext()) {
            ItemStack stack = (ItemStack)var2.next();
            NetUtils.writeItemStack(buf, stack);
        }
    }


    @Override
    public void processPacket(INetHandlerPlayClient proc) {
        proc.handleWindowItems(this);
    }


    @SideOnly(Side.CLIENT)
    public int getWindowId() {
        return this.windowId;
    }


    @SideOnly(Side.CLIENT)
    public List<ItemStack> getItemStacks() {
        return this.itemStacks;
    }
}