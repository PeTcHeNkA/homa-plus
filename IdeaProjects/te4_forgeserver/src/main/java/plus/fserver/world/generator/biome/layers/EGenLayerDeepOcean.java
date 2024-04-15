package plus.fserver.world.generator.biome.layers;

import net.minecraft.init.Biomes;
import net.minecraft.world.biome.Biome;
import plus.fserver.world.generator.biome.EIntCache;


public class EGenLayerDeepOcean extends EGenLayer{
    private final int id;

    public EGenLayerDeepOcean(long seed, EGenLayer parent) {
        super(seed);
        this.parent = parent;
        id = Biome.getIdForBiome(Biomes.DEEP_OCEAN);
    }


    @Override
    public int[] getInts(EIntCache cache, int areaX, int areaY, int areaWidth, int areaHeight) {
        int i1 = areaX - 1, i2 = areaY - 1, i3 = areaWidth + 2, i4 = areaHeight + 2;

        int[] ints = this.parent.getInts(cache, i1, i2, i3, i4);
        int[] intCache = cache.getIntCache(areaWidth * areaHeight);

        for(int i = 0, n1, n2, n3, n4, n5, n6; i < areaHeight; ++i) {
            for(int i5 = 0; i5 < areaWidth; ++i5) {
                n1 = ints[i5 + 1 + (i + 1 - 1) * (areaWidth + 2)];
                n2 = ints[i5 + 1 + 1 + (i + 1) * (areaWidth + 2)];
                n3 = ints[i5 + 1 - 1 + (i + 1) * (areaWidth + 2)];
                n4 = ints[i5 + 1 + (i + 1 + 1) * (areaWidth + 2)];
                n5 = ints[i5 + 1 + (i + 1) * i3];
                n6 = 0;

                if (n1 == 0) {
                    ++n6;
                }
                if (n2 == 0) {
                    ++n6;
                }
                if (n3 == 0) {
                    ++n6;
                }
                if (n4 == 0) {
                    ++n6;
                }
                if (n5 == 0 && n6 > 3) {
                    intCache[i5 + i * areaWidth] = id;
                } else {
                    intCache[i5 + i * areaWidth] = n5;
                }
            }
        }
        return intCache;
    }
}