package plus.common.mashine.inv;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;


public class CraftViewInv implements IInventory {
    protected final IInventory parent;
    protected ItemStack item = ItemStack.EMPTY;

    public CraftViewInv(IInventory parent) {
        this.parent = parent;
    }


    @Override
    public int getSizeInventory() {
        return 1;
    }


    @Override
    public boolean isEmpty() {
        return item.isEmpty();
    }


    @Override
    public ItemStack getStackInSlot(int index) {
        return item;
    }


    @Override
    public ItemStack decrStackSize(int index, int count) {
        return decrStackSize(item, count);
    }


    protected ItemStack decrStackSize(ItemStack item, int count){
        return !(item).isEmpty() && count > 0 ? (item).splitStack(count) : ItemStack.EMPTY;
    }


    @Override
    public ItemStack removeStackFromSlot(int index) {
        ItemStack prev = item;
        item = ItemStack.EMPTY;
        return prev;
    }


    public void setItem(ItemStack item){
        this.item = item;
    }


    public ItemStack getItem(){
        return item;
    }


    @Override
    public void setInventorySlotContents(int index, ItemStack item) {
        this.item = item;
    }


    @Override
    public int getInventoryStackLimit() {
        return 64;
    }


    @Override
    public void markDirty() {

    }


    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return parent.isUsableByPlayer(player);
    }


    @Override
    public void openInventory(EntityPlayer player) {

    }

    @Override
    public void closeInventory(EntityPlayer player) {

    }


    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return true;
    }


    @Override
    public int getField(int id) {
        return 0;
    }


    @Override
    public void setField(int id, int value) {

    }

    @Override
    public int getFieldCount() {
        return 0;
    }


    @Override
    public void clear() {
        item = ItemStack.EMPTY;
    }


    @Override
    public String getName() {
        return "singlecraft";
    }


    @Override
    public boolean hasCustomName() {
        return false;
    }


    @Override
    public ITextComponent getDisplayName() {
        return null;
    }
}