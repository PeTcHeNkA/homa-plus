package net.minecraft.nbt;

import plus.fserver.utls.NbtUtils;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;


public class NBTTagByte extends NBTPrimitive {
    private byte data;

    NBTTagByte() {}


    public NBTTagByte(byte b) {
        this.data = b;
    }


    void write(DataOutput p_74734_1_) throws IOException {
        p_74734_1_.writeByte(this.data);
    }


    void read(DataInput input, int i, NBTSizeTracker tracker) throws IOException {
        tracker.read(72L);
        this.data = input.readByte();
    }


    public static NBTTagByte read(DataInput input, NBTSizeTracker tracker) throws IOException {
        tracker.read(72L);
        return NbtUtils.create(input.readByte());
    }


    @Override
    public byte getId() {
        return 1;
    }


    @Override
    public String toString() {
        return this.data + "b";
    }


    @Override
    public NBTTagByte copy() {
        return this;
    }


    @Override
    public boolean equals(Object other) {
        return super.equals(other) && this.data == ((NBTTagByte)other).data;
    }


    @Override
    public int hashCode() {
        return super.hashCode() ^ this.data;
    }


    @Override
    public long getLong() {
        return this.data;
    }


    @Override
    public int getInt() {
        return this.data;
    }


    @Override
    public short getShort() {
        return this.data;
    }


    @Override
    public byte getByte() {
        return this.data;
    }


    @Override
    public double getDouble() {
        return this.data;
    }


    @Override
    public float getFloat() {
        return this.data;
    }
}
