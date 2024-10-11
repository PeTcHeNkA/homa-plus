package net.minecraft.nbt;

import plus.fserver.utls.NbtUtils;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;


public class NBTTagShort extends NBTPrimitive {
    private short data;

    public NBTTagShort() {}


    public NBTTagShort(short data) {
        this.data = data;
    }


    void write(DataOutput output) throws IOException {
        output.writeShort(this.data);
    }


    @Override
    void read(DataInput input, int depth, NBTSizeTracker tracker) throws IOException {
        tracker.read(80L);
        this.data = input.readShort();
    }


    public static NBTPrimitive read(DataInput input, NBTSizeTracker tracker) throws IOException {
        tracker.read(80L);
        return NbtUtils.create(input.readShort());
    }


    @Override
    public byte getId() {
        return 2;
    }


    @Override
    public String toString() {
        return this.data + "s";
    }


    @Override
    public NBTTagShort copy() {
        return this;
    }


    @Override
    public boolean equals(Object other) {
        return super.equals(other) && this.data == ((NBTTagShort)other).data;
    }


    public int hashCode() {
        return super.hashCode() ^ this.data;
    }


    public long getLong() {
        return this.data;
    }


    public int getInt() {
        return this.data;
    }


    public short getShort() {
        return this.data;
    }


    public byte getByte() {
        return (byte)(this.data & 255);
    }


    public double getDouble() {
        return this.data;
    }


    public float getFloat() {
        return this.data;
    }
}
