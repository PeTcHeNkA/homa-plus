package plus.fserver.world.generator.biome.layers;

import com.google.common.collect.ImmutableList;
import net.minecraft.init.Biomes;
import net.minecraft.util.WeightedRandom;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.ChunkGeneratorSettings;
import net.minecraftforge.common.BiomeManager;
import plus.fserver.world.generator.GenSession;
import plus.fserver.world.generator.biome.BiomeLib;
import java.util.ArrayList;


public class EGenLayerBiomePlus extends EGenLayer {
    private final ArrayList<BiomeEntry>[] biomes;
    private final int fixSize;


    public EGenLayerBiomePlus(long seed, EGenLayer parent, ChunkGeneratorSettings settings, BiomeLib lib) {
        super(seed);
        this.parent = parent;

        if(lib==null) {
            biomes = new ArrayList[BiomeManager.BiomeType.values().length];
            for (BiomeManager.BiomeType type : BiomeManager.BiomeType.values()) {
                ImmutableList<BiomeManager.BiomeEntry> biomesToAdd = BiomeManager.getBiomes(type);
                int idx = type.ordinal();

                if (biomes[idx] == null) biomes[idx] = new ArrayList<>();
                if (biomesToAdd != null) {
                    ArrayList<BiomeEntry> biomesToAdd0 = new ArrayList<>();
                    for (BiomeManager.BiomeEntry e : biomesToAdd) {
                        biomesToAdd0.add(new BiomeEntry(e.biome, e.itemWeight));
                    }
                    biomes[idx].addAll(biomesToAdd0);
                }
            }
            int desertIdx = net.minecraftforge.common.BiomeManager.BiomeType.DESERT.ordinal();

            biomes[desertIdx].add(new BiomeEntry(Biomes.DESERT, 30));
            biomes[desertIdx].add(new BiomeEntry(Biomes.SAVANNA, 20));
            biomes[desertIdx].add(new BiomeEntry(Biomes.PLAINS, 10));
        } else {
            biomes = lib.biomes;
        }
        this.fixSize = settings==null?-1:settings.biomeSize;
    }


    @Override
    public int[] getInts(GenSession cache, int areaX, int areaY, int areaWidth, int areaHeight) {
        int[] aint = this.parent.getInts(cache, areaX, areaY, areaWidth, areaHeight);
        int[] aint1 = cache.getIntCache(areaWidth * areaHeight);

        for (int i = 0; i < areaHeight; ++i) {
            for (int j = 0; j < areaWidth; ++j) {
                cache.initChunkSeed(this, (long)(j + areaX), (long)(i + areaY));
                int k = aint[j + i * areaWidth];
                int l = (k & 3840) >> 8;
                k = k & -3841;

//                if (this.settings != null && this.settings.fixedBiome >= 0)
//                {
//                    aint1[j + i * areaWidth] = this.settings.fixedBiome;
//                }
                if (isBiomeOceanic(k)) {
                    aint1[j + i * areaWidth] = k;
                } else if (k == ID_MUSHROOM_ISLAND) {
                    aint1[j + i * areaWidth] = k;
                } else if (k == 1) {
                    if (l > 0) {
                        if (cache.nextInt(3) == 0) {
                            aint1[j + i * areaWidth] = ID_MESA_CLEAR_ROCK;
                        }
                        else {
                            aint1[j + i * areaWidth] = ID_MESA_ROCK;
                        }
                    } else {
                        aint1[j + i * areaWidth] = getWeightedBiome(cache, BiomeManager.BiomeType.DESERT);
                    }
                } else if (k == 2) {
                    if (l > 0) {
                        aint1[j + i * areaWidth] = ID_JUNGLE;
                    } else {
                        aint1[j + i * areaWidth] = getWeightedBiome(cache, BiomeManager.BiomeType.WARM);
                    }
                } else if (k == 3) {
                    if (l > 0) {
                        aint1[j + i * areaWidth] = ID_REDWOOD_TAIGA;
                    } else {
                        aint1[j + i * areaWidth] = getWeightedBiome(cache, BiomeManager.BiomeType.COOL);
                    }
                } else if (k == 4) {
                    aint1[j + i * areaWidth] = getWeightedBiome(cache,     BiomeManager.BiomeType.ICY);
                } else {
                    aint1[j + i * areaWidth] = ID_MUSHROOM_ISLAND;
                }
            }
        }
        return aint1;
    }


    public static final class BiomeEntry extends WeightedRandom.Item{
        public final int id;
        public BiomeEntry(int id, int itemWeightIn) {
            super(itemWeightIn);
            this.id = id;
        }

        public BiomeEntry(Biome biome, int itemWeightIn) {
            this(Biome.getIdForBiome(biome), itemWeightIn);
        }
    }


    protected int getWeightedBiome(GenSession cache, BiomeManager.BiomeType type) {
        ArrayList<BiomeEntry> list = biomes[type.ordinal()];
        int totalWeight = WeightedRandom.getTotalWeight(list);

        int weight = BiomeManager.isTypeListModded(type)?cache.nextInt(totalWeight)
                :cache.nextInt(totalWeight / 10) * 10;

        return WeightedRandom.getRandomItem(list, weight).id;
    }
}