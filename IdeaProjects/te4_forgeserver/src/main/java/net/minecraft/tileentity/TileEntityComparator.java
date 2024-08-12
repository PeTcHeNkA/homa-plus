//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package net.minecraft.tileentity;

import net.minecraft.nbt.NBTTagCompound;


public class TileEntityComparator extends TileEntity {
    private int outputSignal;

    public TileEntityComparator() {}


    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setIntegerU("OutputSignal", this.outputSignal);
        return nbt;
    }


    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.outputSignal = nbt.getInteger("OutputSignal");
    }


    public int getOutputSignal() {
        return this.outputSignal;
    }


    public void setOutputSignal(int value) {
        this.outputSignal = value;
    }
}