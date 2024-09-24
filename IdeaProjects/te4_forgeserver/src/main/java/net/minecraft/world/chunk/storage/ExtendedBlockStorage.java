package net.minecraft.world.chunk.storage;

import io.netty.buffer.FastByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagEmptyByteArray;
import net.minecraft.world.chunk.BlockStateContainer;
import java.util.Arrays;


public class ExtendedBlockStorage extends BlockStateContainer{
    private static final byte[] ALL_15      = new byte[]{15};
    private static final byte[] ALL_ZERO    = new byte[]{0};
    private final byte data;
    private short blockRefCount;
    private short tickRefCount;
    private byte[] blockLight;           //null (all-0) or any
    private byte[] skyLight = ALL_15;  //all-0 or all-15 or any

    public ExtendedBlockStorage(int y, boolean storeSkylight) {
        this.data = (byte) (y & 15);
    }


    public void set(int x, int y, int z, IBlockState state) {
        Block block1 = (state = state.getClean()).getBlock();
        if (block1 != Blocks.AIR) {
            ++this.blockRefCount;
            if (block1.getTickRandomly()) ++this.tickRefCount;
        }

        if ((block1 = super.update(x, y, z, state).getBlock()) != Blocks.AIR) {
            --this.blockRefCount;
            if (block1.getTickRandomly()) --this.tickRefCount;
        }
    }


    public boolean isEmpty() {
        return this.blockRefCount == 0;
    }
//
//
//    public boolean isEmptyLight(){
//        if(this.blockRefCount != 0 || blockLight != null)return false;
//        byte[] data;
//        if((data = this.skyLight).length == 1) {
//            return data[0] == 15;
//        }
//        return false;
//    }


    public boolean needsRandomTick() {
        return this.tickRefCount > 0;
    }


    public int getYLocation() {
        return this.data & 15;
    }


    public void setSkyLight(int x, int y, int z, int value) {
        byte[] data;
        if((data = this.skyLight).length == 1) {
            //return if all 15 -> 15 or all 0 -> 0
            if(data[0] == value)return;
            this.skyLight = data = new byte[2048];
        }
        setIndex(data, getCoordinateIndex(x, y, z), value);
    }


    public int getSkyLight(int x, int y, int z) {
        final byte[] data;
        if((data = this.skyLight).length == 1) return data[0];
        int index = getCoordinateIndex(x, y, z);
        int arrIndex = getNibbleIndex(index);
        return isLowerNibble(index) ? data[arrIndex] & 15 : data[arrIndex] >> 4 & 15;
    }


    public void setBlockLight(int x, int y, int z, int value) {
        byte[] data;
        if((data = this.blockLight) == null){
            if(value == 0)return;
            this.blockLight = data = new byte[2048];
        }
        setIndex(data, getCoordinateIndex(x, y, z), value);
    }


    public int getBlockLight(int x, int y, int z) {
        byte[] data;
        if((data = this.blockLight) == null)return 0;
        int index = getCoordinateIndex(x, y, z);
        int arrIndex = getNibbleIndex(index);
        return isLowerNibble(index) ? data[arrIndex] & 15 : data[arrIndex] >> 4 & 15;
    }


    public void recalculateRefCounts() {
        this.blockRefCount = 0;
        this.tickRefCount = 0;
        Block block;
        for (int i = 0; i < 16; ++i) {
            for (int j = 0; j < 16; ++j) {
                for (int k = 0; k < 16; ++k) {
                    if ((block = this.get(i, j, k).getBlock()) != Blocks.AIR) {
                        ++this.blockRefCount;
                        if (block.getTickRandomly()) ++this.tickRefCount;
                    }
                }
            }
        }
    }


    @Deprecated
    public BlockStateContainer getData() {
        return this;
    }


    public byte[] getBlockLightData() {
        byte[] data = this.blockLight;
        if(data == null)return this.blockLight = new byte[2048];
        return data;
    }


    public byte[] getSkyLightData() {
        byte[] data = this.skyLight;
        if(data.length == 1) {
            if (data[0] == 15) {
                this.skyLight = data = new byte[2048];
                Arrays.fill(data, (byte) 0xFF);
            } else {
                return this.skyLight = new byte[2048];
            }
        }
        return data;
    }


    public void writeBlockLightTo(FastByteBuf buf){
        byte[] data = this.blockLight;
        if(data == null){
            buf.writerIndex(buf.writerIndex()+2048);
        } else {
            buf.writeBytes(data);
        }
    }


    public void writeSkyLightTo(FastByteBuf buf){
        byte[] data = this.skyLight;
        if(data.length == 1) {
            byte b = data[0];
            if(b == 15){
                buf.writeBytesALL(2048, (byte) 0xFF);
            } else {
                buf.writerIndex(buf.writerIndex()+2048);
            }
        } else {
            buf.writeBytes(data);
        }
    }


    public NBTTagByteArray toBlockLightNbt(){
        byte[] data = this.blockLight;
        if(data == null)return new NBTTagEmptyByteArray(2048, (byte) 0x00);
        return new NBTTagByteArray(data);
    }


    public NBTTagByteArray toSkyLightNbt(){
        byte[] data = this.skyLight;
        if(data.length == 1){
            return new NBTTagEmptyByteArray(2048, data[0]);
        }
        return new NBTTagByteArray(data);
    }


    public void setBlockLight(byte[] blockLight) {
        this.blockLight = lightBlockBytes(blockLight);
    }


    public void setSkyLight(byte[] skyLight) {
        this.skyLight = lightSkyBytes(skyLight);
    }



    static byte[] lightBlockBytes(byte[] data){
        if(data == null || data.length == 0)return null;
        byte empty = 0x00;
        if(data.length != 2048)throw new IllegalArgumentException();
        for (int i = 0; i < 2048; i++)
            if (data[i] != empty)return data;
        return null;
    }


    static byte[] lightSkyBytes(byte[] data){
        if(data == null)return ALL_ZERO;
        int len;
        if((len = data.length) == 1){
            byte b = data[0];
            if(b == 15)return ALL_15;
            return ALL_ZERO;
        }
        if(len == 0)return ALL_ZERO;
        if(len != 2048)throw new IllegalArgumentException();
        for (int i = 0; i < 2048; i++) {
            byte cur = data[i];
            if (cur != 0 && cur != -127) return data;
        }
        return data[0] == 0? ALL_ZERO : ALL_15;
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
}