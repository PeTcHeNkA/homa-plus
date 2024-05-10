package plus.fserver.utls;

import net.minecraft.client.util.RecipeItemHelper;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;


public class EmptyInvCraft extends InventoryCrafting {
    public static EmptyInvCraft INST = new EmptyInvCraft();
    private EmptyInvCraft() {
        super(null, 1,1);
    }


    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        return;
    }


    @Override
    public int getSizeInventory() {
        return 1;
    }


    @Override
    public boolean isEmpty() {
        return true;
    }


    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return false;
    }


    @Override
    public void fillStackedContents(RecipeItemHelper helper) {}


    @Override
    public void clear() {}
}