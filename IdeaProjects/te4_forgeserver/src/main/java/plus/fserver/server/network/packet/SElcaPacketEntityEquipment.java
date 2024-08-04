package plus.fserver.server.network.packet;

import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketEntityEquipment;


public class SElcaPacketEntityEquipment extends SPacketEntityEquipment {
    public SElcaPacketEntityEquipment next;

    public SElcaPacketEntityEquipment(int id, EntityEquipmentSlot slot, ItemStack stack) {
        super(id, slot, stack);
    }


    public SElcaPacketEntityEquipment with(int id, EntityEquipmentSlot slot, ItemStack stack) {
        return next = new SElcaPacketEntityEquipment(id, slot, stack);
    }


    public SElcaPacketEntityEquipment next() {
        return next;
    }
}