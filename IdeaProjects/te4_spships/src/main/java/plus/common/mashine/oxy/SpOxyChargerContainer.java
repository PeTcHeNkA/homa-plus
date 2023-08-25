package plus.common.mashine.oxy;

import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import plus.common.etc.ESlot;
import plus.common.etc.FESlot;
import plus.common.etc.VSlot;
import plus.common.mashine.inv.PlayerExtendedBlockContainer;
import plus.common.mashine.item.ItemOxyBallon;


public class SpOxyChargerContainer extends PlayerExtendedBlockContainer {
    private int progress = 0;

    public SpOxyChargerContainer(IInventory playerInv, IInventory oxyInv) {
        super(oxyInv);

        addSlotToContainer(new ESlot(oxyInv, 0, 44,    18, this::onEditModule));
        addSlotToContainer(new ESlot(oxyInv, 1, 44+18, 18, this::onEditModule));
        addSlotToContainer(new ESlot(oxyInv, 1, 44+36, 18, this::onEditModule));
        addSlotToContainer(new ESlot(oxyInv, 1, 44+54, 18, this::onEditModule));
        addSlotToContainer(new ESlot(oxyInv, 1, 44+72, 18, this::onEditModule));


        addSlotToContainer(new FESlot(oxyInv, 2, 27,    47,
                SpOxyChargerContainer::isOxyBallon,
                this::onContentChanged));
        addSlotToContainer(new FESlot(oxyInv, 3, 27+18, 47,
                SpOxyChargerContainer::isOxyBallon,
                this::onContentChanged)
        );

        addSlotToContainer(new VSlot(oxyInv, 4, 106,    47));
        addSlotToContainer(new VSlot(oxyInv, 5, 106+28, 47));

        addPlayerSlots(playerInv);

    }


    @Override
    public void addListener(IContainerListener listener) {
        super.addListener(listener);
        listener.sendWindowProperty(this, 0, inventory.getField(0));
    }


    private static boolean isOxyBallon(ItemStack item) {
        return ItemOxyBallon.isOxyBallon(item) && item.getItemDamage()>1;
    }


    private void onContentChanged() {
        syncFirstField(inventory.getField(0));
    }


    private void onEditModule() {

    }


    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        int curPrg = inventory.getField(0);
        if(progress != curPrg) syncFirstField(progress = curPrg);
    }
}