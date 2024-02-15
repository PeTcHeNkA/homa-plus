package plus.fserver.world.generator.biome.layers;

import plus.fserver.world.generator.GenSession;


public class EGenLayerIsland extends EGenLayer {
    public EGenLayerIsland(long seed) {
        super(seed);
    }


    @Override
    public int[] getInts(GenSession cache, int areaX, int areaY, int areaWidth, int areaHeight) {
        int[] intCache = cache.getIntCache(areaWidth * areaHeight);

        for(int i = 0; i < areaHeight; ++i) {
            for(int j = 0; j < areaWidth; ++j) {
                cache.initChunkSeed(this, areaX + j, areaY + i);
                intCache[j + i * areaWidth] = cache.nextInt(10) == 0 ? 1 : 0;
            }
        }

        if (areaX > -areaWidth && areaX <= 0 && areaY > -areaHeight && areaY <= 0) {
            intCache[-areaX + -areaY * areaWidth] = 1;
        }
        return intCache;
    }
}