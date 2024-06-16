package plus.common.mashine.capicator;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import plus.common.mashine.inv.PlayerExtendedBlockContainer;
import plus.common.mashine.item.IItemCapacitor;
import plus.common.mashine.item.ItemCapacitor;
import plus.common.mashine.item.ItemMssCapacitor;


public class SpCapacitorContainer extends PlayerExtendedBlockContainer {

    public SpCapacitorContainer(IInventory playerInv, IInventory spCapInv) {
        super(spCapInv);

        addSlotToContainer(new SlotCapacitor(spCapInv, 0, 31, 21));
        addSlotToContainer(new SlotCapacitor(spCapInv, 1, 31, 50));
        addSlotToContainer(new SlotCapacitor(spCapInv, 2, 56, 21));
        addSlotToContainer(new SlotCapacitor(spCapInv, 3, 56, 50));

        addSlotToContainer(new SlotMssCapacitor(spCapInv, 4, 125, 12));
        addSlotToContainer(new SlotMssCapacitor(spCapInv, 5, 125, 59));

        addPlayerSlots(playerInv);
    }


    private static class SlotCapacitor extends Slot {
        public SlotCapacitor(IInventory inv, int i, int x, int y) {
            super(inv, i, x, y);
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            return IItemCapacitor.isCapacitor(stack);
        }
    }


    private static class SlotMssCapacitor extends Slot {
        public SlotMssCapacitor(IInventory inv, int i, int x, int y) {
            super(inv, i, x, y);
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            Item i = stack.getItem();
            return i instanceof ItemCapacitor && !(i instanceof ItemMssCapacitor);
        }
    }
}