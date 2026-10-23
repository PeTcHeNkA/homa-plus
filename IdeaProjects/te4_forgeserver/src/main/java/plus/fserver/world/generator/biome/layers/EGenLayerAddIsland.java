package plus.fserver.world.generator.biome.layers;

import plus.fserver.world.generator.biome.EIntCache;


public class EGenLayerAddIsland extends EGenLayer{
    public EGenLayerAddIsland(long seed, EGenLayer parent) {
        super(seed);
        this.parent = parent;
    }


    @Override
    public int[] getInts(EIntCache cache, int areaX, int areaY, int areaWidth, int areaHeight) {
        int i = areaX - 1, i1 = areaY - 1, i2 = areaWidth + 2, i3 = areaHeight + 2;

        int[] ints = this.parent.getInts(cache, i, i1, i2, i3);
        int[] ints1 = cache.getIntCache(areaWidth * areaHeight);

        for(int i4 = 0, n1, n2, n3, n4, n5, n6, n7; i4 < areaHeight; ++i4) {
            for(int i5 = 0; i5 < areaWidth; ++i5) {
                n1 = ints[i5 + (i4) * i2];
                n2 = ints[i5 + 2 + (i4) * i2];
                n3 = ints[i5 + (i4 + 2) * i2];
                n4 = ints[i5 + 2 + (i4 + 2) * i2];
                n5 = ints[i5 + 1 + (i4 + 1) * i2];

                cache.initChunkSeed(this, i5 + areaX, i4 + areaY);

                if (n5 == 0 && (n1 != 0 || n2 != 0 || n3 != 0 || n4 != 0)) {
                    n6 = 1;
                    n7 = 1;
                    if (n1 != 0 && cache.nextInt(n6++) == 0)
                        n7 = n1;

                    if (n2 != 0 && cache.nextInt(n6++) == 0)
                        n7 = n2;

                    if (n3 != 0 && cache.nextInt(n6++) == 0)
                        n7 = n3;

                    if (n4 != 0 && cache.nextInt(n6) == 0)
                        n7 = n4;

                    if (cache.nextInt(3) == 0)
                        ints1[i5 + i4 * areaWidth] = n7;
                    else if (n7 == 4)
                        ints1[i5 + i4 * areaWidth] = 4;
                    else
                        ints1[i5 + i4 * areaWidth] = 0;

                } else if (n5 > 0 && (n1 == 0 || n2 == 0 || n3 == 0 || n4 == 0)) {
                    if (cache.nextInt(5) == 0) {
                        if (n5 == 4)
                            ints1[i5 + i4 * areaWidth] = 4;
                        else
                            ints1[i5 + i4 * areaWidth] = 0;
                    } else {
                        ints1[i5 + i4 * areaWidth] = n5;
                    }
                } else {
                    ints1[i5 + i4 * areaWidth] = n5;
                }
            }
        }
        return ints1;
    }
}