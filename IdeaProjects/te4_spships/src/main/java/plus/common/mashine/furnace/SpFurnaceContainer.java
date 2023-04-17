package plus.common.mashine.furnace;

import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.common.mashine.inv.PlayerExtendedBlockContainer;
import plus.common.etc.ESlot;
import plus.common.etc.VSlot;


public class SpFurnaceContainer extends PlayerExtendedBlockContainer {
    private int progress = 0;

    public SpFurnaceContainer(IInventory playerInv, IInventory spFurnInv){
        super(spFurnInv);

        addSlotToContainer(new ESlot(spFurnInv, 0, 38,    18, this::onEditModule));
        addSlotToContainer(new ESlot(spFurnInv, 1, 38+18, 18, this::onEditModule));

        addSlotToContainer(new ESlot(spFurnInv, 2, 27,    47, this::onContentChanged));
        addSlotToContainer(new ESlot(spFurnInv, 3, 27+18, 47, this::onContentChanged));

        addSlotToContainer(new VSlot(spFurnInv, 4, 106,    47));
        addSlotToContainer(new VSlot(spFurnInv, 5, 106+28, 47));

        addPlayerSlots(playerInv);
    }


    private void onEditModule() {
        progress = 0;
        inventory.setField(0,0);
        syncFirstField(0);
    }


    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        int curPrg = inventory.getField(0);
        if(progress != curPrg){
            progress = curPrg;
            syncFirstField(progress);
        }
    }


    @Override
    public void addListener(IContainerListener listener) {
        super.addListener(listener);
        listener.sendWindowProperty(this, 0, inventory.getField(0));
    }


    private void test(){
        //BlockFurnace
    }


    private void onContentChanged() {
        //inventory.getField()
    }
}