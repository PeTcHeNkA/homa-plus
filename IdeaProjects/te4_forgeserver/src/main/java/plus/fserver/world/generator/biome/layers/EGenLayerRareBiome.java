package plus.fserver.world.generator.biome.layers;

import plus.fserver.world.generator.biome.EIntCache;


public class EGenLayerRareBiome extends EGenLayer {
    public EGenLayerRareBiome(long seed, EGenLayer layer) {
        super(seed);
        this.parent = layer;
    }


    @Override
    public int[] getInts(EIntCache cache, int areaX, int areaY, int areaWidth, int areaHeight) {
        int[] ints = this.parent.getInts(cache, areaX - 1, areaY - 1, areaWidth + 2, areaHeight + 2);
        int[] intCache = cache.getIntCache(areaWidth * areaHeight);

        for(int i = 0, n1; i < areaHeight; ++i) {
            for(int j = 0; j < areaWidth; ++j) {
                cache.initChunkSeed(this, j + areaX, i + areaY);
                n1 = ints[j + 1 + (i + 1) * (areaWidth + 2)];
                if (cache.nextInt(57) == 0) {
                    if (n1 == 1) {//plants id
                        intCache[j + i * areaWidth] = 129;//mutated plants id
                    } else {
                        intCache[j + i * areaWidth] = n1;
                    }
                } else {
                    intCache[j + i * areaWidth] = n1;
                }
            }
        }
        return intCache;
    }
}