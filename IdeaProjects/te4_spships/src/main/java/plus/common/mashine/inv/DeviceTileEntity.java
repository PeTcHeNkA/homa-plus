package plus.common.mashine.inv;

import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import plus.utls.MathUtils;


public abstract class DeviceTileEntity extends InvTileEntity implements ITickable, ISidedInventory, IPowerContainer {
    protected int power = 50;
    protected float speed = 1;
    protected float progress = 0;

    public DeviceTileEntity(int size) {
        super(size);
    }


    public float tickProgress(){
        return progress+=speed;
    }


    public boolean onSuccessProgress(int max){
        if(tickProgress()>max){
            progress = 0;
            return true;
        }
        return false;
    }


    protected void addItemsInSlot(int slot, SimpleCraftMap.ItemGen gen){
        ItemStack itemStack = stacks.get(slot);
        if(itemStack.isEmpty()){
            stacks.set(slot, gen.create());
        } else {
            itemStack.setCount(itemStack.getCount()+gen.getCount());
        }
    }


    protected void takeOneItem(int slot){
        ItemStack item = stacks.get(slot);
        item.setCount(item.getCount()-1);
    }


    protected void takeOneItem(ItemStack item){
        item.setCount(item.getCount()-1);
    }


    public void resetProgress(){
        progress = 0;
    }


    @Override
    public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction) {
        return true;
    }


    @Override
    public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
        return direction == EnumFacing.DOWN;
    }


    @Override
    public int getField(int id) {
        return (int) progress;
    }


    @Override
    public void setField(int id, int value) {
        this.progress = value;
    }


    @Override
    public int getFieldCount() {
        return 1;
    }


    @Override
    public void setPower(int power) {
        this.power = power;
        speed = (float) MathUtils.calcBigStructCoef(power);
    }


    @Override
    public int getPower() {
        return power;
    }


    protected final net.minecraftforge.items.IItemHandler handlerBottom =
            new net.minecraftforge.items.wrapper.SidedInvWrapper(this, net.minecraft.util.EnumFacing.DOWN);

    protected final net.minecraftforge.items.IItemHandler handlerSide =
            new net.minecraftforge.items.wrapper.SidedInvWrapper(this, net.minecraft.util.EnumFacing.WEST);


    @Override
    @javax.annotation.Nullable
    public <T> T getCapability(net.minecraftforge.common.capabilities.Capability<T> capability, @javax.annotation.Nullable net.minecraft.util.EnumFacing facing) {
        if (facing != null && capability == net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            if (facing == EnumFacing.DOWN)
                return (T) handlerBottom;
            else {
                return (T) handlerSide;
            }
        return super.getCapability(capability, facing);
    }


    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        power = compound.getInteger("pow");
    }


    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setInteger("pow", power);
        return super.writeToNBT(compound);
    }
}