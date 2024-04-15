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
    public double[] depthBuffer = new double[256];
    public final double[] heightMap;
    public Biome[] biomesForGeneration;
    public float[] biomeWeights;
    public final Random rand;

    public GenSession(){
        this(1001);
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
}