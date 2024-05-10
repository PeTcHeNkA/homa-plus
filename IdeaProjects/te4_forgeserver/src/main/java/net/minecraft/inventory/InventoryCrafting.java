package net.minecraft.inventory;

import net.minecraft.client.util.RecipeItemHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import plus.fserver.utls.collections.FixedItemList;


public class InventoryCrafting implements IInventory {
    private final FixedItemList stackList;
    private final int inventoryWidth;
    private final int inventoryHeight;
    private final Container eventHandler;

    public InventoryCrafting(Container eventHandlerIn, int width, int height) {
        this.stackList = new FixedItemList(width * height);
        this.eventHandler = eventHandlerIn;
        this.inventoryWidth = width;
        this.inventoryHeight = height;
    }


    @Override
    public int getSizeInventory() {
        return this.stackList.size();
    }


    @Override
    public boolean isEmpty() {
        for (ItemStack itemstack : this.stackList) {
            if (!itemstack.isEmpty()) {
                return false;
            }
        }
        return true;
    }


    @Override
    public ItemStack getStackInSlot(int index) {
        return index >= this.getSizeInventory() ? ItemStack.EMPTY : this.stackList.get(index);
    }


    public ItemStack getStackInRowAndColumn(int row, int column) {
        return row >= 0 && row < this.inventoryWidth && column >= 0 && column <= this.inventoryHeight ? this.getStackInSlot(row + column * this.inventoryWidth) : ItemStack.EMPTY;
    }


    @Override
    public String getName() {
        return "container.crafting";
    }


    @Override
    public boolean hasCustomName() {
        return false;
    }


    public ITextComponent getDisplayName() {
        return this.hasCustomName() ? new TextComponentString(this.getName()) : new TextComponentTranslation(this.getName());
    }


    public ItemStack removeStackFromSlot(int index) {
        return FixedItemList.getAndRemove(this.stackList, index);
    }


    @Override
    public ItemStack decrStackSize(int index, int count) {
        ItemStack itemstack = FixedItemList.getAndSplit(this.stackList, index, count);
        if (!itemstack.isEmpty()) {
            this.eventHandler.onCraftMatrixChanged(this);
        }
        return itemstack;
    }


    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        this.stackList.fSet(index, stack);
        this.eventHandler.onCraftMatrixChanged(this);
    }


    @Override
    public int getInventoryStackLimit() {
        return 64;
    }


    @Override
    public void markDirty() {}


    @Override
    public boolean isUsableByPlayer(EntityPlayer player)
    {
        return true;
    }


    @Override
    public void openInventory(EntityPlayer player) {}


    @Override
    public void closeInventory(EntityPlayer player) {}


    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return true;
    }


    @Override
    public int getField(int id) {
        return 0;
    }


    @Override
    public void setField(int id, int value) {}


    @Override
    public int getFieldCount() {
        return 0;
    }


    @Override
    public void clear() {
        this.stackList.clear();
    }


    public int getHeight() {
        return this.inventoryHeight;
    }


    public int getWidth() {
        return this.inventoryWidth;
    }


    public void fillStackedContents(RecipeItemHelper helper) {
        for (ItemStack itemstack : this.stackList) {
            helper.accountStack(itemstack);
        }
    }
}