package net.minecraft.world.chunk;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockIDIndexes;
import java.util.Arrays;


public class ChunkPrimer {
    private final char[] data;

    public ChunkPrimer() {
        this.data = new char[65536];
    }


    public void clear(){
        Arrays.fill(data, (char) 0);
    }


    public IBlockState getBlockState(int x, int y, int z) {
        IBlockState state = BlockIDIndexes.BLOCK_INDEX.get(
                this.data[x << 12 | z << 8 | y]
        );
        if(state == null)return BlockIDIndexes.AIR_STATE;
        return state;
    }


    public char[] getRawData(){
        return data;
    }


    public static char getRawStateId(char[] data, int x, int y, int z){
        return data[x << 12 | z << 8 | y];
    }


    public static IBlockState getRawState(char[] data, int x, int y, int z, IBlockState[] rawIndex){
        int id = data[x << 12 | z << 8 | y];
        if(id > rawIndex.length)return BlockIDIndexes.AIR_STATE;
        return rawIndex[id];
    }


    public static void setRawState(char[] data, int x, int y, int z, char stateId){
        data[x << 12 | z << 8 | y] = stateId;
    }


    public int getBlockStateId(int x, int y, int z){
        return this.data[x << 12 | z << 8 | y];
    }


    public void setBlockState(int x, int y, int z, IBlockState state) {
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