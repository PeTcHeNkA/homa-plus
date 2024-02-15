package plus.fserver.world.generator.biome.layers;

import net.minecraft.world.gen.layer.GenLayerEdge;
import plus.fserver.world.generator.GenSession;


public class EGenLayerEdge extends EGenLayer{
    private final GenLayerEdge.Mode mode;

    public EGenLayerEdge(long seed, EGenLayer parent, GenLayerEdge.Mode mode) {
        super(seed);
        this.parent = parent;
        this.mode = mode;
    }


    @Override
    public int[] getInts(GenSession cache, int areaX, int areaY, int areaWidth, int areaHeight) {
        switch (this.mode) {
            case COOL_WARM:
            default:
                return this.getIntsCoolWarm(cache, areaX, areaY, areaWidth, areaHeight);
            case HEAT_ICE:
                return this.getIntsHeatIce(cache, areaX, areaY, areaWidth, areaHeight);
            case SPECIAL:
                return this.getIntsSpecial(cache, areaX, areaY, areaWidth, areaHeight);
        }
    }


    private int[] getIntsCoolWarm(GenSession cache, int aX, int aZ, int aW, int aH) {
        int i1 = aX - 1;
        int i2 = aZ - 1;
        int i3 = 1 + aW + 1;
        int i4 = 1 + aH + 1;
        int[] ints = this.parent.getInts(cache, i1, i2, i3, i4);
        int[] intCache = cache.getIntCache(aW * aH);

        for(int i = 0, n1, n2, n3, n4, n5; i < aH; ++i) {
            for(int j = 0; j < aW; ++j) {
                cache.initChunkSeed(this, j + aX, i + aZ);
                n1 = ints[j + 1 + (i + 1) * i3];
                if (n1 == 1) {
                    n2 = ints[j + 1 + (i + 1 - 1) * i3];
                    n3 = ints[j + 1 + 1 + (i + 1) * i3];
                    n4 = ints[j + 1 - 1 + (i + 1) * i3];
                    n5 = ints[j + 1 + (i + 1 + 1) * i3];

                    if (
                            (n2 == 3 || n3 == 3 || n4 == 3 || n5 == 3) ||
                            (n2 == 4 || n3 == 4 || n4 == 4 || n5 == 4)
                    ) {
                        n1 = 2;
                    }
                }
                intCache[j + i * aW] = n1;
            }
        }
        return intCache;
    }


    private int[] getIntsHeatIce(GenSession cache, int aX, int aZ, int aW, int aH) {
        int i1 = aX - 1;
        int i2 = aZ - 1;
        int i3 = 1 + aW + 1;
        int i4 = 1 + aH + 1;
        int[] ints = this.parent.getInts(cache, i1, i2, i3, i4);
        int[] intCache = cache.getIntCache(aW * aH);

        for(int i = 0, n1, n2, n3, n4, n5; i < aH; ++i) {
            for(int j = 0; j < aW; ++j) {
                n1 = ints[j + 1 + (i + 1) * i3];
                if (n1 == 4) {
                    n2 = ints[j + 1 + (i + 1 - 1) * i3];
                    n3 = ints[j + 1 + 1 + (i + 1) * i3];
                    n4 = ints[j + 1 - 1 + (i + 1) * i3];
                    n5 = ints[j + 1 + (i + 1 + 1) * i3];
                    if (
                            (n2 == 2 || n3 == 2 || n4 == 2 || n5 == 2) ||
                            (n2 == 1 || n3 == 1 || n4 == 1 || n5 == 1)
                    ) {
                        n1 = 3;
                    }
                }
                intCache[j + i * aW] = n1;
            }
        }
        return intCache;
    }


    private int[] getIntsSpecial(GenSession cache, int aX, int aZ, int aW, int aH) {
        int[] ints = this.parent.getInts(cache, aX, aZ, aW, aH);
        int[] intCache = cache.getIntCache(aW * aH);

        for(int i = 0, n1; i < aH; ++i) {
            for(int j = 0; j < aW; ++j) {
                cache.initChunkSeed(this, j + aX, i + aZ);

                n1 = ints[j + i * aW];
                if (n1 != 0 && cache.nextInt(13) == 0) {
                    n1 |= 1 + cache.nextInt(15) << 8 & 3840;
                }
                intCache[j + i * aW] = n1;
            }
        }
        return intCache;
    }
}