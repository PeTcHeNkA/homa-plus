package net.minecraft.world.chunk.storage;

import io.netty.buffer.FastByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagEmptyByteArray;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.BlockIDIndexes;
import net.minecraft.world.chunk.BlockStateContainer;
import net.minecraft.world.chunk.NibbleArray;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import plus.fserver.utls.collections.IntBlockIndex;
import java.util.Arrays;


public class ExtendedBlockStorage extends BlockStateContainer{
    private static final byte[] ALL_15      = new byte[]{15};
    private static final byte[] ALL_ZERO    = new byte[]{0};
    private final int yBase;
    private short blockRefCount;
    private short tickRefCount;
    private byte[] blockLight, skyLight = ALL_ZERO;

    public ExtendedBlockStorage(int y, boolean storeSkylight) {
        this.yBase = y;
    }


    public IBlockState get(int x, int y, int z) {
        return this.blPlt.get(super.getAt(y << 8 | z << 4 | x));
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


    public boolean setB(int x, int y, int z, IBlockState state) {
        IBlockState prev = super.update(x, y, z, state = state.getClean());
        if(prev == state)return false;

        Block block1;
        if ((block1 = state.getBlock()) != Blocks.AIR) {
            ++this.blockRefCount;
            if (block1.getTickRandomly()) ++this.tickRefCount;
        }

        if ((block1 = prev.getBlock()) != Blocks.AIR) {
            --this.blockRefCount;
            if (block1.getTickRandomly()) --this.tickRefCount;
        }
        return true;
    }


    public boolean isEmpty() {
        return this.blockRefCount == 0;
    }


    public boolean needsRandomTick() {
        return this.tickRefCount > 0;
    }


    public int getYLocation() {
        return this.yBase;
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
        return getSkyFromIndex(getCoordinateIndex(x, y, z));
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
        return this.getBlockFromIndex(getCoordinateIndex(x, y, z));
    }


    @Nullable
    public NibbleArray getDataForNBT(byte[] blockIds, NibbleArray data) {
        NibbleArray array = null;
        for (int i = 0, j, x, y, z; i < 4096; ++i) {
            x = i & 15;
            y = i >> 8 & 15;
            z = i >> 4 & 15;
            if (((j = getId(i)) >> 12 & 15) != 0) {
                if (array == null) array = new NibbleArray();
                array.set(x, y, z, j >> 12 & 15);
            }
            blockIds[i] = (byte)(j >> 4 & 255);
            data.set(x, y, z, j & 15);
        }
        return array;
    }


    public void setDataFromNBT(final byte[] blockIds, final NibbleArray data, @Nullable final NibbleArray arr) {
        final IntBlockIndex index = BlockIDIndexes.BLOCK_INDEX;
        if(arr == null) {
            for (int i = 0, x, y, z; i < 4096; ++i) {
                x = i & 15;
                y = i >> 8 & 15;
                z = i >> 4 & 15;
                this.setC(i, index.get((blockIds[i] & 255) << 4 | data.get(x, y, z)));
            }
        } else {
            for (int i = 0, x, y, z; i < 4096; ++i) {
                x = i & 15;
                y = i >> 8 & 15;
                z = i >> 4 & 15;
                this.setC(i, index.get(arr.get(x, y, z) << 12 | (blockIds[i] & 255) << 4 | data.get(x, y, z)));
            }
        }
    }


    public void write(PacketBuffer buf) {
        buf.writeByte(this.bit);
        this.blPlt.write(buf);
        buf.writeLongArray(super.getBackingLongArray());
    }


    public void write(FastByteBuf buf) {
        buf.writeByteV(this.bit);
        this.blPlt.write(buf);
        buf.writeLongArray(super.getBackingLongArray());
    }


    public int getSerializedSize() {
        return 1
                + this.blPlt.getSerializedSize()
                + PacketBuffer.getVarIntSize(super.size())
                + super.getBackingLongArray().length << 3;
    }


    private int getId(int index) {
        return this.blPlt.get(super.getAt(index)).getId();
    }


    private int getBlockFromIndex(int index) {
        byte[] data;
        if((data = this.blockLight) == null)return 0;
        int arrIndex = getNibbleIndex(index);
        return isLowerNibble(index) ? data[arrIndex] & 15 : data[arrIndex] >> 4 & 15;
    }


    private int getSkyFromIndex(int index) {
        byte[] data;
        if((data = this.skyLight).length == 1)return data[0];
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
            return new NBTTagEmptyByteArray(data,2048, data[0]);
        }
        return new NBTTagByteArray(data);
    }


    public void setBlockLight(byte[] blockLight) {
        this.blockLight = lightBlockBytes(blockLight);
    }


    public void setSkyLight(byte[] skyLight) {
        this.skyLight = lightSkyBytes(skyLight);
    }


    public void tryFreeLight(){
        this.blockLight = lightBlockBytes(this.blockLight);
        this.skyLight   = lightSkyBytes(this.skyLight);
    }


    @Nullable
    static byte[] lightBlockBytes(@Nullable byte[] data){
        if(data == null || data.length == 0)return null;
        byte empty = 0x00;
        if(data.length != 2048)throw new IllegalArgumentException();
        for (int i = 0; i < 2048; i++)
            if (data[i] != empty)return data;
        return null;
    }


    @NotNull
    static byte[] lightSkyBytes(@Nullable byte[] data){
        if(data == null)return ALL_ZERO;
        int len;
        if((len = data.length) == 1){
            byte fill = data[0];
            if(fill == 15)return ALL_15;
            if(fill == 0)return ALL_ZERO;
            return new byte[]{fill};
        }
        if(len == 0)return ALL_ZERO;
        if(len != 2048)throw new IllegalArgumentException();
        byte first = data[0];
        for (int i = 1; i < 2048; i++) {
            byte cur = data[i];
            if (cur != first) return data;
        }
        if(first == 0)return ALL_ZERO;
        if(first == 15)return ALL_15;
        return new byte[]{first};
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