package plus.common.mashine.lincker;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import plus.common.mashine.inv.IPowerContainer;
import plus.common.mashine.inv.InvTileEntity;


public class SpCraftTileEntity extends InvTileEntity implements IPowerContainer {
    protected int power = 1;

    public SpCraftTileEntity() {
        super(5);
    }


    @Override
    public int getField(int id) {
        return power;
    }


    @Override
    public void setField(int id, int value) {
        power = value;
    }


    @Override
    public int getFieldCount() {
        return 1;
    }


    @Override
    public int getPower() {
        return power;
    }


    @Override
    public void setPower(int power) {
        this.power = power;
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


    @Override
    public Container createContainer(InventoryPlayer playerInv, EntityPlayer player) {
        return new SpCraftContainer(playerInv, this);
    }


    @Override
    public String getGuiID() {
        return "minecraft:sp_craft_gui";
    }


    @Override
    public String getName() {
        return this.hasCustomName() ? this.customName : "sp_craft_gui";
    }
}