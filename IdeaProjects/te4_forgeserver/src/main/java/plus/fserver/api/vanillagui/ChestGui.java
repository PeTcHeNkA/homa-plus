package plus.fserver.api.vanillagui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IInteractionObject;
import plus.fserver.utls.collections.FixedItemList;


public class ChestGui implements IInventory, IInteractionObject {
    private final FixedItemList items;
    private String customName;

    public ChestGui(int rows) {
        this.items = new FixedItemList(rows*9);
    }


    public void fillOf(ChestGuiTemplate template){
        FixedItemList items = this.items;
        for (int i = 0, s = template.size(); i < s; i++){
            items.set(template.getSlot(i), template.getStackCopy(i));
        }
    }


    public void setCustomName(String name){
        this.customName = name;
    }


    @Override
    public int getSizeInventory() {
        return items.size();
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
    public boolean isUsableByPlayer(EntityPlayer player) {
        return true;
    }

    @Override
    public void openInventory(EntityPlayer player) {}


    @Override
    public void closeInventory(EntityPlayer player) {}


    @Override
    public boolean isItemValidForSlot(int i, ItemStack stack) {
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
    public Container createContainer(InventoryPlayer invPlayer, EntityPlayer player) {
        return new VrContainer(invPlayer, this, player);
    }


    @Override
    public String getGuiID() {
        return "minecraft:container";
    }


    public String getName() {
        return this.hasCustomName() ? this.customName : "container.chest";
    }


    @Override
    public boolean hasCustomName() {
        return customName != null;
    }


    @Override
    public ITextComponent getDisplayName() {
        return this.hasCustomName() ? new TextComponentString(this.getName()) : new TextComponentTranslation(this.getName());
    }


    public ItemStack onClick(VrContainer container, int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player){
        return null;
    }


    public class VrContainer extends ContainerChest {
        private final ChestGui gui;
        public VrContainer(IInventory plInv, ChestGui inv, EntityPlayer player) {
            super(plInv, inv, player);
            this.gui = inv;
        }

        public ChestGui gui(){
            return gui;
        }


        @Override
        public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player) {
            ItemStack result = onClick(this, slotId, dragType, clickTypeIn, player);
            if(result != null)return result;
            return super.slotClick(slotId, dragType, clickTypeIn, player);
        }


        @Override
        public boolean canInteractWith(EntityPlayer playerIn) {
            return true;
        }
    }
}