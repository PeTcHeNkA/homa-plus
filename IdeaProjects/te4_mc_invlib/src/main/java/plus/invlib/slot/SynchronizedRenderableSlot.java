package plus.invlib.slot;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import plus.invlib.network.PacketSyncRenderItem;
//import static plus.network.ItemSynchonolizer.syncIndexes;


public class SynchronizedRenderableSlot extends SSlot {

    protected SynchronizedRenderableSlot(IInventory inventoryIn, int index, int xPosition, int yPosition) {
        super(inventoryIn, index, xPosition, yPosition);
    }

    public static SynchronizedRenderableSlot build(IInventory inventoryIn, int index, int xPosition, int yPosition, EntityPlayer player){
        if(player instanceof EntityPlayerMP){
            return new SyncSlotServer(inventoryIn, index, xPosition, yPosition, (EntityPlayerMP) player);
        } else {
            return new SyncSlotClient(inventoryIn, index, xPosition, yPosition);
        }
    }


    public void onSynchronizedSlotChange(EntityPlayer player) {}
}
class SyncSlotServer extends SynchronizedRenderableSlot{
//    private final EntityPlayerMP playerMp;
    public SyncSlotServer(IInventory inventoryIn, int index, int xPosition, int yPosition, EntityPlayerMP playerMp) {
        super(inventoryIn, index, xPosition, yPosition);
//        syncIndexes.add(index);
//        this.playerMp = playerMp;
    }

    public void onSynchronizedSlotChange(EntityPlayer player) {
        PacketSyncRenderItem.updatePlayerCache((EntityPlayerMP) player, getSlotIndex(), getStack());
    }
}
class SyncSlotClient extends SynchronizedRenderableSlot{
    public SyncSlotClient(IInventory inventoryIn, int index, int xPosition, int yPosition) {
        super(inventoryIn, index, xPosition, yPosition);
    }

    @Override
    public void onSlotChanged() {
        PacketSyncRenderItem.updateSelfCache(getSlotIndex(),getStack());
    }
}