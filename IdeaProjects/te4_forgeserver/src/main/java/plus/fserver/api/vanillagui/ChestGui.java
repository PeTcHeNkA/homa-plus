package plus.fserver.api.vanillagui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IInteractionObject;
import plus.fserver.api.IPlayerDataModel;
import plus.fserver.utls.collections.FixedItemList;


public class ChestGui implements IInventory, IInteractionObject {
    private final FixedItemList items;
    private String customName;
    private Object[] vrPl;

    public ChestGui(int size) {
        this.items = new FixedItemList(size);
    }


    protected void setVrPl(Object[] vrPl){
        this.vrPl = vrPl;
    }


    public void open(EntityPlayer player){
        player.displayGUIChest(this);
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
    public void closeInventory(EntityPlayer player) {
        if(vrPl != null) {
            EntityPlayerMP mp = ((EntityPlayerMP) player);
            mp.sendAllContents(mp.inventoryContainer, mp.inventoryContainer.getInventory());
        }
    }


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
        IInventory inv = invPlayer;
        Object[] vrPl = this.vrPl;
        if(vrPl != null){
            VrPlayerInv inv1 = new VrPlayerInv((ItemStack[]) vrPl[0], (byte[]) vrPl[1]);
            onCreateVrPl(inv1, (IPlayerDataModel)player);
            inv = inv1;
        }

        if(items.size() == 5)return new VrContainerHopper(inv, this, player);
        return new VrContainerChest(inv, this, player);
    }


    protected void onCreateVrPl(VrPlayerInv inv, IPlayerDataModel model){}


    @Override
    public String getGuiID() {
        if(items.size() == 5)return "minecraft:hopper";
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


    public ItemStack onClick(Container container, int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player){
        return null;
    }


    private class VrContainerHopper extends ContainerHopper{
        public VrContainerHopper(IInventory plInv, IInventory hopperInv, EntityPlayer player) {
            super((InventoryPlayer) plInv, hopperInv, player);
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


    private class VrContainerChest extends ContainerChest {
        public VrContainerChest(IInventory plInv, ChestGui inv, EntityPlayer player) {
            super(plInv, inv, player);
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


    public static ItemStack canselAll(Container cont, Integer slotId, Integer dType, ClickType cType, EntityPlayer player){
        if(cType == ClickType.PICKUP || cType == ClickType.PICKUP_ALL)
            return player.inventory.getItemStack();
        if(slotId < 0)return null;
        if(slotId >= cont.inventorySlots.size())return ItemStack.EMPTY;
        return cont.getSlot(slotId).getStack();
    }


    public static ItemStack canselAllSmart(Container cont, Integer slotId, Integer dType, ClickType cType, EntityPlayer player){
        if(slotId < 0)return null;
        if(slotId >= cont.inventorySlots.size())return null;

        Slot slot = cont.getSlot(slotId);
        IInventory other = null;

        if(cont instanceof ContainerChest) {
            ContainerChest cs = (ContainerChest) cont;
            other = cs.getLowerChestInventory();
        } else if (cont instanceof ContainerHopper) {
            ContainerHopper hopper = (ContainerHopper) cont;
            other = hopper.getHopperInventory();
        }

        if (other == null || slot.inventory == other) {
            if (cType == ClickType.PICKUP || cType == ClickType.PICKUP_ALL) {
                return player.inventory.getItemStack();
            }
            return slot.getStack();
        }
        return null;
    }
}