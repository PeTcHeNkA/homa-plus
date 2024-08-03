package plus.fserver.server.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.server.SPacketEntityEquipment;
import java.io.IOException;


public class SElcaPacketEntityEquipment extends SPacketEntityEquipment {
    public SElcaPacketEntityEquipment next;

    public SElcaPacketEntityEquipment(int id, EntityEquipmentSlot slot, ItemStack stack) {
        super(id, slot, stack);
    }


    public SElcaPacketEntityEquipment with(int id, EntityEquipmentSlot slot, ItemStack stack) {
        return next = new SElcaPacketEntityEquipment(id, slot, stack);
    }


    @Override
    public void writePacketData(int id, ByteBuf buf) throws IOException {
        super.writePacketData(id, buf);
        if(next != null)next.writePacketData(id, buf);
    }


    @Override
    public void processPacket(INetHandlerPlayClient client) {
        super.processPacket(client);
        if(next != null) next.processPacket(client);
    }


    public SElcaPacketEntityEquipment next() {
        return next;
    }
}
