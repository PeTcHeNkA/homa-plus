package net.minecraft.world.storage;

import net.minecraftforge.common.util.*;
import net.minecraft.nbt.*;


public abstract class WorldSavedData implements INBTSerializable<NBTTagCompound> {
    public final String mapName;
    private boolean dirty;

    public WorldSavedData(final String name) {
        this.mapName = name;
    }


    public abstract void readFromNBT(final NBTTagCompound p0);


    public abstract NBTTagCompound writeToNBT(final NBTTagCompound p0);


    public void markDirty() {
        this.setDirty(true);
    }


    public void setDirty(final boolean isDirty) {
        this.dirty = isDirty;
    }


    public boolean isDirty() {
        return this.dirty;
    }


    @Override
    public void deserializeNBT(final NBTTagCompound nbt) {
        this.readFromNBT(nbt);
    }


    @Override
    public NBTTagCompound serializeNBT() {
        return this.writeToNBT(new NBTTagCompound());
    }


    public boolean shouldCopyNbtOnAsync(){
        return true;
    }
}