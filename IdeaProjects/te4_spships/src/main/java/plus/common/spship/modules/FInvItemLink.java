package plus.common.spship.modules;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import plus.common.mashine.inv.IItemLink;


public final class FInvItemLink implements IItemLink {
    private final IInventory inventory;
    private final int slot;

    public FInvItemLink(IInventory inventory, int slot) {
        this.inventory = inventory;
        this.slot = slot;
    }


    @Override
    public ItemStack getItem() {
        return inventory.getStackInSlot(slot);
    }


    @Override
    public void setItem(ItemStack item) {
        inventory.setInventorySlotContents(slot, item);
    }
}
