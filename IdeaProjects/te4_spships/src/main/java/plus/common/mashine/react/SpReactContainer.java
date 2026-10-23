package plus.common.mashine.react;

import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import plus.common.mashine.inv.PlayerExtendedBlockContainer;
import plus.common.mashine.item.ReactComponent;


public class SpReactContainer extends PlayerExtendedBlockContainer {
    public SpReactContainer(IInventory playerInv, IInventory spReactInv){
        super(spReactInv);

        addSlotToContainer(new ReactCompSlot(spReactInv, 0, 19,  14));
        addSlotToContainer(new ReactCompSlot(spReactInv, 1, 19,  54));

        addSlotToContainer(new ReactCompSlot(spReactInv, 2, 44,  14));
        addSlotToContainer(new ReactCompSlot(spReactInv, 3, 44,  54));

        addSlotToContainer(new ReactCompSlot(spReactInv, 4, 116, 14));
        addSlotToContainer(new ReactCompSlot(spReactInv, 5, 116, 54));

        addSlotToContainer(new ReactCompSlot(spReactInv, 6, 141, 14));
        addSlotToContainer(new ReactCompSlot(spReactInv, 7, 141, 54));

        addSlotToContainer(new ReactCoreSlot(spReactInv, 8, 80, 34));

        addPlayerSlots(playerInv);
    }


    @Override
    public void addListener(IContainerListener listener) {
        super.addListener(listener);
        listener.sendWindowProperty(this, 0, inventory.getField(0));
        listener.sendWindowProperty(this, 1, inventory.getField(1));
    }


    private static class ReactCompSlot extends Slot {
        public ReactCompSlot(IInventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            return ReactComponent.isReactNoCore(stack);
        }
    }


    private static class ReactCoreSlot extends ReactCompSlot {
        public ReactCoreSlot(IInventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            return ReactComponent.isReactCore(stack);
        }
    }


    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        syncAllFields();
    }
}