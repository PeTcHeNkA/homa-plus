package test;

public class BitArrayF4 {
    private static final int bitsPerEntry = 4;
    private static final long maxEntryValue = (1L << 4 /*bitsPerEntry*/) - 1L;

    protected final long[] longArray;
    protected final int arraySize;

    public BitArrayF4(int arraySizeIn) {
        this(arraySizeIn, bitsPerEntry);
    }

    protected BitArrayF4(int arraySizeIn, int bits) {
        this.arraySize = arraySizeIn;
        this.longArray = new long[(arraySizeIn * bits) & 0x3F];
    }


    public void setAt(int index, int value) {
        int i = index * bitsPerEntry;
        int j = i >> 6;
        int k = ((index + 1) * bitsPerEntry - 1) >> 6;
        int l = i & 63;
        this.longArray[j] = this.longArray[j] & ~(maxEntryValue << l) | ((long)value & maxEntryValue) << l;

        if (j != k) {
            int i1 = 64 - l;
            int j1 = bitsPerEntry - i1;
            this.longArray[k] = this.longArray[k] >>> j1 << j1 | ((long)value & maxEntryValue) >> i1;
        }
    }


    public int getAt(int index) {
        int k, j;

        if ((k = ((index + 1) * bitsPerEntry - 1) >> 6) == (j = (index *= bitsPerEntry) >> 6)) {
            return (int)(this.longArray[j] >>> index & 63 & maxEntryValue);
        } else {
            index &= 63;
            return (int)((this.longArray[j] >>> index | this.longArray[k] << (64 - index)) & maxEntryValue);
        }
    }


    public long[] getBackingLongArray() {
        return this.longArray;
    }


    public int size() {
        return this.arraySize;
    }
}