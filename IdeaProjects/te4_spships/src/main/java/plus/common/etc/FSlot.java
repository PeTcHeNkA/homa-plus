package plus.common.etc;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import plus.common.mashine.item.recept.IItemValidator;


public class FSlot extends Slot {
    private final IItemValidator.Single validItem;
    public FSlot(IInventory inventoryIn, int index, int xPosition, int yPosition, IItemValidator.Single validItem) {
        super(inventoryIn, index, xPosition, yPosition);
        this.validItem = validItem;
    }

    public boolean isItemValid(ItemStack stack) {
        return validItem.isEqual(stack);
    }
}
