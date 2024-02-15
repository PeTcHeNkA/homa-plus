package plus.fserver.world.generator.biome.layers;

import plus.fserver.world.generator.GenSession;


public class EGenLayerVoronoiZoom extends EGenLayer{
    public EGenLayerVoronoiZoom(long seed, EGenLayer parent) {
        super(seed);
        super.parent = parent;
    }


    @Override
    public int[] getInts(GenSession cache, int areaX, int areaY, int areaWidth, int areaHeight) {
        areaX -= 2;
        areaY -= 2;
        int i1 = areaX >> 2;
        int i2 = areaY >> 2;
        int i3 = (areaWidth >> 2) + 2;
        int i4 = (areaHeight >> 2) + 2;
        int[] ints = this.parent.getInts(cache, i1, i2, i3, i4);
        int i5 = i3 - 1 << 2;
        int i6 = i4 - 1 << 2;
        int[] intCache = cache.getIntCache(i5 * i6);

        int i7;
        for(int i = 0; i < i4 - 1; ++i) {
            i7 = 0;
            int n1 = ints[i7 + i * i3];

            for(int j = ints[i7 + (i + 1) * i3]; i7 < i3 - 1; ++i7) {
                cache.initChunkSeed(this, (long) i7 + i1 << 2, (long) i + i2 << 2);
                double d1 = ((double)cache.nextInt(1024) / 1024.0 - 0.5) * 3.6;
                double d2 = ((double)cache.nextInt(1024) / 1024.0 - 0.5) * 3.6;
                cache.initChunkSeed(this, (long) i7 + i1 + 1 << 2, (long) i + i2 << 2);
                double d3 = ((double)cache.nextInt(1024) / 1024.0 - 0.5) * 3.6 + 4.0;
                double d4 = ((double)cache.nextInt(1024) / 1024.0 - 0.5) * 3.6;
                cache.initChunkSeed(this, (long) i7 + i1 << 2, (long) i + i2 + 1 << 2);
                double d5 = ((double)cache.nextInt(1024) / 1024.0 - 0.5) * 3.6;
                double d6 = ((double)cache.nextInt(1024) / 1024.0 - 0.5) * 3.6 + 4.0;
                cache.initChunkSeed(this, (long) i7 + i1 + 1 << 2, (long) i + i2 + 1 << 2);
                double d7 = ((double)cache.nextInt(1024) / 1024.0 - 0.5) * 3.6 + 4.0;
                double d8 = ((double)cache.nextInt(1024) / 1024.0 - 0.5) * 3.6 + 4.0;
                int n3 = ints[i7 + 1 + i * i3] & 255;
                int n4 = ints[i7 + 1 + (i + 1) * i3] & 255;

                for(int k = 0; k < 4; ++k) {
                    int n2 = ((i << 2) + k) * i5 + (i7 << 2);

                    for(int lvt_39_1_ = 0; lvt_39_1_ < 4; ++lvt_39_1_) {
                        double d9 = ((double)k - d2) * ((double)k - d2) + ((double)lvt_39_1_ - d1) * ((double)lvt_39_1_ - d1);
                        double d10 = ((double)k - d4) * ((double)k - d4) + ((double)lvt_39_1_ - d3) * ((double)lvt_39_1_ - d3);
                        double d11 = ((double)k - d6) * ((double)k - d6) + ((double)lvt_39_1_ - d5) * ((double)lvt_39_1_ - d5);
                        double d12 = ((double)k - d8) * ((double)k - d8) + ((double)lvt_39_1_ - d7) * ((double)lvt_39_1_ - d7);
                        if (d9 < d10 && d9 < d11 && d9 < d12) {
                            intCache[n2++] = n1;
                        } else if (d10 < d9 && d10 < d11 && d10 < d12) {
                            intCache[n2++] = n3;
                        } else if (d11 < d9 && d11 < d10 && d11 < d12) {
                            intCache[n2++] = j;
                        } else {
                            intCache[n2++] = n4;
                        }
                    }
                }
                n1 = n3;
                j = n4;
            }
        }
        int[] intCache1 = cache.getIntCache(areaWidth * areaHeight);

        for(i7 = 0; i7 < areaHeight; ++i7) {
            System.arraycopy(intCache, (i7 + (areaY & 3)) * i5 + (areaX & 3), intCache1, i7 * areaWidth, areaWidth);
        }
        return intCache1;
    }
}