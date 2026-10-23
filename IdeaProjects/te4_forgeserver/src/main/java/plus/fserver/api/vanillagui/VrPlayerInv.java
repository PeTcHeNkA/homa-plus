package plus.fserver.api.vanillagui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import plus.fserver.api.IPlayerDataModel;
import plus.fserver.utls.collections.FixedItemList;
import plus.tson.TsonFunc;


public class VrPlayerInv implements IInventory {
    private final FixedItemList items = new FixedItemList(41);

    public VrPlayerInv(ItemStack[] stacks, byte[] slots){
        FixedItemList items = this.items;
        for (int i = 0; i < stacks.length; i++){
            ItemStack stack = stacks[i];
            if(stack != null && !stack.isEmpty()) {
                items.set(slots[i] & 255, stack.copy());
            }
        }
    }


    public void fill(IPlayerDataModel model, TsonFunc[] funcs, byte[] slots) {
        EntityPlayerMP player = model.getSelfPlayer();
        for (int i = 0; i < funcs.length; i++) {
            ItemStack stack = (ItemStack) funcs[i].call(player);
            if (stack != null) {
                this.setInventorySlotContents(slots[i] & 255, stack);
            }
        }
    }


    @Override
    public int getSizeInventory() {
        return 41;
    }


    @Override
    public boolean isEmpty() {
        return items.isAllEmpty();
    }

    @Override
    public ItemStack getStackInSlot(int i) {
        return items.get(i);
    }


    @Override
    public ItemStack decrStackSize(int index, int amount) {
        return FixedItemList.getAndSplit(items, index, amount);
    }


    @Override
    public ItemStack removeStackFromSlot(int i) {
        ItemStack stack = this.items.get(i);
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        } else {
            this.items.fSet(i, ItemStack.EMPTY);
            return stack;
        }
    }


    @Override
    public void setInventorySlotContents(int i, ItemStack stack) {
        items.set(i, stack);
    }


    @Override
    public int getInventoryStackLimit() {
        return 64;
    }


    @Override
    public void markDirty() {}


    @Override
    public boolean isUsableByPlayer(EntityPlayer entityPlayer) {
        return true;
    }


    @Override
    public void openInventory(EntityPlayer entityPlayer) {

    }

    @Override
    public void closeInventory(EntityPlayer entityPlayer) {

    }


    @Override
    public boolean isItemValidForSlot(int i, ItemStack itemStack) {
        return false;
    }


    @Override
    public int getField(int i) {
        return 0;
    }


    @Override
    public void setField(int i, int i1) {}


    @Override
    public int getFieldCount() {
        return 0;
    }


    @Override
    public void clear() {
        items.clear();
    }


    @Override
    public String getName() {
        return "container.inventory";
    }


    @Override
    public boolean hasCustomName() {
        return false;
    }


    @Override
    public ITextComponent getDisplayName() {
        return this.hasCustomName() ? new TextComponentString(this.getName()) : new TextComponentTranslation(this.getName());
    }
}