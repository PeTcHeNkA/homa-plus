package plus.fserver.world.generator.biome;

import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.init.Biomes;
import net.minecraft.util.ReportedException;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.gen.ChunkGeneratorSettings;
import net.minecraft.world.gen.layer.GenLayerEdge;
import net.minecraft.world.storage.WorldInfo;
import org.jetbrains.annotations.NotNull;
import plus.fserver.api.BiomeIndexes;
import plus.fserver.world.generator.GenSession;
import plus.fserver.world.generator.biome.layers.*;
import plus.tson.TsonMap;
import plus.tson.utl.alloc.ConcurrentCasAllocator;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import static net.minecraft.world.gen.layer.GenLayer.getModdedBiomeSize;


public class BiomeProviderEx extends BiomeProvider{
    private final EGenLayer getBiomes;
    private final EGenLayer biomeIndex;
    private final BiomeCacheEx biomeCache = new BiomeCacheEx(this);
    private final ConcurrentCasAllocator cache = new ConcurrentCasAllocator(16,12);
    private final long seed;

    public BiomeProviderEx(World world, WorldInfo info, BiomeLib lib) {
        this(world, info.getSeed(), info.getTerrainType(), info, lib);
    }


    public BiomeProviderEx(World world, long seed, WorldType worldTypeIn, WorldInfo info, BiomeLib lib) {
        this.seed = seed;
        if (worldTypeIn == WorldType.CUSTOMIZED && !info.getGeneratorOptions().isEmpty()) {
            if(world instanceof WorldServer) {
                settings = ((WorldServer)world).getChunkGenSettings().settings;
            }
        }
        EGenLayer[] agenlayer = createLayers(seed, worldTypeIn, settings, lib);

        getBiomes = agenlayer[0];
        biomeIndex = agenlayer[1];
    }


    private static boolean use(TsonMap src, String key){
        return !src.getOrDefaultBool(key, false);
    }


    public static EGenLayer[] createLayers(long seed, WorldType type, ChunkGeneratorSettings settings, BiomeLib lib) {
        TsonMap properties = null;
        if(lib != null) properties = lib.settings;

        TsonMap rem = properties == null?new TsonMap():properties.getOrCreateMap("rem");

        EGenLayer genlayer = new EGenLayerIsland(1L);
        genlayer = new EGenLayerFuzzyZoom(2000L, genlayer);
        genlayer = new EGenLayerAddIsland(1L, genlayer);
        genlayer = new EGenLayerZoom(2001L, genlayer);
        genlayer = new EGenLayerAddIsland(2L, genlayer);
        genlayer = new EGenLayerAddIsland(50L, genlayer);
        genlayer = new EGenLayerAddIsland(70L, genlayer);

        //REM
        if(use(rem, "rem_to_much_ocean"))
            genlayer = new EGenLayerRemoveTooMuchOcean(2L, genlayer);
        if(use(rem, "add_snow"))
            genlayer = new EGenLayerAddSnow(2L, genlayer);


        EGenLayer genlayeredge = new EGenLayerAddIsland(3L, genlayer);

        genlayeredge = new EGenLayerEdge(2L, genlayeredge, GenLayerEdge.Mode.COOL_WARM);
        genlayeredge = new EGenLayerEdge(2L, genlayeredge, GenLayerEdge.Mode.HEAT_ICE);
        genlayeredge = new EGenLayerEdge(3L, genlayeredge, GenLayerEdge.Mode.SPECIAL);

        EGenLayer prev = new EGenLayerZoom(2002L, genlayeredge);

        prev = new EGenLayerZoom(2003L, prev);
        prev = new EGenLayerAddIsland(4L, prev);
        if(use(rem, "add_mushroom_island"))
            prev = new EGenLayerAddMushroomIsland(5L, prev);
        if(use(rem, "add_deep_ocean"))
            prev = new EGenLayerDeepOcean(4L, prev);
        prev = EGenLayerZoom.magnify(1000L, prev, 0);

        int biomeSize = 4;
        int riverSize = biomeSize;

        if (properties != null) {
            biomeSize = properties.getOrDefaultInt("biome_size", settings==null?biomeSize:settings.biomeSize);
            riverSize = properties.getOrDefaultInt("river_size", settings==null?riverSize:settings.riverSize);
        } else if (settings != null) {
            biomeSize = settings.biomeSize;
            riverSize = settings.riverSize;
        }

        if(use(rem, "disable_mod_size"))
            biomeSize = getModdedBiomeSize(type, biomeSize);

        EGenLayer rPrev = EGenLayerZoom.magnify(1000L, prev, 0);
        EGenLayer genlayerriverinit = new EGenLayerRiverInit(100L, rPrev);
        EGenLayer genlayerbiomeedge = getBiomeLayer(prev, settings, lib);
        EGenLayer magnify = EGenLayerZoom.magnify(1000L, genlayerriverinit, 2);

        EGenLayer genlayerhills = new EGenLayerHills(1000L, genlayerbiomeedge, magnify);
        EGenLayer genlayer5 = EGenLayerZoom.magnify(1000L, genlayerriverinit, 2);
        genlayer5 = EGenLayerZoom.magnify(1000L, genlayer5, riverSize);

        //RIVER
        EGenLayer genRive;
        if(properties != null && properties.containsKey("river_biome")){
            genRive = new EGenLayerRiverPlus(1L, genlayer5, properties.getInt("river_biome"));
        } else {
            genRive = new EGenLayerRiverPlus(1L, genlayer5, Biomes.RIVER);
        }

        EGenLayer genlayersmooth = new EGenLayerSmooth(1000L, genRive);
        //todo ??
        genlayerhills = new EGenLayerRareBiome(1001L, genlayerhills);

        for (int k = 0; k < biomeSize; ++k) {
            genlayerhills = new EGenLayerZoom(1000 + k, genlayerhills);

            if (k == 0) {
                genlayerhills = new EGenLayerAddIsland(3L, genlayerhills);
            }

            if (k == 1 || biomeSize == 1) {
                genlayerhills = new EGenLayerShore(1000L, genlayerhills);
                if(lib != null && lib.biomeBounds != null && lib.biomeBounds.length>0){
                    for(int bound=0;bound<lib.biomeBounds.length;bound++) {
                        int[][] ints = lib.biomeBounds[bound];
                        if(ints.length>1) {
                            genlayerhills = new EGenLayerMultiShorePlus(9L, genlayerhills, ints);
                        } else if(ints.length==1){
                            genlayerhills = new EGenLayerShorePlus(
                                    0, genlayerhills, ints[0][0], ints[0][1]);
                        }
                    }
                }
            }
        }

        EGenLayer genlayersmooth1 = new EGenLayerSmooth(1000L, genlayerhills);
        EGenLayer genlayerrivermix = new EGenLayerRiverMixPlus(100L, genlayersmooth1, genlayersmooth, lib);
        EGenLayer genlayer3 = new EGenLayerVoronoiZoom(10L, genlayerrivermix);
        genlayerrivermix.initWorldGenSeed(seed);
        genlayer3.initWorldGenSeed(seed);
        return new EGenLayer[] {genlayerrivermix, genlayer3, genlayerrivermix};
    }


    private static EGenLayer getBiomeLayer(EGenLayer parentLayer, ChunkGeneratorSettings chunkSettings, BiomeLib lib) {
        EGenLayer ret = new EGenLayerBiomePlus(200L, parentLayer, chunkSettings, lib);
        ret = EGenLayerZoom.magnify(1000L, ret, 2);
        ret = new EGenLayerBiomeEdge(1000L, ret);
        return ret;
    }


    public Biome[] getBiomesForGeneration(EIntCache cache, Biome[] biomes, int x, int z, int width, int height) {
        if (biomes == null || biomes.length < width * height) {
            biomes = new Biome[width * height];
        }
        //TODO

        int[] aint;
        //synchronized (cache) {
        cache.resetIntCache();
        aint = getBiomes.getInts(cache, x, z, width, height);
        //}

        try {
            for (int i = 0; i < width * height; ++i) {
                biomes[i] = BiomeIndexes.get(aint[i], Biomes.DEFAULT);
            }
            return biomes;
        }
        catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Invalid Biome id");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("RawBiomeBlock");
            crashreportcategory.addCrashSection("biomes[] size", biomes.length);
            crashreportcategory.addCrashSection("x", x);
            crashreportcategory.addCrashSection("z", z);
            crashreportcategory.addCrashSection("w", width);
            crashreportcategory.addCrashSection("h", height);
            throw new ReportedException(crashreport);
        }
    }


    @Override
    public void cleanupCache() {
        cache.clear(1);
    }


    public Biome[] getBiomes(EIntCache cache, @Nullable Biome[] listToReuse, int x, int z, int width, int length, boolean cacheFlag) {
        if (listToReuse == null || listToReuse.length < width * length) {
            listToReuse = new Biome[width * length];
        }
            //cache.resetIntCache();
            if (cacheFlag && width == 16 && length == 16 && (x & 15) == 0 && (z & 15) == 0) {
                Biome[] abiome;
                synchronized (biomeCache) {
                    cache.resetIntCache();
                    abiome = biomeCache.getCachedBiomes(cache, x, z);
                }
                System.arraycopy(abiome, 0, listToReuse, 0, width * length);
                return listToReuse;
            } else {
                int[] aint;
                cache.resetIntCache();
                aint = biomeIndex.getInts(cache, x, z, width, length);

                for (int i = 0; i < width * length; ++i) {
                    listToReuse[i] = BiomeIndexes.get(aint[i], Biomes.DEFAULT);
                }
            }

        return listToReuse;
    }


    public boolean areBiomesViable(EIntCache cache, int x, int z, int radius, List<Biome> allowed) {
        int i = x - radius >> 2;
        int j = z - radius >> 2;
        int k = x + radius >> 2;
        int l = z + radius >> 2;
        int i1 = k - i + 1;
        int j1 = l - j + 1;
        int[] aint;
        cache.resetIntCache();
        aint = getBiomes.getInts(cache, i, j, i1, j1);

        try {
            for (int k1 = 0; k1 < i1 * j1; ++k1) {
                Biome biome = BiomeIndexes.get(aint[k1]);
                if (!allowed.contains(biome)) {
                    return false;
                }
            }
            return true;
        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Invalid Biome id");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("Layer");
            crashreportcategory.addCrashSection("Layer", getBiomes.toString());
            crashreportcategory.addCrashSection("x", x);
            crashreportcategory.addCrashSection("z", z);
            crashreportcategory.addCrashSection("radius", radius);
            crashreportcategory.addCrashSection("allowed", allowed);
            throw new ReportedException(crashreport);
        }
    }


    @Nullable
    public BlockPos findBiomePosition(EIntCache cache, int x, int z, int range, List<Biome> biomes, Random random) {
        //cache.resetIntCache();
        int i = x - range >> 2;
        int j = z - range >> 2;
        int k = x + range >> 2;
        int l = z + range >> 2;
        int i1 = k - i + 1;
        int j1 = l - j + 1;
        int[] aint;

        cache.resetIntCache();
        aint = getBiomes.getInts(cache, i, j, i1, j1);

        BlockPos blockpos = null;
        int k1 = 0;
        for (int l1 = 0; l1 < i1 * j1; ++l1) {
            int i2 = i + l1 % i1 << 2;
            int j2 = j + l1 / i1 << 2;
            Biome biome = BiomeIndexes.get(aint[l1]);
            if (biomes.contains(biome) && (blockpos == null || random.nextInt(k1 + 1) == 0)) {
                blockpos = new BlockPos(i2, 0, j2);
                ++k1;
            }
        }
        return blockpos;
    }


    @NotNull
    @Override //TODO
    public List<Biome> getBiomesToSpawnIn() {
        return new ArrayList<>();
    }


    public Biome getBiome(int x, int z, @NotNull Biome defaultBiome) {
        return this.biomeCache.getBiome(allocCache(), x, z, defaultBiome);
    }


    @NotNull
    @Override
    public EIntCache allocCache() {
        Object session = cache.tryAllocRaw();
        return session==null ? new GenSession(seed) : (EIntCache) session;
    }


    @Override
    public void freeCache(@NotNull EIntCache session) {
        cache.freeObj(session);
    }


    @NotNull
    @Override
    public Biome[] getBiomesForGeneration(@NotNull Biome[] biomes, int x, int z, int width, int height) {
        return getBiomesForGeneration(allocCache(), biomes, x, z, width, height);
    }


    @NotNull
    @Override
    public Biome[] getBiomes(@Nullable Biome[] listToReuse, int x, int z, int width, int length, boolean cacheFlag) {
        EIntCache session = allocCache();
        listToReuse = getBiomes(session, listToReuse, x, z, width, length, cacheFlag);
        freeCache(session);
        return listToReuse;
    }


    @Override
    public boolean areBiomesViable(int x, int z, int radius, @NotNull List<Biome> allowed) {
        EIntCache session = allocCache();
        boolean result = areBiomesViable(session, x, z, radius, allowed);
        freeCache(session);
        return result;
    }


    @Nullable
    @Override
    public BlockPos findBiomePosition(int x, int z, int range, @NotNull List<Biome> biomes, @NotNull Random random) {
        EIntCache session = allocCache();
        BlockPos result = findBiomePosition(session, x, z, range, biomes, random);
        freeCache(session);
        return result;
    }
}