package plus.common.etc;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import plus.common.mashine.item.recept.IItemValidator;


public class FESlot extends ESlot{
    private final IItemValidator.Single validItem;
    public FESlot(IInventory inventoryIn, int index, int xPosition, int yPosition, IItemValidator.Single validator, Runnable onChanged) {
        super(inventoryIn, index, xPosition, yPosition, onChanged);
        this.validItem = validator;
    }


    public boolean isItemValid(ItemStack stack) {
        return validItem.isEqual(stack);
    }
}