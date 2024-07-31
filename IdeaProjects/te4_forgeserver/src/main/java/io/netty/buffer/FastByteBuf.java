package io.netty.buffer;


public class FastByteBuf extends UnpooledHeapByteBuf {
    private static final UnpooledByteBufAllocator ALLOC = UnpooledByteBufAllocator.DEFAULT;
    private final int maxCapacity;

    public FastByteBuf(int initialCapacity, int maxCapacity) {
        super(ALLOC, initialCapacity, maxCapacity);
        this.maxCapacity = maxCapacity;
        this.writerIndex = 0;
    }


    public FastByteBuf(byte[] initialArray, int maxCapacity) {
        super(ALLOC, initialArray, maxCapacity);
        this.maxCapacity = maxCapacity;
        this.writerIndex = 0;
    }


    public FastByteBuf(byte[] initialArray){
        this(initialArray, initialArray.length);
    }


    public void writeLongArray(final long[] array) {
        this.ensureAccessible();
        writeVarInt(array.length);
        this.ensureWritable0(array.length<<4);
        byte[] arr = this.array;
        int writerIndex = this.writerIndex;

        for (final long l : array) {
            HeapByteBufUtil.setLong(arr, writerIndex, l);
            writerIndex += 8;
        }
        this.writerIndex = writerIndex;
    }


    public void writeVarInt(int input) {
        this.ensureAccessible();
        while ((input & 0xFFFFFF80) != 0x0) {
            this.writeByteF((input & 0x7F) | 0x80);
            input >>>= 7;
        }
        this.writeByteF(input);
    }


    public void writeVarLong(long value) {
        this.ensureAccessible();
        while ((value & 0xFFFFFFFFFFFFFF80L) != 0x0L) {
            this.writeByteF((int)(value & 0x7FL) | 0x80);
            value >>>= 7;
        }
        this.writeByteF((int)value);
    }


    public final void writeByteV(int value) {
        this.ensureAccessible();
        this.ensureWritable0(1);
        this._setByte(this.writerIndex++, value);
    }


    private void writeByteF(int value) {
        this.ensureWritable0(1);
        this._setByte(this.writerIndex++, value);
    }


    private void ensureWritable0(int minWritableBytes) {
        if (minWritableBytes > this.writableBytes()) {
            if (minWritableBytes > this.maxCapacity - this.writerIndex) {
                throw new IndexOutOfBoundsException(String.format("writerIndex(%d) + minWritableBytes(%d) exceeds maxCapacity(%d): %s", this.writerIndex, minWritableBytes, this.maxCapacity, this));
            } else {
                int newCapacity = this.alloc().calculateNewCapacity(this.writerIndex + minWritableBytes, this.maxCapacity);
                this.capacity(newCapacity);
            }
        }
    }
}
