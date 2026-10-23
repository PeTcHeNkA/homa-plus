package plus.fserver.world.generator.biome.layers;

import net.minecraft.init.Biomes;
import net.minecraft.world.biome.Biome;
import plus.fserver.api.BiomeIndexes;
import plus.fserver.world.generator.biome.EIntCache;


public class EGenLayerBiomeEdge extends EGenLayer {
    public EGenLayerBiomeEdge(long seed, EGenLayer parent) {
        super(seed);
        this.parent = parent;
    }


    @Override
    public int[] getInts(EIntCache cache, int areaX, int areaY, int areaWidth, int areaHeight) {
        int[] ints = this.parent.getInts(cache, areaX - 1, areaY - 1, areaWidth + 2, areaHeight + 2);
        int[] intCache = cache.getIntCache(areaWidth * areaHeight);

        for(int i = 0; i < areaHeight; ++i) {
            for(int j = 0; j < areaWidth; ++j) {
                cache.initChunkSeed(this, j + areaX, i + areaY);
                int n1 = ints[j + 1 + (i + 1) * (areaWidth + 2)];
                if (!this.replaceBiomeEdgeIfNecessary(ints, intCache, j, i, areaWidth, n1, ID_EXTREME_HILLS, ID_EXTREME_HILLS_EDGE)
                        && !this.replaceBiomeEdge(ints, intCache, j, i, areaWidth, n1, ID_MESA_ROCK, ID_MESA)
                        && !this.replaceBiomeEdge(ints, intCache, j, i, areaWidth, n1, ID_MESA_CLEAR_ROCK, ID_MESA)
                        && !this.replaceBiomeEdge(ints, intCache, j, i, areaWidth, n1, ID_REDWOOD_TAIGA, ID_TAIGA)) {
                    int n2;
                    int n3;
                    int n4;
                    int n5;
                    if (n1 == ID_DESERT) {
                        n2 = ints[j + 1 + (i + 1 - 1) * (areaWidth + 2)];
                        n3 = ints[j + 1 + 1 + (i + 1) * (areaWidth + 2)];
                        n4 = ints[j + 1 - 1 + (i + 1) * (areaWidth + 2)];
                        n5 = ints[j + 1 + (i + 1 + 1) * (areaWidth + 2)];
                        if (n2 != ID_ICE_PLANTS && n3 != ID_ICE_PLANTS && n4 != ID_ICE_PLANTS && n5 != ID_ICE_PLANTS) {
                            intCache[j + i * areaWidth] = n1;
                        } else {
                            intCache[j + i * areaWidth] = ID_EXTREME_HILLS_WITH_TREES;
                        }
                    } else if (n1 == ID_SWAMPLAND) {
                        n2 = ints[j + 1 + (i + 1 - 1) * (areaWidth + 2)];
                        n3 = ints[j + 1 + 1 + (i + 1) * (areaWidth + 2)];
                        n4 = ints[j + 1 - 1 + (i + 1) * (areaWidth + 2)];
                        n5 = ints[j + 1 + (i + 1 + 1) * (areaWidth + 2)];
                        if(n2 != ID_DESERT && n3 != ID_DESERT && n4 != ID_DESERT && n5 != ID_DESERT
                                && n2 != ID_COLD_TAIGA && n3 != ID_COLD_TAIGA && n4 != ID_COLD_TAIGA && n5 != ID_COLD_TAIGA
                                && n2 != ID_ICE_PLANTS && n3 != ID_ICE_PLANTS && n4 != ID_ICE_PLANTS && n5 != ID_ICE_PLANTS) {
                            if (n2 != ID_JUNGLE && n5 != ID_JUNGLE && n3 != ID_JUNGLE && n4 != ID_JUNGLE) {
                                intCache[j + i * areaWidth] = n1;
                            } else {
                                intCache[j + i * areaWidth] = Biome.getIdForBiome(Biomes.JUNGLE_EDGE);
                            }
                        } else {
                            intCache[j + i * areaWidth] = Biome.getIdForBiome(Biomes.PLAINS);
                        }
                    } else {
                        intCache[j + i * areaWidth] = n1;
                    }
                }
            }
        }

        return intCache;
    }


    private boolean replaceBiomeEdgeIfNecessary(int[] ints1, int[] ints2, int i1, int i2, int i3, int i4, int i5, int i6) {
        if (!biomesEqualOrMesaPlateau(i4, i5)) {
            return false;
        } else {
            int i = ints1[i1 + 1 + (i2 + 1 - 1) * (i3 + 2)];
            int j = ints1[i1 + 1 + 1 + (i2 + 1) * (i3 + 2)];
            int k = ints1[i1 + 1 - 1 + (i2 + 1) * (i3 + 2)];
            int l = ints1[i1 + 1 + (i2 + 1 + 1) * (i3 + 2)];

            if (this.canBiomesBeNeighbors(i, i5) && this.canBiomesBeNeighbors(j, i5) &&
                    this.canBiomesBeNeighbors(k, i5) && this.canBiomesBeNeighbors(l, i5)) {
                ints2[i1 + i2 * i3] = i4;
            } else {
                ints2[i1 + i2 * i3] = i6;
            }
            return true;
        }
    }


    private boolean replaceBiomeEdge(int[] ints1, int[] ints2, int i1, int i2, int i3, int i4, int i5, int i6) {
        if (i4 != i5) {
            return false;
        } else {
            int i = ints1[i1 + 1 + (i2 + 1 - 1) * (i3 + 2)];
            int j = ints1[i1 + 1 + 1 + (i2 + 1) * (i3 + 2)];
            int k = ints1[i1 + 1 - 1 + (i2 + 1) * (i3 + 2)];
            int l = ints1[i1 + 1 + (i2 + 1 + 1) * (i3 + 2)];

            if (biomesEqualOrMesaPlateau(i, i5) && biomesEqualOrMesaPlateau(j, i5)
                    && biomesEqualOrMesaPlateau(k, i5) && biomesEqualOrMesaPlateau(l, i5)) {
                ints2[i1 + i2 * i3] = i4;
            } else {
                ints2[i1 + i2 * i3] = i6;
            }
            return true;
        }
    }


    private boolean canBiomesBeNeighbors(int id1, int id2) {
        if (biomesEqualOrMesaPlateau(id1, id2)) {
            return true;
        } else {
            Biome biome = BiomeIndexes.get(id1);
            Biome biome1 = BiomeIndexes.get(id2);

            if (biome != null && biome1 != null) {
                Biome.TempCategory category = biome.getTempCategory();
                Biome.TempCategory category1 = biome1.getTempCategory();
                return category == category1 || category == Biome.TempCategory.MEDIUM ||
                        category1 == Biome.TempCategory.MEDIUM;
            } else {
                return false;
            }
        }
    }
}