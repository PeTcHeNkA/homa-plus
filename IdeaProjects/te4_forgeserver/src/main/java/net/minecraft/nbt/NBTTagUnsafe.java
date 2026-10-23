package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.UnsupportedEncodingException;


public class NBTTagUnsafe extends NBTBase {
    private Object value;

    public Object getValue(){
        return value;
    }


    public void setValue(Object obj){
        this.value = obj;
    }


    public static class NoCopy extends NBTTagUnsafe{
        @Override
        public NBTBase copy() {
            return NBTTagEnd.INSTANCE;
        }
    }


    @Override
    void write(DataOutput dataOutput) throws IOException {
        throw new UnsupportedEncodingException();
    }


    @Override
    void read(DataInput dataInput, int i, NBTSizeTracker nbtSizeTracker) throws IOException {
        throw new UnsupportedEncodingException();
    }


    @Override
    public String toString() {
        return value.toString();
    }


    @Override
    public byte getId() {
        return 0;
    }


    @Override
    public NBTBase copy() {
        return this;
    }
}
