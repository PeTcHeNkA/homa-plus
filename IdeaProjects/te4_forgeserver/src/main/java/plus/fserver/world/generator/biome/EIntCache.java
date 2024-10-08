package plus.fserver.world.generator.biome;

import com.google.common.collect.Lists;
import plus.fserver.world.generator.biome.layers.EGenLayer;
import java.util.List;


public class EIntCache implements IIntCache {
    private final List<int[]> freeSmallArrays = Lists.newArrayList();
    private final List<int[]> inUseSmallArrays = Lists.newArrayList();
    private final List<int[]> freeLargeArrays = Lists.newArrayList();
    private final List<int[]> inUseLargeArrays = Lists.newArrayList();
    private int intCacheSize = 256;
    private long chunkSeed;
    private long lworldGenSeed;

    public int[] getIntCache(int size) {
        if (size <= 256) {
            if (freeSmallArrays.isEmpty()) {
                int[] aint4 = new int[256];
                inUseSmallArrays.add(aint4);
                return aint4;
            } else {
                int[] aint3 = freeSmallArrays.remove(freeSmallArrays.size() - 1);
                inUseSmallArrays.add(aint3);
                return aint3;
            }
        } else if (size > intCacheSize) {
            intCacheSize = size;
            freeLargeArrays.clear();
            inUseLargeArrays.clear();
            int[] aint2 = new int[intCacheSize];
            inUseLargeArrays.add(aint2);
            return aint2;
        } else if (freeLargeArrays.isEmpty()) {
            int[] aint1 = new int[intCacheSize];
            inUseLargeArrays.add(aint1);
            return aint1;
        } else {
            int[] aint = freeLargeArrays.remove(freeLargeArrays.size() - 1);
            inUseLargeArrays.add(aint);
            return aint;
        }
    }


    @Override
    public void resetIntCache() {
        if (!freeLargeArrays.isEmpty()) {
            freeLargeArrays.remove(freeLargeArrays.size() - 1);
        }
        if (!freeSmallArrays.isEmpty()) {
            freeSmallArrays.remove(freeSmallArrays.size() - 1);
        }
        freeLargeArrays.addAll(inUseLargeArrays);
        freeSmallArrays.addAll(inUseSmallArrays);
        inUseLargeArrays.clear();
        inUseSmallArrays.clear();
    }

    public void initChunkSeed(EGenLayer layer, long num, long num2) {
        this.chunkSeed = lworldGenSeed = layer.worldGenSeed;
        this.chunkSeed *= this.chunkSeed * 6364136223846793005L + 1442695040888963407L;
        this.chunkSeed += num;
        this.chunkSeed *= this.chunkSeed * 6364136223846793005L + 1442695040888963407L;
        this.chunkSeed += num2;
        this.chunkSeed *= this.chunkSeed * 6364136223846793005L + 1442695040888963407L;
        this.chunkSeed += num;
        this.chunkSeed *= this.chunkSeed * 6364136223846793005L + 1442695040888963407L;
        this.chunkSeed += num2;
    }


    public int selectRandom(int i1, int i2){
        return nextInt(2)==1?i1:i2;
    }


    public int nextInt(int max) {
        int i = (int)((this.chunkSeed >> 24) % (long)max);
        if (i < 0) i += max;
        this.chunkSeed *= this.chunkSeed * 6364136223846793005L + 1442695040888963407L;
        this.chunkSeed += lworldGenSeed;
        return i;
    }


    public int selectRandom(int... nums) {
        return nums[this.nextInt(nums.length)];
    }


    public int selectRandom(int num1, int num2, int num3, int num4) {
        switch (this.nextInt(4)){
            case 0:return num1;
            case 1:return num2;
            case 3:return num3;
            default:return num4;
        }
    }


    public int selectRandomNum(int[] nums) {
        return nums[this.nextInt(nums.length)];
    }


    public int selectModeOrRandom(int num1, int num2, int num3, int num4) {
        if (num2 == num3 && num3 == num4) {
            return num2;
        } else if (num1 == num2 && num1 == num3) {
            return num1;
        } else if (num1 == num2 && num1 == num4) {
            return num1;
        } else if (num1 == num3 && num1 == num4) {
            return num1;
        } else if (num1 == num2 && num3 != num4) {
            return num1;
        } else if (num1 == num3 && num2 != num4) {
            return num1;
        } else if (num1 == num4 && num2 != num3) {
            return num1;
        } else if (num2 == num3 && num1 != num4) {
            return num2;
        } else if (num2 == num4 && num1 != num3) {
            return num2;
        } else {
            return num3 == num4 && num1 != num2 ? num3 : this.selectRandom(num1, num2, num3, num4);
        }
    }
}