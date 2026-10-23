package net.minecraft.inventory;

import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;


public class InventoryCraftResult implements IInventory {
    private ItemStack result = ItemStack.EMPTY;
    private IRecipe recipeUsed;

    @Override
    public int getSizeInventory() {
        return 1;
    }


    @Override
    public boolean isEmpty() {
        return result.isEmpty();
    }


    @Override
    public ItemStack getStackInSlot(int index) {
        return result;
    }


    @Override
    public String getName() {
        return "Result";
    }


    @Override
    public boolean hasCustomName() {
        return false;
    }


    @Override
    public ITextComponent getDisplayName() {
        return this.hasCustomName() ? new TextComponentString(this.getName()) : new TextComponentTranslation(this.getName());
    }


    @Override
    public ItemStack decrStackSize(int index, int count) {
        ItemStack result = this.result;
        this.result = ItemStack.EMPTY;
        return result;
    }


    @Override
    public ItemStack removeStackFromSlot(int index) {
        ItemStack result = this.result;
        this.result = ItemStack.EMPTY;
        return result;
    }


    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        result = stack;
    }


    @Override
    public int getInventoryStackLimit() {
        return 64;
    }


    @Override
    public void markDirty() {}


    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
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
        result = ItemStack.EMPTY;
    }


    public void setRecipeUsed(@Nullable IRecipe recipe) {
        this.recipeUsed = recipe;
    }


    @Nullable
    public IRecipe getRecipeUsed() {
        return this.recipeUsed;
    }
}