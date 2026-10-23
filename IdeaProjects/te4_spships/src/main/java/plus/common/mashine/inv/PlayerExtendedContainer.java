package plus.common.mashine.inv;

import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;


public abstract class PlayerExtendedContainer extends Container {
    protected void addPlayerSlots(IInventory plInv) {
        for (int k = 0; k < 3; ++k) {
            for (int i1 = 0; i1 < 9; ++i1) {
                this.addSlotToContainer(new Slot(plInv, i1 + k * 9 + 9, 8 + i1 * 18,
                        84 + k * 18));
            }
        }
        for (int l = 0; l < 9; ++l) {
            this.addSlotToContainer(new Slot(plInv, l, 8 + l * 18, 142));
        }
    }
}