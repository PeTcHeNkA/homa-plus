package plus.common.mashine.oxy;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import plus.common.mashine.inv.EnergyDeviceTileEntity;


public class SpOxyChargerTileEntity extends EnergyDeviceTileEntity {
    private static final int[] SLOTS_INPUT = new int[] {2, 3};
    private static final int[] SLOTS_OUTPUT = new int[] {4, 5};

    private byte skip = 0;

    public SpOxyChargerTileEntity() {
        super(9);
    }



    @Override
    public void update() {
        if (skip++>4) {
            skip = 0;
            if(repairBallon(2, 4, 5) || repairBallon(3, 5, 4)){
                setField(0, 1);
            } else {
                setField(0, 0);
            }
        }
    }


    private boolean repairBallon(int from, int to, int altTo){
        ItemStack item = getStackInSlot(from);
        if(item.isEmpty())return false;
        int dmg = item.getItemDamage();
        if(dmg>1){
            if(!useEnergyIfCan())return false;
            item.setItemDamage(dmg = dmg-1);
            if(dmg>1)return true;
        }
        ItemStack resultItem = getStackInSlot(to);
        if(resultItem.isEmpty()) {
            setInventorySlotContents(from, resultItem);
            setInventorySlotContents(to, item);
        } else {
            resultItem = getStackInSlot(altTo);
            if(resultItem.isEmpty()){
                setInventorySlotContents(from, resultItem);
                setInventorySlotContents(altTo, item);
            }
        }
        return false;
    }


    @Override
    public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn) {
        return new SpOxyChargerContainer(playerInventory, this);
    }


    @Override
    public String getGuiID() {
        return "minecraft:sp_oxy_charger_gui";
    }


    @Override
    public String getName() {
        return this.hasCustomName() ? this.customName : "sp_oxy_charger_gui";
    }


    @Override
    public int useOnActiveTick() {
        return 4;
    }


    @Override
    public int[] getSlotsForFace(EnumFacing side) {
        if(side==EnumFacing.DOWN){
            return SLOTS_OUTPUT;
        } else {
            return SLOTS_INPUT;
        }
    }
}