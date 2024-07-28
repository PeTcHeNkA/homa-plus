package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;


public class NBTTagEnd extends NBTBase {
    public static final NBTTagEnd INSTANCE = new NBTTagEnd();
    NBTTagEnd() {}


    @Override
    void read(DataInput input, int i, NBTSizeTracker tracker) throws IOException {
        tracker.read(64L);
    }


    public static NBTTagEnd read(NBTSizeTracker tracker){
        tracker.read(64L);
        return INSTANCE;
    }


    @Override
    void write(DataOutput output) throws IOException {}


    @Override
    public byte getId() {
        return 0;
    }


    @Override
    public String toString() {
        return "END";
    }


    @Override
    public NBTTagEnd copy() {
        return this;
    }
}
