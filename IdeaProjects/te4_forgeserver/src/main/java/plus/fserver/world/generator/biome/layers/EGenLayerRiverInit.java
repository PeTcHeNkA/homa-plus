package plus.fserver.world.generator.biome.layers;

import plus.fserver.world.generator.GenSession;


public class EGenLayerRiverInit extends EGenLayer {
    public EGenLayerRiverInit(long seed, EGenLayer parent) {
        super(seed);
        this.parent = parent;
    }


    @Override
    public int[] getInts(GenSession cache, int areaX, int areaY, int areaWidth, int areaHeight) {
        int[] ints = this.parent.getInts(cache, areaX, areaY, areaWidth, areaHeight);
        int[] intCache = cache.getIntCache(areaWidth * areaHeight);

        for(int i = 0; i < areaHeight; ++i) {
            for(int j = 0; j < areaWidth; ++j) {
                cache.initChunkSeed(this, j + areaX, i + areaY);
                intCache[j + i * areaWidth] = ints[j + i * areaWidth] > 0 ? cache.nextInt(299999) + 2 : 0;
            }
        }
        return intCache;
    }
}