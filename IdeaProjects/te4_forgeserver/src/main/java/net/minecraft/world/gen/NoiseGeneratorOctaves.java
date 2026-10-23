package net.minecraft.world.gen;

import java.util.Random;
import net.minecraft.util.math.MathHelper;


public class NoiseGeneratorOctaves extends NoiseGenerator {
    private final NoiseGeneratorImproved[] generatorCollection;
    private final int octaves;

    public NoiseGeneratorOctaves(Random seed, int octavesIn) {
        this.octaves = octavesIn;
        this.generatorCollection = new NoiseGeneratorImproved[octavesIn];

        for (int i = 0; i < octavesIn; ++i)
            this.generatorCollection[i] = new NoiseGeneratorImproved(seed);
    }


//    public double[] generateNoiseOctaves(double[] noiseArray, int xOffset, int yOffset, int zOffset, int xSize, int ySize, int zSize, double xScale, double yScale, double zScale) {
//        synchronized (this){
//            return generateNoiseOctaves0(noiseArray, xOffset, yOffset, zOffset, xSize, ySize, zSize, xScale, yScale, zScale);
//        }
//    }


    public double[] generateNoiseOctaves(double[] noiseArray, int xOffset, int yOffset, int zOffset, int xSize, int ySize, int zSize, double xScale, double yScale, double zScale) {
        if (noiseArray == null)
            noiseArray = new double[xSize * ySize * zSize];
        else for (int i = 0; i < noiseArray.length; ++i)
            noiseArray[i] = 0;

        double d3 = 1.0D, d0, d2, xOffS = xOffset * xScale, zOffS = zOffset * zScale;
        long k, l;

        NoiseGeneratorImproved[] generatorCollection = this.generatorCollection;

        for (int j = 0, size = this.octaves; j < size; ++j) {
            k = MathHelper.lfloor(d0 = d3 * xOffS);
            l = MathHelper.lfloor(d2 = zOffS * d3);

            generatorCollection[j].populateNoiseArray(noiseArray,
                    (d0 - (double)k) + (double)k % 16777216L,
                    yOffset * d3 * yScale,
                    (d2 - (double)l) + (double)l % 16777216L, xSize, ySize, zSize,
                    xScale * d3,
                    yScale * d3,
                    zScale * d3, d3
            );
            d3 /= 2.0D;
        }

        return noiseArray;
    }


    public double[] generateNoiseOctaves(double[] noiseArray, int xOffset, int zOffset, int xSize, int zSize, double xScale, double zScale, double ignored) {
        //synchronized (this) {
        return generateNoiseOctaves(noiseArray, xOffset, 10, zOffset, xSize, 1, zSize, xScale, 1, zScale);
        //}
    }
}