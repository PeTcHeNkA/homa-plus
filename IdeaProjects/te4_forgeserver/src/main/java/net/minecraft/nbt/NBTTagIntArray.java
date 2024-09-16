package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;


public class NBTTagIntArray extends NBTBase {
    private int[] intArray;

    NBTTagIntArray() {}

    public NBTTagIntArray(int[] ints) {
        this.intArray = ints;
    }


    public NBTTagIntArray(List<Integer> list) {
        this(toArray(list));
    }


    private static int[] toArray(List<Integer> p_193584_0_) {
        int[] ints = new int[p_193584_0_.size()];

        for(int i = 0; i < p_193584_0_.size(); ++i) {
            Integer lvt_3_1_ = p_193584_0_.get(i);
            ints[i] = lvt_3_1_ == null ? 0 : lvt_3_1_;
        }

        return ints;
    }



    @Override
    void write(DataOutput output) throws IOException {
        int[] arr = this.intArray;
        output.writeInt(arr.length);
        for (int i : arr) output.writeInt(i);
    }


    @Override
    void read(DataInput input, int p_152446_2_, NBTSizeTracker tracker) throws IOException {
        tracker.read(192L);
        int size = input.readInt();
        tracker.read(32L * size);
        int[] arr = this.intArray = new int[size];

        for(int i = 0; i < size; ++i) arr[i] = input.readInt();
    }


    @Override
    public byte getId() {
        return 11;
    }


    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("[I;");
        for(int lvt_2_1_ = 0; lvt_2_1_ < this.intArray.length; ++lvt_2_1_) {
            if (lvt_2_1_ != 0) {
                builder.append(',');
            }
            builder.append(this.intArray[lvt_2_1_]);
        }
        return builder.append(']').toString();
    }


    @Override
    public NBTTagIntArray copy() {
        int[] copy = new int[this.intArray.length];
        System.arraycopy(this.intArray, 0, copy, 0, this.intArray.length);
        return new NBTTagIntArray(copy);
    }


    @Override
    public boolean equals(Object other) {
        return super.equals(other) && Arrays.equals(this.intArray, ((NBTTagIntArray)other).intArray);
    }


    @Override
    public int hashCode() {
        return super.hashCode() ^ Arrays.hashCode(this.intArray);
    }


    public int[] getIntArray() {
        return this.intArray;
    }
}