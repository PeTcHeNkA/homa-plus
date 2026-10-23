package plus.common.mashine.oxy;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemLeaves;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import plus.common.mashine.inv.EnergyDeviceTileEntity;
import plus.common.mashine.item.ItemOxyBallon;


public class SpOxyChargerTileEntity extends EnergyDeviceTileEntity {
    private static final int[] SLOTS_INPUT = new int[] {5, 6};
    private static final int[] SLOTS_OUTPUT = new int[] {7, 8};

    private short skip = 0;
    private boolean boost, needCheck = true;

    public SpOxyChargerTileEntity() {
        super(9);
    }


    @Override
    public void update() {
        if ((skip++ & 3) == 0) {
            int amount = boost ? 5 : 1;
            if(repairBallon(this, amount, 5, 7, 8) || repairBallon(this, amount, 6, 8, 7)){
                setProgress(1);
                if(needCheck){
                    checkBoost();
                    needCheck = false;
                }
            } else {
                skip = 0;
                needCheck = true;
                setProgress(0);
            }
        }
        if(skip > 256){
            skip = 0;
            checkBoost();
        }
    }


    private void checkBoost(){
        for (int i=0;i<5;i++){
            ItemStack stack = getStackInSlot(i);
            if(!stack.isEmpty()){
                boost = true;
                stack.shrink(1);
                setInventorySlotContents(i, stack);
                return;
            }
        }
        boost = false;
    }


    protected static boolean repairBallon(EnergyDeviceTileEntity te, int amount, int from, int to, int altTo){
        ItemStack item = te.getStackInSlot(from);
        if(item.isEmpty())return false;
        int dmg = item.getItemDamage();
        if(dmg>1){
            if(!te.useEnergyIfCan())return false;
            if((dmg -= amount) < 0) dmg = 0;
            item.setItemDamage(dmg);
            if(dmg>1)return true;
        }
        ItemStack resultItem = te.getStackInSlot(to);
        if(resultItem.isEmpty()) {
            te.setInventorySlotContents(from, resultItem);
            te.setInventorySlotContents(to, item);
        } else {
            resultItem = te.getStackInSlot(altTo);
            if(resultItem.isEmpty()){
                te.setInventorySlotContents(from, resultItem);
                te.setInventorySlotContents(altTo, item);
            }
        }
        return false;
    }


    @Override
    public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn) {
        return new SpOxyChargerContainer(playerInventory, this);
    }


    @Override
    public boolean canInsertItem(int index, ItemStack stack, EnumFacing direction) {
        switch (index){
            case 0: case 1: case 2: case 3: case 4:
                return stack.getItem() instanceof ItemLeaves;
            case 5: case 6:
                return SpOxyChargerContainer.isOxyBallon(stack);
            case 7: case 8:
                return ItemOxyBallon.isOxyBallon(stack);
            default:
                return false;
        }
    }


    @Override
    public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
        return index == 7 || index == 8;
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
        if(side == EnumFacing.DOWN){
            return SLOTS_OUTPUT;
        } else {
            return SLOTS_INPUT;
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        return super.writeToNBT(compound);
    }
}