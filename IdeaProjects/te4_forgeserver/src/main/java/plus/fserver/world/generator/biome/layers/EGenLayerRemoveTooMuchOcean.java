package plus.fserver.world.generator.biome.layers;

import plus.fserver.world.generator.GenSession;


public class EGenLayerRemoveTooMuchOcean extends EGenLayer{
    public EGenLayerRemoveTooMuchOcean(long seed, EGenLayer parent) {
        super(seed);
        this.parent = parent;
    }


    @Override
    public int[] getInts(GenSession cache, int areaX, int areaY, int areaWidth, int areaHeight) {
        int i1 = areaX - 1;
        int i2 = areaY - 1;
        int i3 = areaWidth + 2;
        int i4 = areaHeight + 2;
        int[] ints = this.parent.getInts(cache, i1, i2, i3, i4);
        int[] intCache = cache.getIntCache(areaWidth * areaHeight);

        for(int i = 0, n1, n2, n3, n4, n5; i < areaHeight; ++i) {
            for(int j = 0; j < areaWidth; ++j) {
                n1 = ints[j + 1 + (i + 1 - 1) * (areaWidth + 2)];
                n2 = ints[j + 1 + 1 + (i + 1) * (areaWidth + 2)];
                n3 = ints[j + 1 - 1 + (i + 1) * (areaWidth + 2)];
                n4 = ints[j + 1 + (i + 1 + 1) * (areaWidth + 2)];
                n5 = ints[j + 1 + (i + 1) * i3];
                intCache[j + i * areaWidth] = n5;

                cache.initChunkSeed(this, j + areaX, i + areaY);

                if (n5 == 0 && n1 == 0 && n2 == 0 && n3 == 0 && n4 == 0 && cache.nextInt(2) == 0) {
                    intCache[j + i * areaWidth] = 1;
                }
            }
        }
        return intCache;
    }
}