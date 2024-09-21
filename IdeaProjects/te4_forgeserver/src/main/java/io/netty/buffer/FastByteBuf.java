package io.netty.buffer;


public class FastByteBuf extends UnpooledHeapByteBuf {
    private static final UnpooledByteBufAllocator ALLOC = UnpooledByteBufAllocator.DEFAULT;
    private final int maxCapacity;

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
        byte[] arr = this.array;
        while ((input & 0xFFFFFF80) != 0x0) {
            arr[this.writerIndex++] = (byte) ((input & 0x7F) | 0x80);
            input >>>= 7;
        }
        arr[this.writerIndex++] = (byte) input;
    }


    public void writeBytesALL(int count, byte b){
        ensureWritable0(count);
        byte[] arr = this.array;
        for (int i = 0; i < count; i++){
            arr[i] = b;
        }
        this.writerIndex += count;
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
        array[this.writerIndex++] = (byte) value;
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
