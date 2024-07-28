package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import net.minecraft.util.math.MathHelper;


public class NBTTagFloat extends NBTPrimitive {
    private float data;

    NBTTagFloat() {
    }


    public NBTTagFloat(float data) {
        this.data = data;
    }


    @Override
    void write(DataOutput output) throws IOException {
        output.writeFloat(this.data);
    }


    @Override
    void read(DataInput input, int i, NBTSizeTracker tracker) throws IOException {
        tracker.read(96L);
        this.data = input.readFloat();
    }


    public static NBTTagFloat read(DataInput input, NBTSizeTracker tracker) throws IOException {
        tracker.read(96L);
        return new NBTTagFloat(input.readFloat());
    }


    @Override
    public byte getId() {
        return 5;
    }


    @Override
    public String toString() {
        return this.data + "f";
    }


    @Override
    public NBTTagFloat copy() {
        return this;
    }


    @Override
    public boolean equals(Object other) {
        return super.equals(other) && this.data == ((NBTTagFloat)other).data;
    }


    @Override
    public int hashCode() {
        return super.hashCode() ^ Float.floatToIntBits(this.data);
    }


    @Override
    public long getLong() {
        return (long)this.data;
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
        return this.data;
    }
}
