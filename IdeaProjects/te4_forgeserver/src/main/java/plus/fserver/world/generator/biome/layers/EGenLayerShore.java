package plus.fserver.world.generator.biome.layers;

import net.minecraft.init.Biomes;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeJungle;
import net.minecraft.world.biome.BiomeMesa;
import plus.fserver.world.generator.biome.EIntCache;


public class EGenLayerShore extends EGenLayer{

    public EGenLayerShore(long seed, EGenLayer parent) {
        super(seed);
        this.parent = parent;
    }


    @Override
    public int[] getInts(EIntCache cache, int areaX, int areaY, int areaWidth, int areaHeight) {
        int[] ints = this.parent.getInts(cache, areaX - 1, areaY - 1, areaWidth + 2, areaHeight + 2);
        int[] intCache = cache.getIntCache(areaWidth * areaHeight);

        for(int i = 0, n1, n2, n3, n4; i < areaHeight; ++i) {
            for(int j = 0; j < areaWidth; ++j) {
                cache.initChunkSeed(this, j + areaX, i + areaY);
                int biomeId = ints[j + 1 + (i + 1) * (areaWidth + 2)];

                if (biomeId == ID_MUSHROOM_ISLAND) {
                    n1 = ints[j + 1 + (i + 1 - 1) * (areaWidth + 2)];
                    n2 = ints[j + 1 + 1 + (i + 1) * (areaWidth + 2)];
                    n3 = ints[j + 1 - 1 + (i + 1) * (areaWidth + 2)];
                    n4 = ints[j + 1 + (i + 1 + 1) * (areaWidth + 2)];
                    if (n1 != ID_OCEAN && n2 != ID_OCEAN && n3 != ID_OCEAN && n4 != ID_OCEAN) {
                        intCache[j + i * areaWidth] = biomeId;
                    } else {
                        intCache[j + i * areaWidth] = ID_MUSHROOM_ISLAND_SHORE;
                    }
                    //21, 22, 23, 149, 151 is jungle
                } else if (biomeId == 21 || biomeId == 22 || biomeId == 32 || biomeId == 159) {
                    n1 = ints[j + 1 + (i + 1 - 1) * (areaWidth + 2)];
                    n2 = ints[j + 1 + 1 + (i + 1) * (areaWidth + 2)];
                    n3 = ints[j + 1 - 1 + (i + 1) * (areaWidth + 2)];
                    n4 = ints[j + 1 + (i + 1 + 1) * (areaWidth + 2)];
                    if (this.isJungleCompatible(n1) && this.isJungleCompatible(n2) && this.isJungleCompatible(n3) && this.isJungleCompatible(n4)) {
                        if (!isBiomeOceanic(n1) && !isBiomeOceanic(n2) && !isBiomeOceanic(n3) && !isBiomeOceanic(n4)) {
                            intCache[j + i * areaWidth] = biomeId;
                        } else {
                            intCache[j + i * areaWidth] = ID_BEACH;
                        }
                    } else {
                        intCache[j + i * areaWidth] = ID_JUNGLE_EDGE;
                    }
                } else if (biomeId != ID_EXTREME_HILLS && biomeId != ID_EXTREME_HILLS_WITH_TREES && biomeId != ID_EXTREME_HILLS_EDGE) {
                    Biome biome = Biome.getBiome(biomeId);
                    if (biome != null && biome.isSnowyBiome()) {
                        this.replaceIfNeighborOcean(ints, intCache, j, i, areaWidth, biomeId, ID_COLD_BEACH);
                    } else if (biomeId != ID_MESA && biomeId != ID_MESA_ROCK) {
                        if (biomeId != ID_OCEAN && biomeId != ID_DEEP_OCEAN && biomeId != ID_RIVER && biomeId != ID_SWAMPLAND) {
                            n1 = ints[j + 1 + (i + 1 - 1) * (areaWidth + 2)];
                            n2 = ints[j + 1 + 1 + (i + 1) * (areaWidth + 2)];
                            n3 = ints[j + 1 - 1 + (i + 1) * (areaWidth + 2)];
                            n4 = ints[j + 1 + (i + 1 + 1) * (areaWidth + 2)];
                            if (!isBiomeOceanic(n1) && !isBiomeOceanic(n2) && !isBiomeOceanic(n3) && !isBiomeOceanic(n4)) {
                                intCache[j + i * areaWidth] = biomeId;
                            } else {
                                intCache[j + i * areaWidth] = ID_BEACH;
                            }
                        } else {
                            intCache[j + i * areaWidth] = biomeId;
                        }
                    } else {
                        n1 = ints[j + 1 + (i + 1 - 1) * (areaWidth + 2)];
                        n2 = ints[j + 1 + 1 + (i + 1) * (areaWidth + 2)];
                        n3 = ints[j + 1 - 1 + (i + 1) * (areaWidth + 2)];
                        n4 = ints[j + 1 + (i + 1 + 1) * (areaWidth + 2)];
                        if (!isBiomeOceanic(n1) && !isBiomeOceanic(n2) && !isBiomeOceanic(n3) && !isBiomeOceanic(n4)) {
                            if (this.isMesa(n1) && this.isMesa(n2) && this.isMesa(n3) && this.isMesa(n4)) {
                                intCache[j + i * areaWidth] = biomeId;
                            } else {
                                intCache[j + i * areaWidth] = ID_DESERT;
                            }
                        } else {
                            intCache[j + i * areaWidth] = biomeId;
                        }
                    }
                } else {
                    this.replaceIfNeighborOcean(ints, intCache, j, i, areaWidth, biomeId, ID_STONE_BEACH);
                }
            }
        }
        return intCache;
    }


    private void replaceIfNeighborOcean(int[] ints1, int[] ints2, int i1, int i2, int i3, int i4, int i6) {
        if (isBiomeOceanic(i4)) {
            ints2[i1 + i2 * i3] = i4;
        } else {
            int i = ints1[i1 + 1 + (i2 + 1 - 1) * (i3 + 2)];
            int j = ints1[i1 + 1 + 1 + (i2 + 1) * (i3 + 2)];
            int k = ints1[i1 + 1 - 1 + (i2 + 1) * (i3 + 2)];
            int l = ints1[i1 + 1 + (i2 + 1 + 1) * (i3 + 2)];

            if (!isBiomeOceanic(i) && !isBiomeOceanic(j) && !isBiomeOceanic(k) && !isBiomeOceanic(l)) {
                ints2[i1 + i2 * i3] = i4;
            } else {
                ints2[i1 + i2 * i3] = i6;
            }
        }
    }


    private boolean isJungleCompatible(int i) {
        Biome biome = Biome.getBiome(i);
        if (biome != null && biome.getBiomeClass() == BiomeJungle.class) {
            return true;
        } else {
            return i == ID_JUNGLE_EDGE ||
                    i == Biome.getIdForBiome(Biomes.JUNGLE) ||
                    i == Biome.getIdForBiome(Biomes.JUNGLE_HILLS) ||
                    i == Biome.getIdForBiome(Biomes.FOREST) ||
                    i == Biome.getIdForBiome(Biomes.TAIGA) || isBiomeOceanic(i);
        }
    }


    private boolean isMesa(int id) {
        return Biome.getBiome(id) instanceof BiomeMesa;
    }
}