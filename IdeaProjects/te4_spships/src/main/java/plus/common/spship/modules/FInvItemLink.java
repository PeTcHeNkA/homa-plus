package plus.common.spship.modules;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import plus.common.mashine.inv.IItemLink;


public final class FInvItemLink implements IItemLink {
    private final IInventory inventory;
    private final int slot;
    private ItemStack item;

    public FInvItemLink(IInventory inventory, int slot, ItemStack item) {
        this.inventory = inventory;
        this.slot = slot;
        this.item = item;
    }


    @Override
    public ItemStack getItem() {
        return item;
    }


    @Override
    public void setItem(ItemStack item) {
        this.item = item;
        inventory.setInventorySlotContents(slot, item);
    }
}
