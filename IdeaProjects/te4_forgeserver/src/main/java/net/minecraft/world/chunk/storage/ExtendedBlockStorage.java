package net.minecraft.world.chunk.storage;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.chunk.BlockStateContainer;
import plus.fserver.world.light.LightArray;


public class ExtendedBlockStorage extends BlockStateContainer{
    private final int yBase;
    private int blockRefCount;
    private int tickRefCount;
    private LightArray blockLight;
    private LightArray skyLight;

    public ExtendedBlockStorage(int y, boolean storeSkylight) {
        this.yBase = y;
        this.blockLight = new LightArray();
        if (storeSkylight) {
            this.skyLight = new LightArray();
        }
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


    public boolean needsRandomTick() {
        return this.tickRefCount > 0;
    }


    public int getYLocation() {
        return this.yBase;
    }


    public void setSkyLight(int x, int y, int z, int value) {
        this.skyLight.setSkyLight(x, y, z, value);
    }


    public int getSkyLight(int x, int y, int z) {
        return this.skyLight.getBlockLight(x, y, z);
    }


    public void setBlockLight(int x, int y, int z, int value) {
        this.blockLight.setBlockLight(x, y, z, value);
    }


    public int getBlockLight(int x, int y, int z) {
        return this.blockLight.getBlockLight(x, y, z);
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


    public BlockStateContainer getData() {
        return this;
    }


    public LightArray getBlockLight() {
        return this.blockLight;
    }


    public LightArray getSkyLight() {
        return this.skyLight;
    }


    public void setBlockLight(LightArray newBlocklightArray) {
        this.blockLight = newBlocklightArray;
    }


    public void setSkyLight(LightArray newSkylightArray) {
        this.skyLight = newSkylightArray;
    }
}