package net.minecraft.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;


public class ContainerHopper extends Container {
    private final IInventory hopperInventory;

    public ContainerHopper(InventoryPlayer plInv, IInventory hopperInv, EntityPlayer player) {
        this.hopperInventory = hopperInv;
        hopperInv.openInventory(player);

        int i;
        for(i = 0; i < hopperInv.getSizeInventory(); ++i) {
            this.addSlotToContainer(new Slot(hopperInv, i, 44 + i * 18, 20));
        }

        for(i = 0; i < 3; ++i) {
            for(int j = 0; j < 9; ++j) {
                this.addSlotToContainer(new Slot(plInv, j + i * 9 + 9, 8 + j * 18, i * 18 + 51));
            }
        }

        for(i = 0; i < 9; ++i) {
            this.addSlotToContainer(new Slot(plInv, i, 8 + i * 18, 109));
        }
    }


    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return this.hopperInventory.isUsableByPlayer(player);
    }


    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int i) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(i);
        if (slot != null && slot.getHasStack()) {
            IInventory hopperInventory = this.hopperInventory;

            ItemStack sStack = slot.getStack();
            stack = sStack.copy();
            if (i < hopperInventory.getSizeInventory()) {
                if (!this.mergeItemStack(sStack, hopperInventory.getSizeInventory(), this.inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.mergeItemStack(sStack, 0, hopperInventory.getSizeInventory(), false)) {
                return ItemStack.EMPTY;
            }

            if (sStack.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }
        }
        return stack;
    }


    @Override
    public void onContainerClosed(EntityPlayer player) {
        super.onContainerClosed(player);
        this.hopperInventory.closeInventory(player);
    }


    public IInventory getHopperInventory(){
        return hopperInventory;
    }
}