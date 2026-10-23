package plus.common.mashine.oxy;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import plus.common.mashine.block.scan.BlockScanner;
import plus.common.mashine.block.scan.BlockWay;
import plus.common.mashine.inv.EnergyDeviceTileEntity;
import plus.common.mashine.item.ItemCanister;
import plus.common.mashine.item.ItemOxyBallon;
import plus.common.mashine.item.ItemOxyFilterPlate;
import static plus.common.mashine.oxy.SpOxyChargerTileEntity.repairBallon;


public class SpStationOxyChargerTileEntity extends EnergyDeviceTileEntity {
    private static final int[] SLOTS_FILTER = new int[]{2,3,4,5};
    private static final int[] INPUT_WATER  = new int[]{0};
    private static final int[] INPUT_OXY_BALLONS  = new int[]{6, 7};
    private static final int[] OUTPUT = new int[]{1, 8, 9};

    private boolean areaPlaced = false;
    private short skip = 0;
    private int liquidLvl = 0, airLvl = 0;

    public SpStationOxyChargerTileEntity() {
        super(10);
    }
    

    @Override
    public void update() {
        if(++skip > 50){
            skip = 0;
        }
        if((skip & 7) == 0) fillWater();
        if((skip & 1) == 0) updateLtoA();
        if((skip & 3) == 0) moveAirToBallons();
    }


    private void moveAirToBallons() {
        int amount;
        if(airLvl > 0){
            amount = Math.min(airLvl, 100);
        } else return;

        if(repairBallon(this, amount, 6, 8, 9) ||
                repairBallon(this, amount, 7, 9, 8)){
            setProgress(1);
            airLvl -= amount;
        } else {
            setProgress(0);
        }
    }


    private void updateLtoA(){
        if(liquidLvl == 0 || airLvl >= SpStationOxyChargerContainer.MAX_AIR)return;
        if(useEnergyIfCan()){
            liquidLvl -= 2  ;
            airLvl    += 100;
        }
    }


    @Override
    public void setField(int id, int value) {
        if     (id == 1) liquidLvl = value;
        else if(id == 2) airLvl    = value;
        else super.setField(id, value);
    }


    @Override
    public int getField(int id) {
        if     (id == 1) return liquidLvl;
        else if(id == 2) return airLvl;
        else return super.getField(id);
    }


    public int getLiquidLvl(){
        return liquidLvl;
    }


    private void fillWater(){
        if(liquidLvl >= SpStationOxyChargerContainer.MAX_LIQUID)return;
        ItemStack stack = getStackInSlot(0);
        if(stack.isEmpty())return;

        if(stack.getItem() == Items.WATER_BUCKET){
            ItemStack to = getStackInSlot(1);
            if(to.isEmpty()){
                to = new ItemStack(Items.BUCKET);
            }
            if(to.getItem() == Items.BUCKET && to.getCount() < 16){
                to.setCount(to.getCount()+1);
            } else {
                return;
            }
            liquidLvl += 1000;
            setInventorySlotContents(1, to);
            return;
        }
        if(stack.getItem() == ItemCanister.WATER_CANISTER){
            ItemStack to = getStackInSlot(1);
            ItemStack result = ItemCanister.free1(stack);
            if(ItemCanister.getLvl(result) == 0){
                if(!to.isEmpty())return;
                setInventorySlotContents(1, result);
                setInventorySlotContents(0, ItemStack.EMPTY);
            } else {
                setInventorySlotContents(0, result);
            }
            liquidLvl += 1000;
        }
    }


    public void placeArea(){

    }


    @Override
    public String getGuiID() {
        return "minecraft:sp_oxy_station_charger_gui";
    }


    @Override
    public Container createContainer(InventoryPlayer inventoryPlayer, EntityPlayer entityPlayer) {
        return new SpStationOxyChargerContainer(inventoryPlayer, this);
    }


    @Override
    public int[] getSlotsForFace(EnumFacing enumFacing) {
        switch (enumFacing){
            case UP: return SLOTS_FILTER;
            case DOWN: return OUTPUT;
            case WEST:
            case EAST: return INPUT_WATER;
            case NORTH:
            case SOUTH: return INPUT_OXY_BALLONS;
            default: return null;
        }
    }


    @Override
    public boolean canInsertItem(int index, ItemStack stack, EnumFacing direction) {
        switch (index){
            case 2: case 3: case 4: case 5:
                return stack.getItem() instanceof ItemOxyFilterPlate;
            case 0:
                return SpStationOxyChargerContainer.isWaterField(stack);
            case 6: case 7:
                return SpOxyChargerContainer.isOxyBallon(stack);
            case 8: case 9:
                return ItemOxyBallon.isOxyBallon(stack);
        }
        return false;
    }


    @Override
    public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
        return index == 1 || index == 8 || index == 9;
    }


    @Override
    public int useOnActiveTick() {
        return 12;
    }


    @Override
    public void readFromNBT(NBTTagCompound compound) {
        liquidLvl = compound.getShort("liq");
        airLvl    = compound.getInteger("air");
        super.readFromNBT(compound);
    }


    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagCompound result = super.writeToNBT(compound);
        result.setShort("liq", (short) liquidLvl);
        result.setInteger("air", airLvl);
        return result;
    }
}