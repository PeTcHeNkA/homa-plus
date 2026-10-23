package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import net.minecraft.util.math.MathHelper;


public class NBTTagDouble extends NBTPrimitive {
    private double data;

    NBTTagDouble() {}


    public NBTTagDouble(double data) {
        this.data = data;
    }


    @Override
    void write(DataOutput output) throws IOException {
        output.writeDouble(this.data);
    }


    @Override
    void read(DataInput input, int i, NBTSizeTracker tracker) throws IOException {
        tracker.read(128L);
        this.data = input.readDouble();
    }


    public static NBTTagDouble read(DataInput input, NBTSizeTracker tracker) throws IOException {
        tracker.read(128L);
        return new NBTTagDouble(input.readDouble());
    }


    @Override
    public byte getId() {
        return 6;
    }


    @Override
    public String toString() {
        return this.data + "d";
    }


    @Override
    public NBTTagDouble copy() {
        return this;
    }


    @Override
    public boolean equals(Object other) {
        return super.equals(other) && this.data == ((NBTTagDouble)other).data;
    }


    @Override
    public int hashCode() {
        long bits = Double.doubleToLongBits(this.data);
        return super.hashCode() ^ (int)(bits ^ bits >>> 32);
    }


    @Override
    public long getLong() {
        return (long)Math.floor(this.data);
    }


    @Override
    public int getInt() {
        return MathHelper.floor(this.data);
    }


    @Override
    public short getShort() {
        return (short)(MathHelper.floor(this.data) & '\uffff');
    }


    @Override
    public byte getByte() {
        return (byte)(MathHelper.floor(this.data) & 255);
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
