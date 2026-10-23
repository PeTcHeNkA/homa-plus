package plus.fserver.utls.collections;

import net.minecraft.util.math.MathHelper;
import plus.fserver.ElcaCV;


public class BitArrayMut {
    private long[] longArray;
    private int bitsPerEntry;
    private long maxEntryValue;
    private int arraySize;

    public BitArrayMut(){}

    public BitArrayMut(int bitsPerEntryIn, int arraySizeIn) {
        init(bitsPerEntryIn, arraySizeIn);
    }


    public BitArrayMut copy(){
        BitArrayMut copy = new BitArrayMut();
        copy.longArray = longArray;
        copy.bitsPerEntry = bitsPerEntry;
        copy.maxEntryValue = maxEntryValue;
        copy.arraySize = arraySize;
        return copy;
    }


    public void init(int bitsPerEntryIn, int arraySizeIn) {
        this.arraySize = arraySizeIn;
        this.bitsPerEntry = bitsPerEntryIn;
        this.maxEntryValue = (1L << bitsPerEntryIn) - 1L;
        this.longArray = new long[MathHelper.roundUp(arraySizeIn * bitsPerEntryIn, 64) / 64];
    }


    public void setAt(int index, int value) {
        if(ElcaCV.CHECK_BI_ARRAY)
            if(index<0 || index>=this.arraySize || value > maxEntryValue)
                throw new IllegalArgumentException("Index ["+index+"] should be in [0, "+this.arraySize+"] and value ["+value+"] in [0, "+maxEntryValue+"]");

        int i = index * this.bitsPerEntry;
        int j = i >> 6;
        int k = ((index + 1) * this.bitsPerEntry - 1) >> 6;
        int l = i & 63;
        this.longArray[j] = this.longArray[j] & ~(this.maxEntryValue << l) | ((long)value & this.maxEntryValue) << l;

        if (j != k) {
            int i1 = 64 - l;
            int j1 = this.bitsPerEntry - i1;
            this.longArray[k] = this.longArray[k] >>> j1 << j1 | ((long)value & this.maxEntryValue) >> i1;
        }
    }


    public int getAt(int index) {
        if(ElcaCV.CHECK_BI_ARRAY)
            if(index<0 || index>=this.arraySize)
                throw new IllegalArgumentException("Index should be in range [0, "+this.arraySize+"]");

        int k, j, bits;

        if ((k = ((index + 1) * (bits = this.bitsPerEntry) - 1) >> 6) == (j = (index *= bits) >> 6)) {
            return (int)(this.longArray[j] >>> index & 63 & this.maxEntryValue);
        } else {
            index &= 63;
            long[] longArr;
            return (int)(((longArr = this.longArray)[j] >>> index | longArr[k] << (64 - index)) & this.maxEntryValue);
        }
    }


    public long[] getBackingLongArray() {
        return this.longArray;
    }


    public int size() {
        return this.arraySize;
    }
}