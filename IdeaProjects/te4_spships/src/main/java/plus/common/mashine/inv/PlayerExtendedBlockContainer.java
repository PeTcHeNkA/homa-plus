package plus.common.mashine.inv;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


public abstract class PlayerExtendedBlockContainer extends PlayerExtendedContainer{
    protected final IInventory inventory;

    public PlayerExtendedBlockContainer(IInventory selfInv){
        inventory = selfInv;
    }


    public void syncAllFields(){
        for(IContainerListener l:listeners){
            for(int id=0;id<inventory.getFieldCount();id++)
                l.sendWindowProperty(this, id, inventory.getField(id));
        }
    }


    public void syncFirstField(int val){
        for(IContainerListener l:listeners){
            l.sendWindowProperty(this, 0, val);
        }
    }


    @SideOnly(Side.CLIENT)
    public void updateProgressBar(int id, int data) {
        inventory.setField(id, data);
    }


    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return inventory.isUsableByPlayer(player);
    }


    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack item = slot.getStack();
            itemstack = item.copy();

            if (index < 9) {
                if (!this.mergeItemStack(item, 9 - 4, 45 - 4, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.mergeItemStack(item, 0, 9, false)) {
                return ItemStack.EMPTY;
            }

            if (item.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }

            if (item.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(playerIn, item);
        }
        return itemstack;
    }
}