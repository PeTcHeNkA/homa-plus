package plus.fserver.world.generator.biome.layers;

import net.minecraft.init.Biomes;
import net.minecraft.world.biome.Biome;
import plus.fserver.world.generator.biome.EIntCache;


public abstract class EGenLayer{
    static final int ID_OCEAN                    = Biome.getIdForBiome(Biomes.OCEAN                   );
    static final int ID_BEACH                    = Biome.getIdForBiome(Biomes.BEACH                   );
    static final int ID_COLD_BEACH               = Biome.getIdForBiome(Biomes.COLD_BEACH              );
    static final int ID_STONE_BEACH              = Biome.getIdForBiome(Biomes.STONE_BEACH             );
    static final int ID_JUNGLE_EDGE              = Biome.getIdForBiome(Biomes.JUNGLE_EDGE             );
    static final int ID_JUNGLE                   = Biome.getIdForBiome(Biomes.JUNGLE                  );
    static final int ID_DESERT                   = Biome.getIdForBiome(Biomes.DESERT                  );
    static final int ID_DEEP_OCEAN               = Biome.getIdForBiome(Biomes.DEEP_OCEAN              );
    static final int ID_MUSHROOM_ISLAND          = Biome.getIdForBiome(Biomes.MUSHROOM_ISLAND         );
    static final int ID_MUSHROOM_ISLAND_SHORE    = Biome.getIdForBiome(Biomes.MUSHROOM_ISLAND_SHORE   );
    static final int ID_EXTREME_HILLS_WITH_TREES = Biome.getIdForBiome(Biomes.EXTREME_HILLS_WITH_TREES);
    static final int ID_EXTREME_HILLS_EDGE       = Biome.getIdForBiome(Biomes.EXTREME_HILLS_EDGE      );
    static final int ID_RIVER                    = Biome.getIdForBiome(Biomes.RIVER                   );
    static final int ID_SWAMPLAND                = Biome.getIdForBiome(Biomes.SWAMPLAND               );
    static final int ID_MESA_ROCK                = Biome.getIdForBiome(Biomes.MESA_ROCK               );
    static final int ID_MESA_CLEAR_ROCK          = Biome.getIdForBiome(Biomes.MESA_CLEAR_ROCK         );
    static final int ID_MESA                     = Biome.getIdForBiome(Biomes.MESA                    );
    static final int ID_EXTREME_HILLS            = Biome.getIdForBiome(Biomes.EXTREME_HILLS           );
    static final int ID_ICE_PLANTS               = Biome.getIdForBiome(Biomes.ICE_PLAINS              );
    static final int ID_REDWOOD_TAIGA            = Biome.getIdForBiome(Biomes.REDWOOD_TAIGA           );
    static final int ID_TAIGA                    = Biome.getIdForBiome(Biomes.TAIGA                   );
    static final int ID_COLD_TAIGA               = Biome.getIdForBiome(Biomes.COLD_TAIGA              );

    public long worldGenSeed;
    protected EGenLayer parent;
    private long baseSeed;

    public EGenLayer(long seed) {
        this.baseSeed = seed;
        this.baseSeed *= this.baseSeed * 6364136223846793005L + 1442695040888963407L;
        this.baseSeed += seed;
        this.baseSeed *= this.baseSeed * 6364136223846793005L + 1442695040888963407L;
        this.baseSeed += seed;
        this.baseSeed *= this.baseSeed * 6364136223846793005L + 1442695040888963407L;
        this.baseSeed += seed;
    }


    public void initWorldGenSeed(long seed) {
        this.worldGenSeed = seed;

        if (this.parent != null) {
            this.parent.initWorldGenSeed(seed);
        }

        this.worldGenSeed *= this.worldGenSeed * 6364136223846793005L + 1442695040888963407L;
        this.worldGenSeed += this.baseSeed;
        this.worldGenSeed *= this.worldGenSeed * 6364136223846793005L + 1442695040888963407L;
        this.worldGenSeed += this.baseSeed;
        this.worldGenSeed *= this.worldGenSeed * 6364136223846793005L + 1442695040888963407L;
        this.worldGenSeed += this.baseSeed;
    }


    public abstract int[] getInts(EIntCache cache, int areaX, int areaY, int areaWidth, int areaHeight);


    protected static boolean biomesEqualOrMesaPlateau(int biomeIDA, int biomeIDB) {
        if (biomeIDA == biomeIDB) {
            return true;
        } else {
            Biome biome = Biome.getBiome(biomeIDA);
            Biome biome1 = Biome.getBiome(biomeIDB);

            if (biome != null && biome1 != null) {
                if (biome != Biomes.MESA_ROCK && biome != Biomes.MESA_CLEAR_ROCK) {
                    return biome == biome1 || biome.getBiomeClass() == biome1.getBiomeClass();
                } else {
                    return biome1 == Biomes.MESA_ROCK || biome1 == Biomes.MESA_CLEAR_ROCK;
                }
            } else {
                return false;
            }
        }
    }


    protected static boolean isBiomeOceanic(int id) {
        return net.minecraftforge.common.BiomeManager.oceanBiomes.contains(Biome.getBiome(id));
    }

    /* ======================================== FORGE START =====================================*/
//    protected long nextLong(long par1) {
//        long j = (this.chunkSeed >> 24) % par1;
//
//        if (j < 0) {
//            j += par1;
//        }
//        this.chunkSeed *= this.chunkSeed * 6364136223846793005L + 1442695040888963407L;
//        this.chunkSeed += this.worldGenSeed;
//        return j;
//    }
//
//
//    public static int getModdedBiomeSize(WorldType worldType, int original) {
//        net.minecraftforge.event.terraingen.WorldTypeEvent.BiomeSize event = new net.minecraftforge.event.terraingen.WorldTypeEvent.BiomeSize(worldType, original);
//        net.minecraftforge.common.MinecraftForge.TERRAIN_GEN_BUS.post(event);
//        return event.getNewSize();
//    }
    /* ========================================= FORGE END ======================================*/
}