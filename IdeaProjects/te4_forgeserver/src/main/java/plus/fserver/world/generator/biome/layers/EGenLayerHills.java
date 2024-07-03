package plus.fserver.world.generator.biome.layers;

import net.minecraft.init.Biomes;
import net.minecraft.world.biome.Biome;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import plus.fserver.api.BiomeIndexes;
import plus.fserver.world.generator.biome.EIntCache;


public class EGenLayerHills extends EGenLayer {
    private static final Logger LOGGER = LogManager.getLogger();
    private final EGenLayer riverLayer;

    public EGenLayerHills(long seed, EGenLayer parent, EGenLayer rivGen) {
        super(seed);
        this.parent = parent;
        this.riverLayer = rivGen;
    }


    @Override
    public int[] getInts(EIntCache cache, int areaX, int areaY, int areaWidth, int areaHeight) {
        int[] ints = this.parent.getInts(cache, areaX - 1, areaY - 1, areaWidth + 2, areaHeight + 2);
        int[] ints1 = this.riverLayer.getInts(cache, areaX - 1, areaY - 1, areaWidth + 2, areaHeight + 2);
        int[] intCache = cache.getIntCache(areaWidth * areaHeight);

        for(int i = 0; i < areaHeight; ++i) {
            for(int j = 0; j < areaWidth; ++j) {
                cache.initChunkSeed(this, (long)(j + areaX), (long)(i + areaY));
                int biomeId    = ints[j + 1 + (i + 1) * (areaWidth + 2)];
                int biomeId2   = ints1[j + 1 + (i + 1) * (areaWidth + 2)];
                boolean flag = (biomeId2 - 2) % 29 == 0;
                if (biomeId > 255) {
                    LOGGER.debug("old! {}", biomeId);
                }

                Biome biome = BiomeIndexes.get(biomeId);
                boolean flag2 = biome != null && biome.isMutation();
                Biome mutated;
                if (biomeId != 0 && biomeId2 >= 2 && (biomeId2 - 2) % 29 == 1 && !flag2) {
                    mutated = Biome.getMutationForBiome(biome);
                    intCache[j + i * areaWidth] = mutated == null ? biomeId : Biome.getIdForBiome(mutated);
                } else if (cache.nextInt(3) != 0 && !flag) {
                    intCache[j + i * areaWidth] = biomeId;
                } else {
                    mutated = biome;
                    int biomeId3;
                    if (biome == Biomes.DESERT) {
                        mutated = Biomes.DESERT_HILLS;
                    } else if (biome == Biomes.FOREST) {
                        mutated = Biomes.FOREST_HILLS;
                    } else if (biome == Biomes.BIRCH_FOREST) {
                        mutated = Biomes.BIRCH_FOREST_HILLS;
                    } else if (biome == Biomes.ROOFED_FOREST) {
                        mutated = Biomes.PLAINS;
                    } else if (biome == Biomes.TAIGA) {
                        mutated = Biomes.TAIGA_HILLS;
                    } else if (biome == Biomes.REDWOOD_TAIGA) {
                        mutated = Biomes.REDWOOD_TAIGA_HILLS;
                    } else if (biome == Biomes.COLD_TAIGA) {
                        mutated = Biomes.COLD_TAIGA_HILLS;
                    } else if (biome == Biomes.PLAINS) {
                        if (cache.nextInt(3) == 0) {
                            mutated = Biomes.FOREST_HILLS;
                        } else {
                            mutated = Biomes.FOREST;
                        }
                    } else if (biome == Biomes.ICE_PLAINS) {
                        mutated = Biomes.ICE_MOUNTAINS;
                    } else if (biome == Biomes.JUNGLE) {
                        mutated = Biomes.JUNGLE_HILLS;
                    } else if (biome == Biomes.OCEAN) {
                        mutated = Biomes.DEEP_OCEAN;
                    } else if (biome == Biomes.EXTREME_HILLS) {
                        mutated = Biomes.EXTREME_HILLS_WITH_TREES;
                    } else if (biome == Biomes.SAVANNA) {
                        mutated = Biomes.SAVANNA_PLATEAU;
                    } else if (biomesEqualOrMesaPlateau(biomeId, Biome.getIdForBiome(Biomes.MESA_ROCK))) {
                        mutated = Biomes.MESA;
                    } else if (biome == Biomes.DEEP_OCEAN && cache.nextInt(3) == 0) {
                        biomeId3 = cache.nextInt(2);
                        if (biomeId3 == 0) {
                            mutated = Biomes.PLAINS;
                        } else {
                            mutated = Biomes.FOREST;
                        }
                    }

                    biomeId3 = Biome.getIdForBiome(mutated);
                    if (flag && biomeId3 != biomeId) {
                        Biome mutated3 = Biome.getMutationForBiome(mutated);
                        biomeId3 = mutated3 == null ? biomeId : Biome.getIdForBiome(mutated3);
                    }

                    if (biomeId3 == biomeId) {
                        intCache[j + i * areaWidth] = biomeId;
                    } else {
                        int n1 = ints[j + 1 + i * (areaWidth + 2)];
                        int n2 = ints[j + 2 + (i + 1) * (areaWidth + 2)];
                        int n3 = ints[j + (i + 1) * (areaWidth + 2)];
                        int n4 = ints[j + 1 + (i + 2) * (areaWidth + 2)];
                        int n5 = 0;

                        if (biomesEqualOrMesaPlateau(n1, biomeId)) {
                            ++n5;
                        }
                        if (biomesEqualOrMesaPlateau(n2, biomeId)) {
                            ++n5;
                        }
                        if (biomesEqualOrMesaPlateau(n3, biomeId)) {
                            ++n5;
                        }
                        if (biomesEqualOrMesaPlateau(n4, biomeId)) {
                            ++n5;
                        }
                        if (n5 >= 3) {
                            intCache[j + i * areaWidth] = biomeId3;
                        } else {
                            intCache[j + i * areaWidth] = biomeId;
                        }
                    }
                }
            }
        }

        return intCache;
    }
}