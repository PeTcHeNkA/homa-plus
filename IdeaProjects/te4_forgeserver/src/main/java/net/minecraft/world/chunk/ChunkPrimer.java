package net.minecraft.world.chunk;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockIDIndexes;


public class ChunkPrimer {
    private final char[] data = new char[65536];

    public ChunkPrimer() {}


    public IBlockState getBlockState(int x, int y, int z) {
        IBlockState state = BlockIDIndexes.BLOCK_INDEX.get(
                //this.data[getBlockIndex(x, y, z)]
                this.data[x << 12 | z << 8 | y]
        );
        return state == null ? BlockIDIndexes.AIR_STATE : state;
    }


//    public IBlockState getBlockState(int x, int y, int z)
//    {
//        IBlockState iblockstate = Block.BLOCK_STATE_IDS.getByValue(this.data[getBlockIndex(x, y, z)]);
//        return iblockstate == null ? DEFAULT_STATE : iblockstate;
//    }

    public int getBlockStateId(int x, int y, int z){
        return this.data[x << 12 | z << 8 | y];
    }


    public void setBlockState(int x, int y, int z, IBlockState state) {
        //this.data[getBlockIndex(x, y, z)] = (char)state.getId();
        this.data[x << 12 | z << 8 | y] = (char)state.getId();
    }


    private static int getBlockIndex(int x, int y, int z) {
        return x << 12 | z << 8 | y;
    }


    public int findGroundBlockIdx(int x, int z) {
        final int i = x << 12 | z << 8;
        int low = 0;
        int high = 255;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            IBlockState state = BlockIDIndexes.BLOCK_INDEX.get(this.data[i | mid]);
            if (state != null && state.isFullBlock()) {
                low = mid + 1;
            } else high = mid - 1;
        }
        return low>0?low-1:0;
    }
}