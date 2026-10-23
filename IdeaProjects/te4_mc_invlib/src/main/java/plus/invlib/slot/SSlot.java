package plus.invlib.slot;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

public class SSlot extends Slot {
    public final int defXpos;
    public final int defYpos;

    public SSlot(IInventory inventoryIn, int index, int xPosition, int yPosition) {
        super(inventoryIn, index, xPosition, yPosition);
        defXpos = xPosition;
        defYpos = yPosition;
    }
//
//        @Nullable
//        @SideOnly(Side.CLIENT)
//        public String getSlotTexture(){
//            return Te4InvContainer.RES_LOC;
//        }
}
