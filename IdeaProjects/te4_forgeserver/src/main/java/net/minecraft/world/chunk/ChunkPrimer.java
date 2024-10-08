package net.minecraft.world.chunk;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockIDIndexes;
import org.jetbrains.annotations.Nullable;
import plus.fserver.ElcaConfig;

import java.util.Arrays;

import static io.netty.util.internal.shaded.org.jctools.util.UnsafeAccess.UNSAFE;


public class ChunkPrimer {
    //private static final ChunkPrimer[] temp;
    //private static final long tempShift;
    //private static volatile int cursor = 0;
    private char[] data;

//    static {
//        temp = new ChunkPrimer[ElcaConfig.CACHE_CHUNK_PRIMER];
//        for (int i = 0; i < temp.length; i++){
//            temp[i] = new ChunkPrimer(null);
//        }
//        try {
//            tempShift = UNSAFE.objectFieldOffset(ChunkPrimer.class.getDeclaredField("data"));
//        } catch (NoSuchFieldException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//
//    private static char[] alloc() {
//        ChunkPrimer[] nodes = temp;
//        for (int i = cursor, s = nodes.length; i < s; i++){
//            ChunkPrimer node = nodes[i];
//            char[] result;
//            if((result = node.tryGet()) != null){
//                cursor = i;
//                return result;
//            }
//        }
//        for (int i = 0, s = cursor; i < s; i++){
//            ChunkPrimer node = nodes[i];
//            char[] result;
//            if((result = node.tryGet()) != null){
//                cursor = i;
//                return result;
//            }
//        }
//        return new char[65536];
//    }
//
//
//    private static void free(char[] src){
//        ChunkPrimer[] nodes = temp;
//        for (int i = cursor, s = nodes.length; i < s; i++) {
//            ChunkPrimer node = nodes[i];
//            if(node.tryFree(src)){
//                cursor = i;
//                return;
//            }
//        }
//        for (int i = 0, s = cursor; i < s; i++) {
//            ChunkPrimer node = nodes[i];
//            if(node.tryFree(src)){
//                cursor = i;
//                return;
//            }
//        }
//    }
//
//
//    public static void freeMemory(){
//        for (ChunkPrimer primer:temp) primer.data = null;
//    }
//
//
//    ChunkPrimer(char[] data){
//        this.data = data;
//    }
//
//
//    public static ChunkPrimer create(){
//        return new ChunkPrimer(alloc());
//    }


    public ChunkPrimer() {
        this.data = new char[65536];
    }


    public void clear(){
        Arrays.fill(data, (char) 0);
    }

//
//
//    @Nullable
//    private char[] tryGet(){
//        char[] temp = this.data;
//        if(temp == null)return null;
//        if(UNSAFE.compareAndSwapObject(this, tempShift, temp, null)) return temp;
//        return null;
//    }
//
//
//    private boolean tryFree(char[] src){
//        char[] temp = this.data;
//        if(temp == null) {
//            return UNSAFE.compareAndSwapObject(this, tempShift, null, src);
//        }
//        return false;
//    }


    public IBlockState getBlockState(int x, int y, int z) {
        IBlockState state = BlockIDIndexes.BLOCK_INDEX.get(
                this.data[x << 12 | z << 8 | y]
        );
        if(state == null)return BlockIDIndexes.AIR_STATE;
        return state;
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
//
//
//    public void free(){
//        char[] data;
//        free(data = this.data);
//        this.data = null;
//    }
}