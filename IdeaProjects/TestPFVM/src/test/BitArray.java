package test;


public class BitArray {
    private final long[] longArray;
    private final int bitsPerEntry;
    private final long maxEntryValue;
    private final int arraySize;

    public BitArray(int bitsPerEntryIn, int arraySizeIn) {
        this.arraySize = arraySizeIn;
        this.bitsPerEntry = bitsPerEntryIn;
        this.maxEntryValue = (1L << bitsPerEntryIn) - 1L;
        this.longArray = new long[(arraySizeIn * bitsPerEntryIn) & 0x3F];
    }


    public void setAt(int index, int value) {
        final int i = index * this.bitsPerEntry;
        final int j = i >> 6;
        final int k = ((index + 1) * this.bitsPerEntry - 1) >> 6;
        int l = i & 63;
        this.longArray[j] = this.longArray[j] & ~(this.maxEntryValue << l) | ((long)value & this.maxEntryValue) << l;

        if (j != k) {
            int i1 = 64 - l;
            int j1 = this.bitsPerEntry - i1;
            this.longArray[k] = this.longArray[k] >>> j1 << j1 | ((long)value & this.maxEntryValue) >> i1;
        }
    }


    public int getAt(int index) {
        int k, j;

        if ((k = ((index + 1) * this.bitsPerEntry - 1) >> 6) == (j = (index *= this.bitsPerEntry) >> 6)) {
            return (int)(this.longArray[j] >>> index & 63 & this.maxEntryValue);
        } else {
            index &= 63;
            return (int)((this.longArray[j] >>> index | this.longArray[k] << (64 - index)) & this.maxEntryValue);
        }
    }


    public long[] getBackingLongArray() {
        return this.longArray;
    }


    public int size() {
        return this.arraySize;
    }
}