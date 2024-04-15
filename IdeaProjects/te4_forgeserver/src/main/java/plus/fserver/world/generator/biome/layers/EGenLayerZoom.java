package plus.fserver.world.generator.biome.layers;

import plus.fserver.world.generator.biome.EIntCache;


public class EGenLayerZoom extends EGenLayer{
    public EGenLayerZoom(long seed, EGenLayer parent) {
        super(seed);
        super.parent = parent;
    }


    @Override
    public int[] getInts(EIntCache cache, int areaX, int areaY, int areaWidth, int areaHeight) {
        int i1 = areaX >> 1;
        int i2 = areaY >> 1;
        int i3 = (areaWidth >> 1) + 2;
        int i4 = (areaHeight >> 1) + 2;
        int[] ints = this.parent.getInts(cache, i1, i2, i3, i4);
        int i5 = i3 - 1 << 1;
        int i6 = i4 - 1 << 1;
        int[] intCache = cache.getIntCache(i5 * i6);

        int i7;
        for(int i = 0, n1, n2, n3, n4; i < i4 - 1; ++i) {
            i7 = (i << 1) * i5;
            n1 = 0;
            n2 = ints[n1 + (i) * i3];

            for(int j = ints[n1 + (i + 1) * i3]; n1 < i3 - 1; ++n1) {
                cache.initChunkSeed(this, (long)n1 + i1 << 1, (long)i + i2 << 1);
                n3 = ints[n1 + 1 + i * i3];
                n4 = ints[n1 + 1 + (i + 1) * i3];
                intCache[i7] = n2;
                intCache[i7++ + i5] = cache.selectRandom(n2, j);
                intCache[i7] = cache.selectRandom(n2, n3);
                intCache[i7++ + i5] = this.selectModeOrRandom(cache, n2, n3, j, n4);
                n2 = n3;
                j = n4;
            }
        }

        int[] intCache1 = cache.getIntCache(areaWidth * areaHeight);

        for(i7 = 0; i7 < areaHeight; ++i7) {
            System.arraycopy(intCache, (i7 + (areaY & 1)) * i5 + (areaX & 1), intCache1, i7 * areaWidth, areaWidth);
        }
        return intCache1;
    }


    protected int selectModeOrRandom(EIntCache cache, int j2, int l2, int k2, int i3) {
        return cache.selectModeOrRandom(j2, l2, k2, i3);
    }


    public static EGenLayer magnify(long seed, EGenLayer parent, int num) {
        EGenLayer genlayer = parent;
        for (int i = 0; i < num; ++i) genlayer = new EGenLayerZoom(seed + (long)i, genlayer);
        return genlayer;
    }
}