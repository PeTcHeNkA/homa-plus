package plus.common.etc;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

public class ESlot extends Slot {
    private final Runnable onChanged;
    public ESlot(IInventory inventoryIn, int index, int xPosition, int yPosition, Runnable onChanged) {
        super(inventoryIn, index, xPosition, yPosition);
        this.onChanged = onChanged;
    }


    public void onSlotChanged() {
        super.onSlotChanged();
        onChanged.run();
    }
}