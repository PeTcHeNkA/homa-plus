package plus.fserver.world.light;

import io.netty.buffer.FastByteBuf;


public final class LightArray {
    private byte[] data;

    public LightArray() {}


    public LightArray(byte[] data, byte empty) {
        if(data == null || data.length == 0)return;
        if(data.length != 2048)throw new IllegalArgumentException();
        l1:{
            for (int i = 0; i < 2048; i++)
                if (data[i] != empty)break l1;
            return;
        }
        this.data = data;
    }


    public LightArray(byte[] data) {
        this.data = data;
        if (data != null && data.length != 2048) {
            throw new IllegalArgumentException("ChunkNibbleArrays should be 2048 bytes not: " + data.length);
        }
    }


    public int getSkyLight(int x, int y, int z) {
        return this.getSkyFromIndex(getCoordinateIndex(x, y, z));
    }


    public int getBlockLight(int x, int y, int z) {
        return this.getBlockFromIndex(getCoordinateIndex(x, y, z));
    }


    public void setSkyLight(int x, int y, int z, int value) {
        byte[] data = this.data;
        if(data == null){
            if(value == 15)return;
            this.data = data = new byte[2048];
        }
        setIndex(data, getCoordinateIndex(x, y, z), value);
    }


    public void setBlockLight(int x, int y, int z, int value) {
        byte[] data = this.data;
        if(data == null){
            if(value == 0)return;
            this.data = data = new byte[2048];
        }
        setIndex(data, getCoordinateIndex(x, y, z), value);
    }


    private int getSkyFromIndex(int index) {
        byte[] data;
        if((data = this.data) == null)return 15;
        int arrIndex = getNibbleIndex(index);
        return isLowerNibble(index) ? data[arrIndex] & 15 : data[arrIndex] >> 4 & 15;
    }


    private int getBlockFromIndex(int index) {
        byte[] data;
        if((data = this.data) == null)return 0;
        int arrIndex = getNibbleIndex(index);
        return isLowerNibble(index) ? data[arrIndex] & 15 : data[arrIndex] >> 4 & 15;
    }


    static void setIndex(byte[] data, int index, int value) {
        int arrIndex = getNibbleIndex(index);
        if (isLowerNibble(index)) {
            data[arrIndex] = (byte)(data[arrIndex] & 240 | value & 15);
        } else {
            data[arrIndex] = (byte)(data[arrIndex] & 15 | (value & 15) << 4);
        }
    }


    static int getCoordinateIndex(int x, int y, int z) {
        return y << 8 | z << 4 | x;
    }


    static boolean isLowerNibble(int index) {
        return (index & 1) == 0;
    }


    static int getNibbleIndex(int arrIndex) {
        return arrIndex >> 1;
    }


    public byte[] getData() {
        return this.data;
    }


    public void writeSkyLightTo(FastByteBuf buf){
        byte[] data = this.data;
        if(data == null){
            writeEmptySkyLight(buf);
        } else {
            buf.writeBytes(data);
        }
    }


    public void writeBlockLightTo(FastByteBuf buf){
        byte[] data = this.data;
        if(data == null){
            writeEmptyBlockLight(buf);
        } else {
            buf.writeBytes(data);
        }
    }


    public static void writeEmptySkyLight(FastByteBuf buf){
        buf.writeBytesALL(2048, (byte) 0xFF);
    }


    public static void writeEmptyBlockLight(FastByteBuf buf){
        buf.writeBytesALL(2048, (byte) 0x00);
    }
}