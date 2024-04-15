package plus.fserver.world.generator.biome.layers;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.world.biome.Biome;
import plus.fserver.api.BiomeIndexes;
import plus.fserver.api.ExBiome;
import plus.fserver.world.generator.biome.BiomeLib;
import plus.fserver.world.generator.biome.EIntCache;
import java.util.ArrayList;


public class EGenLayerRiverMixPlus extends EGenLayer {
    private final EGenLayer biomePatternGeneratorChain;
    private final EGenLayer riverPatternGeneratorChain;
    private final IntOpenHashSet badIds;

    public EGenLayerRiverMixPlus(long seed, EGenLayer bio, EGenLayer riv, BiomeLib lib) {
        super(seed);
        this.biomePatternGeneratorChain = bio;
        this.riverPatternGeneratorChain = riv;
        badIds = getBadIds(lib);
    }


    private static IntOpenHashSet getBadIds(BiomeLib lib){
        IntOpenHashSet set = new IntOpenHashSet();
        set.add(0);
        set.add(24);
        if(lib==null)return set;
        for(int[][] ints:lib.biomeBounds){
            for (int[] bound :ints){
                for(int id:bound){
                    if(isBad(id)) set.add(id);
                }
            }
        }
        for(ArrayList<EGenLayerBiomePlus.BiomeEntry>list: lib.biomes){
            for(EGenLayerBiomePlus.BiomeEntry e:list){
                if(isBad(e.id)) set.add(e.id);
            }
        }
        return set;
    }


    private static boolean isBad(int id){
        Biome biome = BiomeIndexes.get(id);
        if(biome instanceof ExBiome){
            return ((ExBiome) biome).getSelfSettings().riverSize == 0;
        }
        return false;
    }


    public void initWorldGenSeed(long seed) {
        this.biomePatternGeneratorChain.initWorldGenSeed(seed);
        this.riverPatternGeneratorChain.initWorldGenSeed(seed);
        super.initWorldGenSeed(seed);
    }


    private boolean isValidBiome(int id){
        return !badIds.contains(id);
    }


    @Override
    public int[] getInts(EIntCache cache, int areaX, int areaY, int areaWidth, int areaHeight) {
        int[] aint = this.biomePatternGeneratorChain.getInts(cache, areaX, areaY, areaWidth, areaHeight);
        int[] aint1 = this.riverPatternGeneratorChain.getInts(cache, areaX, areaY, areaWidth, areaHeight);
        int[] aint2 = cache.getIntCache(areaWidth * areaHeight);

        for (int i = 0; i < areaWidth * areaHeight; ++i) {
            if (isValidBiome(aint[i])) {
                if (aint1[i] == 7) {
                    if (aint[i] == 12) {
                        aint2[i] = 11;
                    } else if (aint[i] != 14 && aint[i] != 15) {
                        aint2[i] = aint1[i] & 255;
                    } else {
                        aint2[i] = 15;
                    }
                } else {
                    aint2[i] = aint[i];
                }
            } else {
                aint2[i] = aint[i];
            }
        }
        return aint2;
    }
}