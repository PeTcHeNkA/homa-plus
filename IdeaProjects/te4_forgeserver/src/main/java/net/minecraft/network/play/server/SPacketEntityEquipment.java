package net.minecraft.network.play.server;

import java.io.IOException;
import io.netty.buffer.ByteBuf;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.fserver.server.network.NetUtils;
import plus.fserver.server.network.packet.SPacketPlayBase;


public class SPacketEntityEquipment extends SPacketPlayBase {
    private int entityID;
    private EntityEquipmentSlot equipmentSlot;
    private ItemStack itemStack;

    public SPacketEntityEquipment() {
        this.itemStack = ItemStack.EMPTY;
    }

    public SPacketEntityEquipment(int id, EntityEquipmentSlot slot, ItemStack stack) {
        this.itemStack = ItemStack.EMPTY;
        this.entityID = id;
        this.equipmentSlot = slot;
        this.itemStack = stack.copy();
    }


    @Override
    public void readPacketData(PacketBuffer buf) throws IOException {
        this.entityID = buf.readVarInt();
        this.equipmentSlot = buf.readEnumValue(EntityEquipmentSlot.class);
        this.itemStack = buf.readItemStack();
    }


    @Deprecated
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeVarInt(this.entityID);
        buf.writeEnumValue(this.equipmentSlot);
        buf.writeItemStack(this.itemStack);
    }


    @Override
    public void writePacketData(int id, ByteBuf buf) throws IOException {
        NetUtils.writeVarInt(buf, id);

        NetUtils.writeVarInt(buf, this.entityID);
        NetUtils.writeEnumValue(buf, this.equipmentSlot);
        NetUtils.writeItemStack(buf, this.itemStack);
    }


    @Override
    public void processPacket(INetHandlerPlayClient client) {
        client.handleEntityEquipment(this);
    }


    @SideOnly(Side.CLIENT)
    public ItemStack getItemStack() {
        return this.itemStack;
    }


    @SideOnly(Side.CLIENT)
    public int getEntityID() {
        return this.entityID;
    }


    @SideOnly(Side.CLIENT)
    public EntityEquipmentSlot getEquipmentSlot() {
        return this.equipmentSlot;
    }
}