package plus.fserver.world.generator.biome.layers;

import net.minecraft.init.Biomes;
import net.minecraft.world.biome.Biome;
import plus.fserver.world.generator.GenSession;


public class EGenLayerRiverPlus extends EGenLayer {
    private final int riverBiomeId;

    public EGenLayerRiverPlus(long seed, EGenLayer parent, Biome biome) {
        super(seed);
        super.parent = parent;
        this.riverBiomeId = Biome.getIdForBiome(biome);
    }


    public EGenLayerRiverPlus(long seed, EGenLayer parent, int biome) {
        super(seed);
        super.parent = parent;
        this.riverBiomeId = biome;
    }


    @Override
    public int[] getInts(GenSession cache, int p_75904_1_, int p_75904_2_, int p_75904_3_, int p_75904_4_) {
        int i1 = p_75904_1_ - 1;
        int i2 = p_75904_2_ - 1;
        int i3 = p_75904_3_ + 2;
        int i4 = p_75904_4_ + 2;
        int[] ints = this.parent.getInts(cache, i1, i2, i3, i4);
        int[] intCache = cache.getIntCache(p_75904_3_ * p_75904_4_);

        for(int i = 0, n1, n2, n3, n4,n5; i < p_75904_4_; ++i) {
            for(int j = 0; j < p_75904_3_; ++j) {
                n1 = this.riverFilter(ints[j + (i + 1) * i3]);
                n2 = this.riverFilter(ints[j + 2 + (i + 1) * i3]);
                n3 = this.riverFilter(ints[j + 1 +  i      * i3]);
                n4 = this.riverFilter(ints[j + 1 + (i + 2) * i3]);
                n5 = this.riverFilter(ints[j + 1 + (i + 1) * i3]);
                if (n5 == n1 && n5 == n3 && n5 == n2 && n5 == n4) {
                    intCache[j + i * p_75904_3_] = -1;
                } else {
                    intCache[j + i * p_75904_3_] = Biome.getIdForBiome(Biomes.RIVER);
                }
            }
        }
        return intCache;
    }


    private int riverFilter(int num) {
        return num >= 2 ? 2 + (num & 1) : num;
    }
}