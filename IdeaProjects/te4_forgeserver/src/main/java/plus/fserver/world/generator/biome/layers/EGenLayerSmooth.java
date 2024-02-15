package plus.fserver.world.generator.biome.layers;

import plus.fserver.world.generator.GenSession;


public class EGenLayerSmooth extends EGenLayer{
    public EGenLayerSmooth(long seed, EGenLayer parent) {
        super(seed);
        super.parent = parent;
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
                n1 = ints[j + (i + 1) * i3];
                n2 = ints[j + 2 + (i + 1) * i3];
                n3 = ints[j + 1 +  i      * i3];
                n4 = ints[j + 1 + (i + 2) * i3];
                n5 = ints[j + 1 + (i + 1) * i3];

                if (n1 == n2 && n3 == n4) {
                    cache.initChunkSeed(this, j + areaX, i + areaY);
                    if (cache.nextInt(2) == 0)
                        n5 = n1;
                    else
                        n5 = n3;
                } else {
                    if (n1 == n2) n5 = n1;
                    if (n3 == n4) n5 = n3;
                }
                intCache[j + i * areaWidth] = n5;
            }
        }
        return intCache;
    }
}