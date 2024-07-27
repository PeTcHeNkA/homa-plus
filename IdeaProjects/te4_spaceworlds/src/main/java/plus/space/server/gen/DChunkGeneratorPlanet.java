package plus.space.server.gen;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockIDIndexes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.*;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.ChunkGeneratorSettings;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.NoiseGeneratorOctaves;
import net.minecraft.world.gen.NoiseGeneratorPerlin;
import net.minecraft.world.gen.feature.WorldGenDungeons;
import net.minecraft.world.gen.feature.WorldGenLakes;
import net.minecraft.world.gen.structure.*;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.terraingen.TerrainGen;
import plus.fserver.ElcaConfig;
import plus.fserver.world.generator.GenSession;
import plus.fserver.world.generator.biome.BiomeProviderEx;
import plus.fserver.world.generator.future.WorldGenStalactite;
import plus.fserver.world.generator.map.EMapGenBase;
import plus.fserver.world.generator.map.EMapGenCaves;
import plus.fserver.world.generator.map.EMapGenRavine;
import plus.space.server.SolarWorldItem;
import plus.space.server.biome.TsonBiomePlus;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import static plus.fserver.ElcaCV.SERVER_COMPILE;


public class DChunkGeneratorPlanet implements IChunkGenerator {
    //    private final long seed;
    private final Random rand;
    private NoiseGeneratorOctaves minLimitPerlinNoise;
    private NoiseGeneratorOctaves maxLimitPerlinNoise;
    private NoiseGeneratorOctaves mainPerlinNoise;
    private NoiseGeneratorPerlin surfaceNoise;
    public NoiseGeneratorOctaves scaleNoise;
    public NoiseGeneratorOctaves depthNoise;
    public NoiseGeneratorOctaves forestNoise;
    private final World world;
    private final boolean mapFeaturesEnabled;
    private final WorldType terrainType;
    private ChunkGeneratorSettings settings;
    private IBlockState oceanBlock = Blocks.WATER.getDefaultState();

    private final EMapGenBase caveGenerator = new EMapGenCaves();
    private final EMapGenBase ravineGenerator = new EMapGenRavine();
    //    private MapGenBase caveGenerator = new MapGenCaves();
//    private MapGenBase ravineGenerator = new MapGenRavine();
    private MapGenStronghold strongholdGenerator = new MapGenStronghold();
    private MapGenVillage villageGenerator = new MapGenVillage();
    private MapGenMineshaft mineshaftGenerator = new MapGenMineshaft();
    private MapGenScatteredFeature scatteredFeatureGenerator = new MapGenScatteredFeature();


    private StructureOceanMonument oceanMonumentGenerator = new StructureOceanMonument();
    //private WoodlandMansion woodlandMansionGenerator = new WoodlandMansion(this);


    public DChunkGeneratorPlanet(WorldServer world, SolarWorldItem item) {
        {
            //caveGenerator = net.minecraftforge.event.terraingen.TerrainGen.getModdedMapGen(caveGenerator, net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.CAVE);
            strongholdGenerator = (MapGenStronghold) TerrainGen.getModdedMapGen(strongholdGenerator, net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.STRONGHOLD);
            villageGenerator = (MapGenVillage) TerrainGen.getModdedMapGen(villageGenerator, net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.VILLAGE);
            mineshaftGenerator = (MapGenMineshaft) TerrainGen.getModdedMapGen(mineshaftGenerator, net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.MINESHAFT);
            scatteredFeatureGenerator = (MapGenScatteredFeature) TerrainGen.getModdedMapGen(scatteredFeatureGenerator, net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.SCATTERED_FEATURE);

            //ravineGenerator = net.minecraftforge.event.terraingen.TerrainGen.getModdedMapGen(ravineGenerator, net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.RAVINE);

            oceanMonumentGenerator = (StructureOceanMonument) TerrainGen.getModdedMapGen(oceanMonumentGenerator, net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.OCEAN_MONUMENT);
            //woodlandMansionGenerator = (WoodlandMansion) TerrainGen.getModdedMapGen(woodlandMansionGenerator, net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.WOODLAND_MANSION);
        }
        this.world = world;
        this.mapFeaturesEnabled = world.getWorldInfo().isMapFeaturesEnabled();
        this.terrainType = world.getWorldInfo().getTerrainType();
        rand = new Random(world.getSeed());

        //this.seed = seed;
        this.minLimitPerlinNoise = new NoiseGeneratorOctaves(rand, 16);
        this.maxLimitPerlinNoise = new NoiseGeneratorOctaves(rand, 16);
        this.mainPerlinNoise = new NoiseGeneratorOctaves(rand, 8);
        this.surfaceNoise = new NoiseGeneratorPerlin(rand, 4);
        this.scaleNoise = new NoiseGeneratorOctaves(rand, 10);
        this.depthNoise = new NoiseGeneratorOctaves(rand, 16);
        this.forestNoise = new NoiseGeneratorOctaves(rand, 8);

        String generatorOptions = world.getWorldInfo().getGeneratorOptions();

        if (generatorOptions != null) {
            this.settings = ChunkGeneratorSettings.Factory.jsonToFactory(generatorOptions).build();
            this.oceanBlock = this.settings.useLavaOceans ? Blocks.LAVA.getDefaultState() : Blocks.WATER.getDefaultState();
            world.setSeaLevel(this.settings.seaLevel);
        }

        net.minecraftforge.event.terraingen.InitNoiseGensEvent.ContextOverworld ctx =
                new net.minecraftforge.event.terraingen.InitNoiseGensEvent.ContextOverworld(minLimitPerlinNoise, maxLimitPerlinNoise, mainPerlinNoise, surfaceNoise, scaleNoise, depthNoise, forestNoise);
        ctx = TerrainGen.getModdedNoiseGenerators(world, this.rand, ctx);
        this.minLimitPerlinNoise = ctx.getLPerlin1();
        this.maxLimitPerlinNoise = ctx.getLPerlin2();
        this.mainPerlinNoise = ctx.getPerlin();
        this.surfaceNoise = ctx.getHeight();
        this.scaleNoise = ctx.getScale();
        this.depthNoise = ctx.getDepth();
        this.forestNoise = ctx.getForest();
    }


    private final ConcurrentLinkedQueue<DGenSession> sessionPool = new ConcurrentLinkedQueue<>();
    private DGenSession makeGenSession() {
        DGenSession session = sessionPool.poll();
        return session==null ? new DGenSession() : session;
    }
    private void freeSession(DGenSession session) {
        sessionPool.add(session);
    }
    @Override
    public void cleanupCache() {
        if(sessionPool.size()==1){
            sessionPool.clear();
        } else {
            while (sessionPool.size()>1) sessionPool.poll();
        }
    }


    public void setBlocksInChunk(int x, int z, ChunkPrimer primer) {
        BiomeProvider provider = this.world.getBiomeProvider();
        DGenSession session = makeGenSession();
        setBlocksInChunk(session, x, z, primer);
        freeSession(session);
    }

    private static class DGenSession extends GenSession{
        private int[] waterLvl;
        private IBlockState[] ocean;
    }


    private int[] waterLvl(DGenSession session, Biome[] biomesForGeneration0) {
        int[] cache = session.waterLvl;
        IBlockState[] ocean = session.ocean;
        if(cache == null) cache = session.waterLvl = new int[256];
        if(ocean == null) ocean = new IBlockState[256];
        Biome biome = null;
        int prev = -1;
        int seaLvl = settings.seaLevel;
        IBlockState oceanBlock = this.oceanBlock;
        for (int x = 0; x < 16; x++) {
            int x16 = x << 4;
            for (int z = 0; z < 16; z++) {
                int index = x16 + z;

                Biome biome2 = biomesForGeneration0[index];
                if (biome2 == biome) {
                    cache[index] = prev;
                    ocean[index] = oceanBlock;
                } else {
                    biome = biome2;
                    if (biome2 instanceof TsonBiomePlus) {
                        TsonBiomePlus tsonBiome = ((TsonBiomePlus) biome);
                        prev       = cache[index] = seaLvl + tsonBiome.getSeaLevel();
                        oceanBlock = ocean[index] = tsonBiome.getOceanBlock();
                    } else {
                        prev       = cache[index] = seaLvl;
                        oceanBlock = ocean[index] = this.oceanBlock;
                    }
                }
            }
        }
        for (int x = 0; x < 16; x++) {
            int x16 = x << 4;
            for (int z = 0; z < 16; z++) {
                int cur, xUp, xDn, zUp, zDn;
                cur = cache[x16 + z];

                if(x == 0)xUp = cur;
                else xUp = cache[x16 - 16 + z];

                if(x == 15)xDn = cur;
                else xDn = cache[x16 + 16 + z];

                if(z == 0)zUp = cur;
                else zUp = cache[x16 + z - 1];

                if(z == 15)zDn = cur;
                else zDn = cache[x16 + z + 1];

                cache[x16 + z] = 1 + MathHelper.floor((cur+cur+xUp+xDn+zUp+zDn)/6f);
            }
        }
        return cache;
    }


    public void setBlocksInChunk(DGenSession session, final int x, final int z, ChunkPrimer primer) {
        BiomeProvider provider = this.world.getBiomeProvider();
        if(provider instanceof BiomeProviderEx){
            session.biomesForGeneration = ((BiomeProviderEx)provider)
                    .getBiomesForGeneration(session, session.biomesForGeneration, x * 4 - 2, z * 4 - 2, 10, 10);
        } else {
            session.biomesForGeneration = provider
                    .getBiomesForGeneration(session.biomesForGeneration, x * 4 - 2, z * 4 - 2, 10, 10);
        }

        this.generateHeightmap(session, x * 4, 0, z * 4);

        if(provider instanceof BiomeProviderEx){
            session.biomesForGeneration = ((BiomeProviderEx)provider)
                    .getBiomes(session, session.biomesForGeneration, x * 16, z * 16, 16, 16, true);
        } else {
            session.biomesForGeneration = provider
                    .getBiomes(session.biomesForGeneration, x * 16, z * 16, 16, 16);
        }

        final int[] levels = waterLvl(session, session.biomesForGeneration);
        final IBlockState ocean = this.oceanBlock;
        final double[] heightMap = session.heightMap;

        for (int i = 0; i < 4; ++i) {
            int j = i * 5;
            int k = (i + 1) * 5;

            for (int l = 0; l < 4; ++l) {
                int i1 = (j + l) * 33;
                int j1 = (j + l + 1) * 33;
                int k1 = (k + l) * 33;
                int l1 = (k + l + 1) * 33;

                for (int i2 = 0; i2 < 32; ++i2) {
                    double d1 = heightMap[i1 + i2];
                    double d2 = heightMap[j1 + i2];
                    double d3 = heightMap[k1 + i2];
                    double d4 = heightMap[l1 + i2];
                    double d5 = (heightMap[i1 + i2 + 1] - d1) * 0.125D;
                    double d6 = (heightMap[j1 + i2 + 1] - d2) * 0.125D;
                    double d7 = (heightMap[k1 + i2 + 1] - d3) * 0.125D;
                    double d8 = (heightMap[l1 + i2 + 1] - d4) * 0.125D;

                    for (int j2 = 0; j2 < 8; ++j2) {
                        int ay = i2 * 8 + j2;

                        double d10 = d1;
                        double d11 = d2;
                        double d12 = (d3 - d1) * 0.25D;
                        double d13 = (d4 - d2) * 0.25D;

                        for (int k2 = 0; k2 < 4; ++k2) {
                            int ax = i * 4 + k2;

                            double d16 = (d11 - d10) * 0.25D;
                            double d17 = d10 - d16;

                            for (int l2 = 0; l2 < 4; ++l2) {
                                int az = l * 4 + l2;

                                if ((d17 += d16) > 0.0D) {
                                    primer.setBlockState(ax, ay, az, BlockIDIndexes.STONE_STATE);
                                } else {
                                    if (ay < levels[(az << 4) + ax]) {
                                        primer.setBlockState(ax, ay, az, ocean);
                                    }
                                }
                            }
                            d10 += d12;
                            d11 += d13;
                        }
                        d1 += d5;
                        d2 += d6;
                        d3 += d7;
                        d4 += d8;
                    }
                }
            }
        }
    }


    public void replaceBiomeBlocks(GenSession session, int x, int z, ChunkPrimer primer, Biome[] biomesIn) {
        if (!ForgeEventFactory.onReplaceBiomeBlocks(this, x, z, primer, this.world)) return;
        session.depthBuffer = this.surfaceNoise.getRegion(session.depthBuffer, x * 16, z * 16, 16, 16, 0.0625D, 0.0625D, 1.0D);

        for (int dX = 0; dX < 16; ++dX) {
            for (int dZ = 0; dZ < 16; ++dZ) {
                Biome biome = biomesIn[dZ + dX * 16];
                biome.genTerrainBlocks(this.world, session.rand, primer, x * 16 + dX, z * 16 + dZ, session.depthBuffer[dZ + dX * 16]);
            }
        }
    }


    @Override
    public Chunk generateChunk(int x, int z) {
        DGenSession session = makeGenSession();

        session.rand.setSeed((long)x * 341873128712L + (long)z * 132897987541L);
        ChunkPrimer chunkprimer = new ChunkPrimer();

        this.setBlocksInChunk(session, x, z, chunkprimer);
        this.replaceBiomeBlocks(session, x, z, chunkprimer, session.biomesForGeneration);

        if (this.settings.useCaves) {
            this.caveGenerator.generate(this.world, session, x, z, chunkprimer);
        }

        if (this.settings.useRavines) {
            this.ravineGenerator.generate(this.world, session, x, z, chunkprimer);
        }

        if (this.mapFeaturesEnabled) {
            if (this.settings.useMineShafts) {
                this.mineshaftGenerator.generate(this.world, x, z, chunkprimer);
            }

            if (this.settings.useVillages) {
                this.villageGenerator.generate(this.world, x, z, chunkprimer);
            }

            if (this.settings.useStrongholds) {
                this.strongholdGenerator.generate(this.world, x, z, chunkprimer);
            }

            if (this.settings.useTemples) {
                this.scatteredFeatureGenerator.generate(this.world, x, z, chunkprimer);
            }

            if (this.settings.useMonuments) {
                this.oceanMonumentGenerator.generate(this.world, x, z, chunkprimer);
            }

//            if (this.settings.useMansions) {
//                this.woodlandMansionGenerator.generate(this.world, x, z, chunkprimer);
//            }
        }

        Chunk chunk = new Chunk(this.world, chunkprimer, x, z);
        byte[] abyte = chunk.getBiomeArray();

        for (int i = 0; i < abyte.length; ++i) {
            abyte[i] = (byte)Biome.getIdForBiome(session.biomesForGeneration[i]);
        }

        freeSession(session);

        chunk.generateSkylightMap();
        return chunk;
    }


    private static int getSeaLvl(Biome biome){
        if(biome instanceof TsonBiomePlus){
            return ((TsonBiomePlus)biome).getSeaLevel();
        }
        return 0;
    }


    private void generateHeightmap(GenSession session, int x, int y, int z) {
        double[] depth = session.depthRegion = this.depthNoise.generateNoiseOctaves(session.depthRegion, x, z, 5, 5, (double)this.settings.depthNoiseScaleX, (double)this.settings.depthNoiseScaleZ, (double)this.settings.depthNoiseScaleExponent);
        float f = this.settings.coordinateScale;
        float f1 = this.settings.heightScale;
        double[] mainNoise = session.mainNoiseRegion = this.mainPerlinNoise.generateNoiseOctaves(session.mainNoiseRegion, x, y, z, 5, 33, 5, f / this.settings.mainNoiseScaleX, (double)(f1 / this.settings.mainNoiseScaleY), (double)(f / this.settings.mainNoiseScaleZ));
        double[] minLim = session.minLimitRegion = this.minLimitPerlinNoise.generateNoiseOctaves(session.minLimitRegion, x, y, z, 5, 33, 5, f, f1, f);
        double[] maxLim = session.maxLimitRegion = this.maxLimitPerlinNoise.generateNoiseOctaves(session.maxLimitRegion, x, y, z, 5, 33, 5, f, f1, f);
        int i = 0;
        int j = 0;

        Biome[] biomesForGeneration = session.biomesForGeneration;
        final float depthOff = this.settings.biomeDepthOffSet;
        final float scaleOff = this.settings.biomeScaleOffset;
        final float depthW = this.settings.biomeDepthWeight;
        final float scaleW = this.settings.biomeScaleWeight;
        final float[] weights = session.biomeWeights;
        final float baseSize = this.settings.baseSize;
        final boolean bigTerrian = this.terrainType == WorldType.AMPLIFIED;

        for (int k = 0; k < 5; ++k) {
            for (int l = 0; l < 5; ++l) {
                float f2 = 0.0F;
                float f3 = 0.0F;
                float f4 = 0.0F;
                Biome biome = biomesForGeneration[k + 2 + (l + 2) * 10];

                for (int j1 = -2; j1 <= 2; ++j1) {
                    for (int k1 = -2; k1 <= 2; ++k1) {
                        Biome biome1 = biomesForGeneration[k + j1 + 2 + (l + k1 + 2) * 10];
                        float f5 = depthOff + biome1.getBaseHeight() * depthW;
                        float f6 = scaleOff + biome1.getHeightVariation() * scaleW;

                        if (bigTerrian && f5 > 0.0F) {
                            f5 = 1.0F + f5 * 2.0F;
                            f6 = 1.0F + f6 * 4.0F;
                        }

                        float f7 = weights[j1 + 2 + (k1 + 2) * 5] / (f5 + 2.0F);

                        if (biome1.getBaseHeight() > biome.getBaseHeight() || getSeaLvl(biome1) > getSeaLvl(biome)) {
                            f7 /= 2.0F;
                        }

                        f2 += f6 * f7;
                        f3 += f5 * f7;
                        f4 += f7;
                    }
                }
                f2 = f2 / f4;
                f3 = f3 / f4;
                f2 = f2 * 0.9F + 0.1F;
                f3 = (f3 * 4.0F - 1.0F) / 8.0F;
                double d7 = depth[j] / 8000.0D;

                if (d7 < 0.0D) {
                    d7 = -d7 * 0.3D;
                }

                d7 = d7 * 3.0D - 2.0D;

                if (d7 < 0.0D) {
                    d7 = d7 / 2.0D;

                    if (d7 < -1.0D) {
                        d7 = -1.0D;
                    }

                    d7 = d7 / 1.4D;
                    d7 = d7 / 2.0D;
                } else {
                    if (d7 > 1.0D) {
                        d7 = 1.0D;
                    }
                    d7 = d7 / 8.0D;
                }

                ++j;
                double d8 = f3;
                double d9 = f2;
                d8 = d8 + d7 * 0.2D;
                d8 = d8 * (double)baseSize / 8.0D;
                double d0 = (double)baseSize + d8 * 4.0D;

                for (int l1 = 0; l1 < 33; ++l1) {
                    double d1 = ((double)l1 - d0) * (double)this.settings.stretchY * 128.0D / 256.0D / d9;

                    if (d1 < 0.0D) {
                        d1 *= 4.0D;
                    }

                    double d2 = minLim[i] / (double)this.settings.lowerLimitScale;
                    double d3 = maxLim[i] / (double)this.settings.upperLimitScale;
                    double d4 = (mainNoise[i] / 10.0D + 1.0D) / 2.0D;
                    double d5 = MathHelper.clampedLerp(d2, d3, d4) - d1;

                    if (l1 > 29) {
                        double d6 = (float)(l1 - 29) / 3.0F;
                        d5 = d5 * (1.0D - d6) + -10.0D * d6;
                    }

                    session.heightMap[i] = d5;
                    ++i;
                }
            }
        }
    }


    @Override
    public void populate(int x, int z) {
        BlockFalling.fallInstantly = true;
        int i = x * 16;
        int j = z * 16;
        BlockPos blockpos = new BlockPos(i, 0, j);
        Biome biome = this.world.getBiome(blockpos.add(16, 0, 16));
        this.rand.setSeed(this.world.getSeed());
        long k = this.rand.nextLong() / 2L * 2L + 1L;
        long l = this.rand.nextLong() / 2L * 2L + 1L;
        this.rand.setSeed((long)x * k + (long)z * l ^ this.world.getSeed());

        ChunkPos chunkpos = new ChunkPos(x, z);

        ForgeEventFactory.onChunkPopulate(true, this, this.world, this.rand, x, z, false);

        if (biome != Biomes.DESERT && biome != Biomes.DESERT_HILLS && this.settings.useWaterLakes && this.rand.nextInt(this.settings.waterLakeChance) == 0)
            if (TerrainGen.populate(this, this.world, this.rand, x, z, false, PopulateChunkEvent.Populate.EventType.LAKE)) {
                int i1 = this.rand.nextInt(16) + 8;
                int j1 = this.rand.nextInt(256);
                int k1 = this.rand.nextInt(16) + 8;
                (new WorldGenLakes(Blocks.WATER)).generate(this.world, this.rand, blockpos.add(i1, j1, k1));
            }

        if (this.rand.nextInt(this.settings.lavaLakeChance / 10) == 0 && this.settings.useLavaLakes)
            if (TerrainGen.populate(this, this.world, this.rand, x, z, false, PopulateChunkEvent.Populate.EventType.LAVA)) {
                int i2 = this.rand.nextInt(16) + 8;
                int l2 = this.rand.nextInt(this.rand.nextInt(248) + 8);
                int k3 = this.rand.nextInt(16) + 8;

                if (l2 < this.world.getSeaLevel() || this.rand.nextInt(this.settings.lavaLakeChance / 8) == 0)
                {
                    (new WorldGenLakes(Blocks.LAVA)).generate(this.world, this.rand, blockpos.add(i2, l2, k3));
                }
            }

        if(true){
            new WorldGenStalactite().generate(world, rand, blockpos.add(8,8,8));
        }

        if (this.settings.useDungeons)
            if (TerrainGen.populate(this, this.world, this.rand, x, z, false,
                    PopulateChunkEvent.Populate.EventType.DUNGEON)) {
                for (int j2 = 0; j2 < this.settings.dungeonChance; ++j2) {
                    int i3 = this.rand.nextInt(16) + 8;
                    int l3 = this.rand.nextInt(256);
                    int l1 = this.rand.nextInt(16) + 8;
                    (new WorldGenDungeons()).generate(this.world, this.rand, blockpos.add(i3, l3, l1));
                }
            }

        biome.decorate(this.world, this.rand, new BlockPos(i, 0, j));


        Biome finalBiome = biome;
        ((WorldServer)world).addSyncTask(() -> {
            boolean flag0 = false;
            if (mapFeaturesEnabled) {
                if (settings.useMineShafts) {
                    mineshaftGenerator.generateStructure(world, rand, chunkpos);
                }
                if (settings.useVillages) {
                    flag0 = villageGenerator.generateStructure(world, rand, chunkpos);
                }
                if (settings.useStrongholds) {
                    strongholdGenerator.generateStructure(world, rand, chunkpos);
                }
                if (settings.useTemples) {
                    scatteredFeatureGenerator.generateStructure(world, rand, chunkpos);
                }
                if (settings.useMonuments) {
                    oceanMonumentGenerator.generateStructure(world, rand, chunkpos);
                }
//                if (settings.useMansions) {
//                    woodlandMansionGenerator.generateStructure(world, rand, chunkpos);
//                }
            }
            if (TerrainGen.populate( DChunkGeneratorPlanet.this, world, rand, x, z, flag0, PopulateChunkEvent.Populate.EventType.ANIMALS))
                WorldEntitySpawner.performWorldGenSpawning(world, finalBiome, i + 8, j + 8, 16, 16, rand);
        });

        if (TerrainGen.populate(this, this.world, this.rand, x, z, false, PopulateChunkEvent.Populate.EventType.ICE)) {
            Chunk chunk = world.getChunk(x, z);
            BiomeProvider bpw = world.provider.getBiomeProvider();
            IBlockState prevState, newState;
            BlockPos.MutableBlockPos blockpos2 = new BlockPos.MutableBlockPos();

            for (int dX = 0; dX < 16; ++dX) {
                int absX = blockpos.getX()+dX;
                for (int dZ = 0; dZ < 16; ++dZ) {
                    int absZ = blockpos.getZ()+dZ;

                    Biome biome0 = chunk.getBiome(absX, absZ, bpw);
                    BlockPos blockpos1 = chunk.getPrecipitationHeight(absX, absZ);
                    blockpos2.setPos(blockpos1.getX(), blockpos1.getY()-1, blockpos1.getZ());

                    if (biome0.getTemperature(blockpos2) < 0.15f && canBlockFreezeBody(chunk, blockpos2)) {
                        prevState = chunk.setBlockState(blockpos2, newState = Blocks.ICE.getDefaultState());
                        if(prevState != null)
                            world.markAndNotifyBlock(blockpos2, chunk, prevState, newState, 2);
                    }

                    if (biome0.getTemperature(blockpos1) < 0.15f && canSnowAtBody(world, chunk, blockpos1)) {
                        prevState = chunk.setBlockState(blockpos1, newState = Blocks.SNOW_LAYER.getDefaultState());

                        if(prevState != null)
                            world.markAndNotifyBlock(blockpos1, chunk, prevState, newState, 2);
                    }
                }
            }
        }

        ForgeEventFactory.onChunkPopulate(false, this, this.world, this.rand, x, z, false);

        BlockFalling.fallInstantly = false;
    }


    private boolean canSnowAtBody(World world, Chunk chunk, BlockPos pos) {
        if (pos.getY() >= 0 && pos.getY() < 256 && chunk.getLightFor(EnumSkyBlock.BLOCK, pos) < 10) {
            IBlockState state = chunk.getBlockState(pos);
            return state.getBlock().isAir(state, world, pos) && Blocks.SNOW_LAYER.canPlaceBlockAt(world, pos);
        }
        return false;
    }


    private boolean canBlockFreezeBody(Chunk chunk, BlockPos pos) {
        if (pos.getY() >= 0 && pos.getY() < 256 && chunk.getLightFor(EnumSkyBlock.BLOCK, pos) < 10) {
            IBlockState state = chunk.getBlockState(pos);
            Block block = state.getBlock();
            return (block == Blocks.WATER || block == Blocks.FLOWING_WATER) && state.getValue(BlockLiquid.LEVEL) == 0;
        }
        return false;
    }


    @Override
    public boolean generateStructures(Chunk chunkIn, int x, int z) {
        boolean flag = false;
        if (this.settings.useMonuments && this.mapFeaturesEnabled && chunkIn.getInhabitedTime() < 3600L) {
            flag |= this.oceanMonumentGenerator.generateStructure(this.world, this.rand, new ChunkPos(x, z));
        }
        return flag;
    }


    @Override
    public List<Biome.SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos) {
        if (this.mapFeaturesEnabled && creatureType == EnumCreatureType.MONSTER) {
            if (this.scatteredFeatureGenerator.isSwampHut(pos)) {
                return this.scatteredFeatureGenerator.getMonsters();
            }

            if (this.settings.useMonuments &&
                    this.oceanMonumentGenerator.isPositionInStructure(this.world, pos)) {
                return this.oceanMonumentGenerator.getMonsters();
            }
        }
        return (!world.isRemote || SERVER_COMPILE)?
                world.localSelector.getBiome(pos).getSpawnableList(creatureType)
                :
                world.getBiome(pos).getSpawnableList(creatureType);
    }


    @Override
    public boolean isInsideStructure(World worldIn, String structureName, BlockPos pos) {
        if (!this.mapFeaturesEnabled) {
            return false;
        } else if ("Stronghold".equals(structureName) && this.strongholdGenerator != null) {
            return this.strongholdGenerator.isInsideStructure(pos);
        }
        else if ("Monument".equals(structureName) && this.oceanMonumentGenerator != null) {
            return this.oceanMonumentGenerator.isInsideStructure(pos);
        } else if ("Village".equals(structureName) && this.villageGenerator != null) {
            return this.villageGenerator.isInsideStructure(pos);
        } else if ("Mineshaft".equals(structureName) && this.mineshaftGenerator != null) {
            return this.mineshaftGenerator.isInsideStructure(pos);
        } else {
            return "Temple".equals(structureName) && this.scatteredFeatureGenerator != null ? this.scatteredFeatureGenerator.isInsideStructure(pos) : false;
        }
    }


    @Nullable
    @Override
    public BlockPos getNearestStructurePos(World worldIn, String structureName, BlockPos position, boolean findUnexplored) {
        if (!this.mapFeaturesEnabled) {
            return null;
        } else if ("Stronghold".equals(structureName) && this.strongholdGenerator != null) {
            return this.strongholdGenerator.getNearestStructurePos(worldIn, position, findUnexplored);
        }
        else if ("Monument".equals(structureName) && this.oceanMonumentGenerator != null) {
            return this.oceanMonumentGenerator.getNearestStructurePos(worldIn, position, findUnexplored);
        }
        else if ("Village".equals(structureName) && this.villageGenerator != null) {
            return this.villageGenerator.getNearestStructurePos(worldIn, position, findUnexplored);
        }
        else if ("Mineshaft".equals(structureName) && this.mineshaftGenerator != null) {
            return this.mineshaftGenerator.getNearestStructurePos(worldIn, position, findUnexplored);
        } else {
            return "Temple".equals(structureName) && this.scatteredFeatureGenerator != null ? this.scatteredFeatureGenerator.getNearestStructurePos(worldIn, position, findUnexplored) : null;
        }
    }


    @Override
    public void recreateStructures(Chunk chunkIn, int x, int z) {
        if(1==1)return;
        if (this.mapFeaturesEnabled) {
            if (this.settings.useMineShafts) {
                this.mineshaftGenerator.generate(this.world, x, z, (ChunkPrimer)null);
            }

            if (this.settings.useVillages) {
                this.villageGenerator.generate(this.world, x, z, (ChunkPrimer)null);
            }

            if (this.settings.useStrongholds) {
                this.strongholdGenerator.generate(this.world, x, z, (ChunkPrimer)null);
            }

            if (this.settings.useTemples) {
                this.scatteredFeatureGenerator.generate(this.world, x, z, (ChunkPrimer)null);
            }

            if (this.settings.useMonuments) {
                this.oceanMonumentGenerator.generate(this.world, x, z, (ChunkPrimer)null);
            }
        }
    }


    @Override
    public boolean maybeAsync() {
        return true;
    }


    @Override
    public boolean maybeAsyncPopulate() {
        return ElcaConfig.OVERWORLD_ASYNC_POPULATION;
    }
}