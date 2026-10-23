package plus.common.mashine.oxy;

import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import plus.common.etc.VSlot;
import plus.common.mashine.inv.PlayerExtendedBlockContainer;
import plus.common.mashine.item.ItemCanister;
import plus.common.mashine.item.ItemOxyFilterPlate;


public class SpStationOxyChargerContainer extends PlayerExtendedBlockContainer {
    public static final int MAX_LIQUID = 10_000;
    public static final int MAX_AIR    = 40_000;
    private int prevLiquidLvl = 0, prevAirLvl = 0, progress = 0;

    public SpStationOxyChargerContainer(IInventory playerInv, IInventory oxyInv) {
        super(oxyInv);

        addSlotToContainer(new SlotLiquid(oxyInv, 0, 12, 11));
        addSlotToContainer(new SlotLiquid(oxyInv, 1, 12, 11 + 49));

        addSlotToContainer(new SlotFilter(oxyInv, 2, 57     , 7));
        addSlotToContainer(new SlotFilter(oxyInv, 3, 57 + 18, 7));
        addSlotToContainer(new SlotFilter(oxyInv, 4, 57 + 36, 7));
        addSlotToContainer(new SlotFilter(oxyInv, 5, 57 + 54, 7));

        addSlotToContainer(new SpOxyChargerContainer.OxyBallonSlot(
                oxyInv, 6, 36 + 00, 53, this::onContentChanged));

        addSlotToContainer(new SpOxyChargerContainer.OxyBallonSlot(
                oxyInv, 7, 36 + 18, 53, this::onContentChanged));

        addSlotToContainer(new VSlot(oxyInv, 8,  115,    53));
        addSlotToContainer(new VSlot(oxyInv, 9,  115+28, 53));

        addPlayerSlots(playerInv);
    }

    private static class SlotLiquid extends Slot {
        public SlotLiquid(IInventory inv, int i, int x, int y) {
            super(inv, i, x, y);
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            return isWaterField(stack);
        }
    }


    public static boolean isWaterField(ItemStack stack){
        Item item = stack.getItem();
        return item == Items.WATER_BUCKET || item == ItemCanister.WATER_CANISTER;
    }


    private static class SlotFilter extends Slot {
        public SlotFilter(IInventory inv, int i, int x, int y) {
            super(inv, i, x, y);
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            return stack.getItem() instanceof ItemOxyFilterPlate;
        }
    }


    public void onContentChanged() {
    }


    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        int curPrg = inventory.getField(0);
        if (progress != curPrg) {
            syncFirstField(progress = curPrg);
        }
        int curLvl = inventory.getField(1);
        if (prevLiquidLvl != curLvl) {
            syncField(1, prevLiquidLvl = curLvl);
        }
        int curAir = inventory.getField(2);
        if (prevAirLvl != curAir) {
            syncField(2, prevAirLvl = curAir);
        }
    }
}