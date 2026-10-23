package plus.common.mashine.inv;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;


public class DoubleCraftViewInv extends CraftViewInv{
    protected ItemStack item2 = ItemStack.EMPTY;

    public DoubleCraftViewInv(IInventory parent) {
        super(parent);
    }


    @Override
    public int getSizeInventory() {
        return 2;
    }


    @Override
    public boolean isEmpty() {
        return item.isEmpty() && item2.isEmpty();
    }


    @Override
    public ItemStack getStackInSlot(int index) {
        return index==0?item:item2;
    }


    public ItemStack getSecondItem(){
        return item2;
    }


    public void setSecondItem(ItemStack item){
        this.item2 = item;
    }


    @Override
    public ItemStack decrStackSize(int index, int count) {
        return decrStackSize(getStackInSlot(index), count);
    }


    @Override
    public ItemStack removeStackFromSlot(int index) {
        if(index==0)return super.removeStackFromSlot(index);
        ItemStack prev = item2;
        item2 = ItemStack.EMPTY;
        return prev;
    }


    @Override
    public void setInventorySlotContents(int index, ItemStack item) {
        if(index==0) {
            this.item = item;
        } else {
            this.item2 = item;
        }
    }


    @Override
    public String getName() {
        return "dublecraft";
    }
}