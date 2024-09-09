package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;


public class NBTTagIntArrayUnsafe extends NBTBase {
    private byte[] intArray;

    public NBTTagIntArrayUnsafe(byte[] bytes) {
        this.intArray = bytes;
    }


    @Override
    void write(DataOutput output) throws IOException {
        output.writeInt(this.intArray.length);
        byte[] var2 = this.intArray;
        for (byte b : var2) {
            output.writeInt(b & 255);
        }
    }


    @Override
    void read(DataInput input, int i, NBTSizeTracker tracker) throws IOException {
        throw new UnsupportedEncodingException();
    }


    @Override
    public byte getId() {
        return 11;
    }


    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("[I;");

        for(int i = 0; i < this.intArray.length; ++i) {
            if (i != 0) {
                builder.append(',');
            }

            builder.append(this.intArray[i]);
        }
        return builder.append(']').toString();
    }


    @Override
    public NBTTagIntArray copy() {
        byte[] realArr = this.intArray;
        int[] ints = new int[realArr.length];
        for (int i = 0; i < ints.length; i++){
            ints[i] = realArr[i] & 255;
        }
        return new NBTTagIntArray(ints);
    }


    @Override
    public boolean equals(Object obj) {
        return super.equals(obj) && Arrays.equals(this.intArray, ((NBTTagIntArrayUnsafe)obj).intArray);
    }


    @Override
    public int hashCode() {
        return super.hashCode() ^ Arrays.hashCode(this.intArray);
    }
}