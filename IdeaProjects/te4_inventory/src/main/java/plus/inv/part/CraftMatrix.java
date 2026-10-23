package plus.inv.part;

import net.minecraft.inventory.Slot;
import plus.inv.AInventoryClientRenderer;
import plus.invlib.slot.SSlot;
import plus.invlib.render.Updateable;

import java.util.List;


public class CraftMatrix extends AbcPart implements Updateable {
    public CraftMatrix(AInventoryClientRenderer invrenderer) {
        super(invrenderer);
    }

    private int horizontalShift = 200;
    private int verticalShift = 50;


    @Override
    public void update() {
        List<Slot> slots = invrenderer.inventorySlots.inventorySlots;
        for(int i=15;i<20;i+=1) {
            SSlot slot = (SSlot) slots.get(i);

            slot.xPos = horizontalShift + slot.defXpos;
            slot.yPos = verticalShift + slot.defYpos;
        }
    }
}