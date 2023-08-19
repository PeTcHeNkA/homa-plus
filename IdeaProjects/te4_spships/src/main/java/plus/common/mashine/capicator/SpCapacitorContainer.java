package plus.common.mashine.capicator;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import plus.common.etc.FSlot;
import plus.common.mashine.inv.PlayerExtendedBlockContainer;
import plus.common.mashine.item.ItemCapacitor;
import plus.common.mashine.item.ItemMssCapacitor;


public class SpCapacitorContainer extends PlayerExtendedBlockContainer {

    public SpCapacitorContainer(IInventory playerInv, IInventory spCapInv) {
        super(spCapInv);

        addSlotToContainer(new FSlot(spCapInv, 0, 31, 21, ItemCapacitor::isCapacitor));
        addSlotToContainer(new FSlot(spCapInv, 1, 31, 50, ItemCapacitor::isCapacitor));
        addSlotToContainer(new FSlot(spCapInv, 2, 56, 21, ItemCapacitor::isCapacitor));
        addSlotToContainer(new FSlot(spCapInv, 3, 56, 50, ItemCapacitor::isCapacitor));

        addSlotToContainer(new FSlot(spCapInv, 4, 125, 12, this::isNoMassive));
        addSlotToContainer(new FSlot(spCapInv, 5, 125, 59, this::isNoMassive));

        addPlayerSlots(playerInv);
    }


    private boolean isNoMassive(ItemStack itemStack){
        Item i = itemStack.getItem();
        return i instanceof ItemCapacitor && !(i instanceof ItemMssCapacitor);
    }
}