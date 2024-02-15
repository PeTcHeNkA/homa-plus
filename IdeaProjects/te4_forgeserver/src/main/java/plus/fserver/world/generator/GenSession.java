package plus.fserver.world.generator;

import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;
import plus.fserver.world.generator.biome.EIntCache;
import plus.fserver.world.generator.biome.layers.EGenLayer;
import java.util.Random;


public final class GenSession extends EIntCache{
    public double[] mainNoiseRegion;
    public double[] minLimitRegion;
    public double[] maxLimitRegion;
    public double[] depthRegion;
    public Biome[] biomesForGeneration;
    public double[] depthBuffer = new double[256];
    public final double[] heightMap;
    public final float[] biomeWeights;
    public final Random rand;

    private long chunkSeed;
    private long lworldGenSeed;

    public GenSession(){
        rand = new Random(1001);
        this.heightMap = new double[825];
        this.biomeWeights = new float[25];
    }

    public GenSession(long seed){
        rand = new Random(seed);
        this.heightMap = new double[825];
        this.biomeWeights = new float[25];

        for (int i = -2; i <= 2; ++i) {
            for (int j = -2; j <= 2; ++j) {
                float f = 10.0F / MathHelper.sqrt((float)(i * i + j * j) + 0.2F);
                this.biomeWeights[i + 2 + (j + 2) * 5] = f;
            }
        }
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