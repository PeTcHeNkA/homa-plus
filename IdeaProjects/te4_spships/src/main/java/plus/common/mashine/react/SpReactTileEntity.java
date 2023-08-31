package plus.common.mashine.react;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import plus.common.Strings;
import plus.common.mashine.inv.IEnergyFolder;
import plus.common.mashine.inv.InvTileEntity;
import plus.common.mashine.item.ReactComponent;
import javax.annotation.Nullable;


public class SpReactTileEntity extends InvTileEntity implements ITickable, IEnergyFolder{
    private int boostFlag = 0;
    private float maxPower;
    private float genSpeed;
    private float energy;
    private byte ignoreTicks = 0;

    public SpReactTileEntity() {
        super(9);
    }


    @Override
    public Container createContainer(InventoryPlayer playerInv, EntityPlayer playerIn) {
        return new SpReactContainer(playerInv, this);
    }


    @Override
    public String getGuiID() {
        return "minecraft:aether_react";
    }


    private void checkItems(){
        boostFlag = 1;
        ItemStack core = stacks.get(8);
        if(core.isEmpty()){
            genSpeed = 0;
            return;
        }
        int coreLvl = ((ReactComponent)core.getItem()).level  ;

        //int summTeh = 0;
        float summPow = 0;
        for(int i=0;i<8;++i){
            ItemStack rci = stacks.get(i);
            NBTTagCompound tags = rci.getTagCompound();
            if(rci.isEmpty() || tags==null)continue;
            ReactComponent rc = (ReactComponent) rci.getItem();

            if(rc.getType() == ReactComponent.TYPE.COMPONENT){
                //summTeh = summTeh + tags.getInteger(("teh-prc"));
                summPow = summPow + (tags.getFloat(Strings.POWER)+(rc.level/2f));
            } else {
                ++boostFlag;
            }
        }
        if(summPow==0){
            genSpeed = 0;
        } else {
            NBTTagCompound tags = core.getTagCompound();
            if(tags==null){
                genSpeed = 0;
                return;
            }
            ReactComponent rc = (ReactComponent) core.getItem();
            summPow += (tags.getFloat(Strings.POWER)+(rc.level/2f));

            genSpeed = coreLvl * summPow*20;
            maxPower = genSpeed*2;
        }
    }


    @Override
    public ItemStack removeStackFromSlot(int index) {
        ItemStack result = super.removeStackFromSlot(index);
        checkItems();
        return result;
    }


    @Override
    public ItemStack decrStackSize(int index, int count) {
        ItemStack result = super.decrStackSize(index, count);
        checkItems();
        return result;
    }


    @Override
    public void setInventorySlotContents(int index, @Nullable ItemStack stack) {
        super.setInventorySlotContents(index, stack);
        checkItems();
    }


    private void gen(){
        if(ignoreTicks++==21) {
            if (energy < maxPower && genSpeed > 0) {
                for (int i = 0; i < 9; i++) {
                    ItemStack rci = stacks.get(i);
                    rci.setItemDamage(rci.getItemDamage() + boostFlag);
                    if(rci.getItemDamage()>rci.getMaxDamage()){
                        stacks.set(i, ItemStack.EMPTY);
                        checkItems();
                    }
                }
                energy += genSpeed;
            }
            ignoreTicks=0;
        }
    }


    @Override
    public void update() {
        gen();
    }


    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        energy = compound.getFloat(Strings.ENERGY);
        checkItems();
    }


    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setFloat(Strings.ENERGY, energy);
        return super.writeToNBT(compound);
    }


    @Override
    @javax.annotation.Nullable
    public <T> T getCapability(net.minecraftforge.common.capabilities.Capability<T> capability, @javax.annotation.Nullable net.minecraft.util.EnumFacing facing) {
        return null;
    }


    @Override
    public int getFieldCount() {
        return 2;
    }


    @Override
    public int getField(int id) {
        if(id==0){
            return (int) (genSpeed*100);
        } else {
            return (int) energy;
        }
    }


    @Override
    public boolean takeEnergy(int amount) {
        if(amount<energy){
            energy-=amount;
            return true;
        }
        return false;
    }


    @Override
    public boolean putEnergy(int amount) {
        return false;
    }


    @Override
    public int getCurEnergy() {
        if(energy<maxPower) {
            return (int) energy;
        } else {
            return (int) maxPower;
        }
    }


    @Override
    public int getMaxEnergy() {
        return (int) maxPower;
    }
}