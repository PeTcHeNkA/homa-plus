package plus.common.mashine.react;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import plus.common.etc.FESlot;
import plus.common.mashine.inv.PlayerExtendedBlockContainer;
import plus.common.mashine.item.ReactComponent;


public class SpReactContainer extends PlayerExtendedBlockContainer {
    public SpReactContainer(IInventory playerInv, IInventory spReactInv){
        super(spReactInv);

        addESlot(spReactInv, 0, 19,  14);
        addESlot(spReactInv, 1, 19,  54);

        addESlot(spReactInv, 2, 44,  14);
        addESlot(spReactInv, 3, 44,  54);

        addESlot(spReactInv, 4, 116, 14);
        addESlot(spReactInv, 5, 116, 54);

        addESlot(spReactInv, 6, 141, 14);
        addESlot(spReactInv, 7, 141, 54);

        addSlotToContainer(
                new FESlot(spReactInv, 8, 80, 34,
                ReactComponent::isReactCore, this::onContentChanged)
        );

        addPlayerSlots(playerInv);
    }


    private void addESlot(IInventory spReactInv, int i, int x, int y){
        addSlotToContainer(new FESlot(spReactInv, i, x, y, ReactComponent::isReactNoCore, this::onContentChanged));
    }


    private void onContentChanged() {
    }
}