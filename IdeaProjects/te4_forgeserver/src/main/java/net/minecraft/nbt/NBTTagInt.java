package net.minecraft.nbt;

import plus.fserver.utls.NbtUtils;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;


public class NBTTagInt extends NBTPrimitive {
    private int data;

    NBTTagInt() {}


    public NBTTagInt(int data) {
        this.data = data;
    }


    @Override
    void write(DataOutput out) throws IOException {
        out.writeInt(this.data);
    }


    @Override
    void read(DataInput input, int i, NBTSizeTracker tracker) throws IOException {
        tracker.read(96L);
        this.data = input.readInt();
    }


    public static NBTPrimitive read(DataInput input, NBTSizeTracker tracker) throws IOException {
        tracker.read(96L);
        return NbtUtils.create(input.readInt());
    }


    @Override
    public byte getId() {
        return 3;
    }


    @Override
    public String toString() {
        return String.valueOf(this.data);
    }


    @Override
    public NBTTagInt copy() {
        return this;
    }


    @Override
    public boolean equals(Object p_equals_1_) {
        return super.equals(p_equals_1_) && this.data == ((NBTTagInt)p_equals_1_).data;
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
        return (short)(this.data & '\uffff');
    }


    @Override
    public byte getByte() {
        return (byte)(this.data & 255);
    }


    @Override
    public double getDouble() {
        return this.data;
    }


    @Override
    public float getFloat() {
        return (float)this.data;
    }
}
