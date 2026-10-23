package plus.common.mashine.press;

import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import plus.common.etc.ESlot;
import plus.common.etc.VSlot;
import plus.common.mashine.inv.PlayerExtendedBlockContainer;


public class SpPressContainer extends PlayerExtendedBlockContainer {
    private int progress = 0;

    public SpPressContainer(IInventory playerInv, IInventory spPressInv){
        super(spPressInv);

        addSlotToContainer(new ESlot(spPressInv, 0, 38,    29,   this::onCmsg));
        addSlotToContainer(new ESlot(spPressInv, 1, 38+18, 29,   this::onCmsg));
        addSlotToContainer(new ESlot(spPressInv, 2, 38,    29+18, this::onCmsg));
        addSlotToContainer(new ESlot(spPressInv, 3, 38+18, 29+18, this::onCmsg));

        addSlotToContainer(new VSlot(spPressInv, 4, 118,    38));

        addPlayerSlots(playerInv);
    }


    @Override
    public void addListener(IContainerListener listener) {
        super.addListener(listener);
        listener.sendWindowProperty(this, 0, inventory.getField(0));
    }


    private void onCmsg() {
        progress = 0;
        inventory.setField(0,0);
        syncFirstField(0);
    }


    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        int curPrg = inventory.getField(0);
        if(progress != curPrg) syncFirstField(progress = curPrg);
    }
}