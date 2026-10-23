package test;

public class BitArrayV {
    private final long[] longArray;
    private final int bitsPerEntry;
    private final long maxEntryValue;
    private final int arraySize;

    public BitArrayV(int bitsPerEntryIn, int arraySizeIn) {
        this.arraySize = arraySizeIn;
        this.bitsPerEntry = bitsPerEntryIn;
        this.maxEntryValue = (1L << bitsPerEntryIn) - 1L;
        this.longArray = new long[(arraySizeIn * bitsPerEntryIn) & 0x3F];
    }


    public void setAt(int index, int value) {
        switch (bitsPerEntry){
            case 3:
                setAt3(longArray, index, value);
            case 4:
                setAt4(longArray, index, value);
            case 5:
                setAt5(longArray, index, value);
        }
    }


    public int getAt(int index) {
        switch (bitsPerEntry){
            case 3:
                return getAt3(longArray, index);
            case 4:
                return getAt4(longArray, index);
            case 5:
                return getAt5(longArray, index);
        }
        return 0;
    }


    public long[] getBackingLongArray() {
        return this.longArray;
    }


    public int size() {
        return this.arraySize;
    }


    public static void setAt3(long[] longArray, int index, int value) {
        final int bitsPerEntry = 3;
        final long maxEntryValue = (1L << bitsPerEntry) - 1L;

        int i = index * bitsPerEntry;
        int j = i >> 6;
        int k = ((index + 1) * bitsPerEntry - 1) >> 6;
        int l = i & 63;
        longArray[j] = longArray[j] & ~(maxEntryValue << l) | ((long)value & maxEntryValue) << l;

        if (j != k) {
            int i1 = 64 - l;
            int j1 = bitsPerEntry - i1;
            longArray[k] = longArray[k] >>> j1 << j1 | ((long)value & maxEntryValue) >> i1;
        }
    }


    public static void setAt4(long[] longArray, int index, int value) {
        final int bitsPerEntry = 4;
        final long maxEntryValue = (1L << bitsPerEntry) - 1L;

        int i = index * bitsPerEntry;
        int j = i >> 6;
        int k = ((index + 1) * bitsPerEntry - 1) >> 6;
        int l = i & 63;
        longArray[j] = longArray[j] & ~(maxEntryValue << l) | ((long)value & maxEntryValue) << l;

        if (j != k) {
            int i1 = 64 - l;
            int j1 = bitsPerEntry - i1;
            longArray[k] = longArray[k] >>> j1 << j1 | ((long)value & maxEntryValue) >> i1;
        }
    }


    public static void setAt5(long[] longArray, int index, int value) {
        final int bitsPerEntry = 5;
        final long maxEntryValue = (1L << bitsPerEntry) - 1L;

        int i = index * bitsPerEntry;
        int j = i >> 6;
        int k = ((index + 1) * bitsPerEntry - 1) >> 6;
        int l = i & 63;
        longArray[j] = longArray[j] & ~(maxEntryValue << l) | ((long)value & maxEntryValue) << l;

        if (j != k) {
            int i1 = 64 - l;
            int j1 = bitsPerEntry - i1;
            longArray[k] = longArray[k] >>> j1 << j1 | ((long)value & maxEntryValue) >> i1;
        }
    }


    public static int getAt3(long[] longArray, int index) {
        final int bitsPerEntry = 3;
        final long maxEntryValue = (1L << bitsPerEntry) - 1L;
        int k, j;

        if ((k = ((index + 1) * bitsPerEntry - 1) >> 6) == (j = (index *= bitsPerEntry) >> 6)) {
            return (int)(longArray[j] >>> index & 63 & maxEntryValue);
        } else {
            index &= 63;
            return (int)((longArray[j] >>> index | longArray[k] << (64 - index)) & maxEntryValue);
        }
    }


    public static int getAt4(long[] longArray, int index) {
        final int bitsPerEntry = 3;
        final long maxEntryValue = (1L << bitsPerEntry) - 1L;
        int k, j;

        if ((k = ((index + 1) * bitsPerEntry - 1) >> 6) == (j = (index *= bitsPerEntry) >> 6)) {
            return (int)(longArray[j] >>> index & 63 & maxEntryValue);
        } else {
            index &= 63;
            return (int)((longArray[j] >>> index | longArray[k] << (64 - index)) & maxEntryValue);
        }
    }


    public static int getAt5(long[] longArray, int index) {
        final int bitsPerEntry = 3;
        final long maxEntryValue = (1L << bitsPerEntry) - 1L;
        int k, j;

        if ((k = ((index + 1) * bitsPerEntry - 1) >> 6) == (j = (index *= bitsPerEntry) >> 6)) {
            return (int)(longArray[j] >>> index & 63 & maxEntryValue);
        } else {
            index &= 63;
            return (int)((longArray[j] >>> index | longArray[k] << (64 - index)) & maxEntryValue);
        }
    }
}