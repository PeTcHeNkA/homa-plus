package plus.common.etc;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import plus.common.network.PacketSyncRenderItem;


public class SyncRenderSlot extends Slot {
    protected SyncRenderSlot(IInventory inventoryIn, int index, int xPosition, int yPosition) {
        super(inventoryIn, index, xPosition, yPosition);
    }


    public static SyncRenderSlot build(IInventory inventoryIn, int index, int xPosition, int yPosition, EntityPlayer player){
        if(!player.world.isRemote){
            return new SyncSlotServer(inventoryIn, index, xPosition, yPosition);
        } else {
            return new SyncSlotClient(inventoryIn, index, xPosition, yPosition);
        }
    }


    public void onSynchronizedSlotChange(EntityPlayer player) {}
}


final class SyncSlotServer extends SyncRenderSlot {
    public SyncSlotServer(IInventory inventoryIn, int index, int xPosition, int yPosition) {
        super(inventoryIn, index, xPosition, yPosition);
    }

    public void onSynchronizedSlotChange(EntityPlayer player) {
        plus.server.ServPacketSyncRenderItem.updatePlayerCache((EntityPlayerMP) player, getSlotIndex(), getStack());
    }
}


final class SyncSlotClient extends SyncRenderSlot {
    public SyncSlotClient(IInventory inventoryIn, int index, int xPosition, int yPosition) {
        super(inventoryIn, index, xPosition, yPosition);
    }

    @Override
    public void onSlotChanged() {
        PacketSyncRenderItem.updateSelfCache(getSlotIndex(),getStack());
    }
}