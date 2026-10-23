package net.minecraft.nbt;

import java.io.DataOutput;
import java.io.IOException;


public class NBTTagEmptyByteArray extends NBTTagByteArray{
    private int len;
    private byte value;

    public NBTTagEmptyByteArray(byte[] ref, int len, byte value){
        super(ref);
        this.len = len;
        this.value = value;
    }

    public NBTTagEmptyByteArray(int len, byte value){
        super(NBTTagCompound.empty);
        this.len = len;
        this.value = value;
    }

    @Override
    void write(DataOutput output) throws IOException {
        output.writeInt(len);
        for (int i = 0, s = len; i<s;i++){
            output.write(value);
        }
    }
}