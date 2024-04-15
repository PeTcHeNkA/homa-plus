package plus.fserver.world.generator.biome.layers;

import plus.fserver.world.generator.biome.EIntCache;


public class EGenLayerAddSnow extends EGenLayer{
    public EGenLayerAddSnow(long seed, EGenLayer parent) {
        super(seed);
        this.parent = parent;
    }


    @Override
    public int[] getInts(EIntCache cache, int areaX, int areaY, int areaWidth, int areaHeight) {
        int i1 = areaX - 1;
        int i2 = areaY - 1;
        int i3 = areaWidth + 2;
        int i4 = areaHeight + 2;
        int[] ints = this.parent.getInts(cache, i1, i2, i3, i4);
        int[] intCache = cache.getIntCache(areaWidth * areaHeight);

        for(int i = 0; i < areaHeight; ++i) {
            for(int j = 0; j < areaWidth; ++j) {
                int n2 = ints[j + 1 + (i + 1) * i3];
                cache.initChunkSeed(this, j + areaX, i + areaY);
                if (n2 == 0) {
                    intCache[j + i * areaWidth] = 0;
                } else {
                    int n3 = cache.nextInt(6);
                    if (n3 == 0) {
                        n3 = 4;
                    } else if (n3 <= 1) {
                        n3 = 3;
                    } else {
                        n3 = 1;
                    }
                    intCache[j + i * areaWidth] = n3;
                }
            }
        }
        return intCache;
    }
}