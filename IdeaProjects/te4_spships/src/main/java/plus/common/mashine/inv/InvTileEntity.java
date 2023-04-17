package plus.common.mashine.inv;

import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityLockableLoot;
import net.minecraft.util.NonNullList;


public abstract class InvTileEntity extends TileEntityLockableLoot {
    public NonNullList<ItemStack> stacks;

    public InvTileEntity(int size){
        stacks = NonNullList.withSize(size, ItemStack.EMPTY);
    }


    @Override
    protected NonNullList<ItemStack> getItems() {
        return stacks;
    }


    @Override
    public int getSizeInventory() {
        return stacks.size();
    }


    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.stacks = NonNullList.withSize(getSizeInventory(), ItemStack.EMPTY);
        ItemStackHelper.loadAllItems(compound, this.stacks);
        if (compound.hasKey("CustomName", 8)) {
            this.customName = compound.getString("CustomName");
        }
    }


    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        ItemStackHelper.saveAllItems(compound, this.stacks, false);
        if (this.hasCustomName()) {
            compound.setString("CustomName", this.customName);
        }
        return super.writeToNBT(compound);
    }


    @Override
    public boolean isEmpty() {
        for (ItemStack itemstack : this.stacks) {
            if (!itemstack.isEmpty()) {
                return false;
            }
        }
        return true;
    }


    @Override
    public ItemStack getStackInSlot(int index) {
        return stacks.get(index);
    }


    @Override
    public int getInventoryStackLimit() {
        return 64;
    }


    @Override
    public String getName() {
        return hasCustomName()?customName:"";
    }
}