package plus.common.mashine.press;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import plus.common.mashine.inv.EnergyDeviceTileEntity;
import plus.common.mashine.inv.SimpleCraftMap;
import plus.common.mashine.item.BasicItem;


public class SpPressTileEntity extends EnergyDeviceTileEntity {
    private static final int[] SLOTS_INPUT = new int[] {0, 1, 2, 3};
    private static final int[] SLOTS_OUTPUT = new int[] {4};
    private static final SimpleCraftMap pressCrafts = new SimpleCraftMap() {
        @Override
        public void onInit() {
            add(Blocks.SAND, Blocks.SANDSTONE);

            add(Items.IRON_INGOT, BasicItem.PLATE_IRON, 2);
            add(Items.GOLD_INGOT, BasicItem.PLATE_GOLD, 2);

            add(BasicItem.ALUMINE_INGOT, BasicItem.PLATE_ALUMINE, 2);
            add(BasicItem.COPPER_INGOT,  BasicItem.PLATE_COPPER,  2);
            add(BasicItem.MEFRIL_INGOT,  BasicItem.PLATE_MEFRIL,  2);
        }
    };


    public SpPressTileEntity() {
        super(5);
    }


    @Override
    public int[] getSlotsForFace(EnumFacing side) {
        if(side == EnumFacing.DOWN){
            return SLOTS_OUTPUT;
        } else {
            return SLOTS_INPUT;
        }
    }


    @Override
    public void update(){
        if(isValidItems()){
            if(!useEnergyIfCan())return;
            if(onSuccessProgress(100)){
                completeCraft();
            }
        } else {
            resetProgress();
        }
    }


    @Override
    public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn) {
        return new SpPressContainer(playerInventory, this);
    }


    @Override
    public String getGuiID() {
        return "minecraft:sp_press_gui";
    }


    private void completeCraft(){
        SimpleCraftMap.ItemGen result = pressCrafts.get(stacks.get(0).getItem());
        takeOneItem(0);
        takeOneItem(1);
        takeOneItem(2);
        takeOneItem(3);
        addItemsInSlot(4, result);
        markDirty();
    }


    private boolean isValidItems(){
        Item item = stacks.get(0).getItem();
        ItemStack result = stacks.get(4);
        Item resulti = result.getItem();

        SimpleCraftMap.ItemGen gen = pressCrafts.get(item);

        if(gen != null && (resulti == Items.AIR ||
                (result.getCount()+gen.getCount()<=64 && gen.get() == resulti)  ) ){
            return  stacks.get(1).getItem() == item &&
                    stacks.get(2).getItem() == item &&
                    stacks.get(3).getItem() == item;
        }
        return false;
    }


    @Override
    public int useOnActiveTick() {
        return 2;
    }
}