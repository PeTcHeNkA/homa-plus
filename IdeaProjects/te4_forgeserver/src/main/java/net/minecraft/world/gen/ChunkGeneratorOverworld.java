package net.minecraft.world.gen;

import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
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
import net.minecraft.world.gen.feature.WorldGenDungeons;
import net.minecraft.world.gen.feature.WorldGenLakes;
import net.minecraft.world.gen.structure.MapGenMineshaft;
import net.minecraft.world.gen.structure.MapGenScatteredFeature;
import net.minecraft.world.gen.structure.MapGenStronghold;
import net.minecraft.world.gen.structure.MapGenVillage;
import net.minecraft.world.gen.structure.StructureOceanMonument;
import net.minecraft.world.gen.structure.WoodlandMansion;
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
import plus.tson.utl.alloc.ConcurrentCasAllocator;
import static plus.fserver.ElcaCV.SERVER_COMPILE;


public class ChunkGeneratorOverworld implements IChunkGenerator {
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
    private WoodlandMansion woodlandMansionGenerator = new WoodlandMansion(this);


    public ChunkGeneratorOverworld(World worldIn, long seed, boolean mapFeaturesEnabledIn, String generatorOptions) {
        {
            //caveGenerator = net.minecraftforge.event.terraingen.TerrainGen.getModdedMapGen(caveGenerator, net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.CAVE);
            strongholdGenerator = (MapGenStronghold)net.minecraftforge.event.terraingen.TerrainGen.getModdedMapGen(strongholdGenerator, net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.STRONGHOLD);
            villageGenerator = (MapGenVillage)net.minecraftforge.event.terraingen.TerrainGen.getModdedMapGen(villageGenerator, net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.VILLAGE);
            mineshaftGenerator = (MapGenMineshaft)net.minecraftforge.event.terraingen.TerrainGen.getModdedMapGen(mineshaftGenerator, net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.MINESHAFT);
            scatteredFeatureGenerator = (MapGenScatteredFeature)net.minecraftforge.event.terraingen.TerrainGen.getModdedMapGen(scatteredFeatureGenerator, net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.SCATTERED_FEATURE);

            //ravineGenerator = net.minecraftforge.event.terraingen.TerrainGen.getModdedMapGen(ravineGenerator, net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.RAVINE);

            oceanMonumentGenerator = (StructureOceanMonument)net.minecraftforge.event.terraingen.TerrainGen.getModdedMapGen(oceanMonumentGenerator, net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.OCEAN_MONUMENT);
            woodlandMansionGenerator = (WoodlandMansion)net.minecraftforge.event.terraingen.TerrainGen.getModdedMapGen(woodlandMansionGenerator, net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.WOODLAND_MANSION);
        }
        this.world = worldIn;
        this.mapFeaturesEnabled = mapFeaturesEnabledIn;
        this.terrainType = worldIn.getWorldInfo().getTerrainType();
        rand = new Random(seed);

        //this.seed = seed;
        this.minLimitPerlinNoise = new NoiseGeneratorOctaves(rand, 16);
        this.maxLimitPerlinNoise = new NoiseGeneratorOctaves(rand, 16);
        this.mainPerlinNoise = new NoiseGeneratorOctaves(rand, 8);
        this.surfaceNoise = new NoiseGeneratorPerlin(rand, 4);
        this.scaleNoise = new NoiseGeneratorOctaves(rand, 10);
        this.depthNoise = new NoiseGeneratorOctaves(rand, 16);
        this.forestNoise = new NoiseGeneratorOctaves(rand, 8);


        if (generatorOptions != null) {
            this.settings = ChunkGeneratorSettings.Factory.jsonToFactory(generatorOptions).build();
            this.oceanBlock = this.settings.useLavaOceans ? Blocks.LAVA.getDefaultState() : Blocks.WATER.getDefaultState();
            worldIn.setSeaLevel(this.settings.seaLevel);
        }

        net.minecraftforge.event.terraingen.InitNoiseGensEvent.ContextOverworld ctx =
                new net.minecraftforge.event.terraingen.InitNoiseGensEvent.ContextOverworld(minLimitPerlinNoise, maxLimitPerlinNoise, mainPerlinNoise, surfaceNoise, scaleNoise, depthNoise, forestNoise);
        ctx = net.minecraftforge.event.terraingen.TerrainGen.getModdedNoiseGenerators(worldIn, this.rand, ctx);
        this.minLimitPerlinNoise = ctx.getLPerlin1();
        this.maxLimitPerlinNoise = ctx.getLPerlin2();
        this.mainPerlinNoise = ctx.getPerlin();
        this.surfaceNoise = ctx.getHeight();
        this.scaleNoise = ctx.getScale();
        this.depthNoise = ctx.getDepth();
        this.forestNoise = ctx.getForest();
    }


    private final ConcurrentCasAllocator sessionPool = new ConcurrentCasAllocator(8,4);
    private GenSession makeGenSession() {
        Object session = sessionPool.tryAllocRaw();
        return session==null ? new GenSession() : (GenSession) session;
    }
    private void freeSession(GenSession session) {
        sessionPool.freeObj(session);
    }

    @Override
    public void cleanupCache() {
        sessionPool.clear(1);
    }


    public void setBlocksInChunk(int x, int z, ChunkPrimer primer) {
        BiomeProvider provider = this.world.getBiomeProvider();
        GenSession session = makeGenSession();
        setBlocksInChunk(session, x, z, primer);
        freeSession(session);
    }


    public void setBlocksInChunk(GenSession session, final int x, final int z, ChunkPrimer primer) {
        BiomeProvider provider = this.world.getBiomeProvider();
        if(provider instanceof BiomeProviderEx){
            session.biomesForGeneration = ((BiomeProviderEx)provider)
                    .getBiomesForGeneration(session, session.biomesForGeneration, x * 4 - 2, z * 4 - 2, 10, 10);
        } else {
            session.biomesForGeneration = provider
                    .getBiomesForGeneration(session.biomesForGeneration, x * 4 - 2, z * 4 - 2, 10, 10);
        }

        this.generateHeightmap(session, x * 4, 0, z * 4);
        double[] heightMap = session.heightMap;
        int seaLvl = this.settings.seaLevel;
        IBlockState oceanBlock = this.oceanBlock;

        for (int i = 0; i < 4; ++i) {
            int j = i * 5;
            int k = (i + 1) * 5;

            for (int l = 0; l < 4; ++l) {
                int i1 = (j + l) * 33;
                int j1 = (j + l + 1) * 33;
                int k1 = (k + l) * 33;
                int l1 = (k + l + 1) * 33;

                for (int i2 = 0; i2 < 32; ++i2) {
                    double d1 =  heightMap[i1 + i2];
                    double d2 =  heightMap[j1 + i2];
                    double d3 =  heightMap[k1 + i2];
                    double d4 =  heightMap[l1 + i2];
                    double d5 = (heightMap[i1 + i2 + 1] - d1) * 0.125D;
                    double d6 = (heightMap[j1 + i2 + 1] - d2) * 0.125D;
                    double d7 = (heightMap[k1 + i2 + 1] - d3) * 0.125D;
                    double d8 = (heightMap[l1 + i2 + 1] - d4) * 0.125D;

                    for (int j2 = 0; j2 < 8; ++j2) {
                        double d10 = d1;
                        double d11 = d2;
                        double d12 = (d3 - d1) * 0.25D;
                        double d13 = (d4 - d2) * 0.25D;

                        for (int k2 = 0; k2 < 4; ++k2) {
                            double d16 = (d11 - d10) * 0.25D;
                            double lvt_45_1_ = d10 - d16;

                            for (int l2 = 0; l2 < 4; ++l2) {
                                if ((lvt_45_1_ += d16) > 0.0D) {
                                    primer.setBlockState(i * 4 + k2, i2 * 8 + j2, l * 4 + l2, BlockIDIndexes.STONE_STATE);
                                } else if (i2 * 8 + j2 < seaLvl) {
                                    primer.setBlockState(i * 4 + k2, i2 * 8 + j2, l * 4 + l2, oceanBlock);
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
        if (!net.minecraftforge.event.ForgeEventFactory.onReplaceBiomeBlocks(this, x, z, primer, this.world)) return;
        double[] depthBuffer = session.depthBuffer = this.surfaceNoise.getRegion(session.depthBuffer, x * 16, z * 16, 16, 16, 0.0625D, 0.0625D, 1.0D);
        World world = this.world;
        Random rand = session.rand;
        for (int dX = 0; dX < 16; ++dX) {
            for (int dZ = 0; dZ < 16; ++dZ) {
                Biome biome = biomesIn[dZ + (dX << 4)];
                biome.genTerrainBlocks(world, rand, primer, (x << 4) + dX, (z << 4) + dZ, depthBuffer[dZ + (dX << 4)]);
            }
        }
    }


    @Override
    public Chunk generateChunk(int x, int z) {
        World world = this.world;
        BiomeProvider provider = world.getBiomeProvider();
        GenSession session = makeGenSession();

        session.rand.setSeed((long)x * 341873128712L + (long)z * 132897987541L);
        ChunkPrimer chunkprimer = session.primer();
        this.setBlocksInChunk(session, x, z, chunkprimer);

        if(provider instanceof BiomeProviderEx){
            session.biomesForGeneration = ((BiomeProviderEx)provider)
                    .getBiomes(session, session.biomesForGeneration, x * 16, z * 16, 16, 16, true);
        } else {
            session.biomesForGeneration = provider
                    .getBiomes(session.biomesForGeneration, x * 16, z * 16, 16, 16);
        }

        this.replaceBiomeBlocks(session, x, z, chunkprimer, session.biomesForGeneration);

        ChunkGeneratorSettings settings = this.settings;

        if (settings.useCaves) {
            this.caveGenerator.generate(world, session, x, z, chunkprimer);
        }

        if (settings.useRavines) {
            this.ravineGenerator.generate(world, session, x, z, chunkprimer);
        }

        if (this.mapFeaturesEnabled) {
            if (settings.useMineShafts) {
                this.mineshaftGenerator.generate(world, x, z, chunkprimer);
            }

            if (settings.useVillages) {
                this.villageGenerator.generate(world, x, z, chunkprimer);
            }

            if (settings.useStrongholds) {
                this.strongholdGenerator.generate(world, x, z, chunkprimer);
            }

            if (settings.useTemples) {
                this.scatteredFeatureGenerator.generate(world, x, z, chunkprimer);
            }

            if (settings.useMonuments) {
                this.oceanMonumentGenerator.generate(world, x, z, chunkprimer);
            }

            if (settings.useMansions) {
                this.woodlandMansionGenerator.generate(world, x, z, chunkprimer);
            }
        }

        Chunk chunk = new Chunk(world, chunkprimer, x, z);
        byte[] abyte = chunk.getBiomeArray();

        Biome[] biomes = session.biomesForGeneration;

        for (int i = 0; i < abyte.length; ++i) {
            abyte[i] = (byte)Biome.getIdForBiome(biomes[i]);
        }

        chunk.generateSkylightMap(session.pooledPos);
        freeSession(session);

        return chunk;
    }


    private void generateHeightmap(GenSession session, int x, int y, int z) {
        ChunkGeneratorSettings settings = this.settings;

        double[] depthRegion = session.depthRegion = this.depthNoise.generateNoiseOctaves(session.depthRegion, x, z, 5, 5, settings.depthNoiseScaleX, settings.depthNoiseScaleZ, settings.depthNoiseScaleExponent);
        float f  = settings.coordinateScale;
        float f1 = settings.heightScale;

        double[] mainNoiseRegion = session.mainNoiseRegion = this.mainPerlinNoise.generateNoiseOctaves(session.mainNoiseRegion, x, y, z, 5, 33, 5, f / settings.mainNoiseScaleX, f1 / settings.mainNoiseScaleY, f / settings.mainNoiseScaleZ);
        double[] minLimitRegion  = session.minLimitRegion  = this.minLimitPerlinNoise.generateNoiseOctaves(session.minLimitRegion, x, y, z, 5, 33, 5, f, f1, f);
        double[] maxLimitRegion  = session.maxLimitRegion  = this.maxLimitPerlinNoise.generateNoiseOctaves(session.maxLimitRegion, x, y, z, 5, 33, 5, f, f1, f);
        Biome[] biomesForGeneration = session.biomesForGeneration;

        float[] biomeWeights = session.biomeWeights;
        double[] heightMap   = session.heightMap;

        int i = 0;
        int j = 0;

        for (int k = 0; k < 5; ++k) {
            for (int l = 0; l < 5; ++l) {
                float f2 = 0.0F;
                float f3 = 0.0F;
                float f4 = 0.0F;
                int i1 = 2;
                Biome biome = biomesForGeneration[k + 2 + (l + 2) * 10];

                for (int j1 = -2; j1 <= 2; ++j1) {
                    for (int k1 = -2; k1 <= 2; ++k1) {
                        Biome biome1 = biomesForGeneration[k + j1 + 2 + (l + k1 + 2) * 10];
                        float f5 = settings.biomeDepthOffSet + biome1.getBaseHeight()      * settings.biomeDepthWeight;
                        float f6 = settings.biomeScaleOffset + biome1.getHeightVariation() * settings.biomeScaleWeight;

                        if (this.terrainType == WorldType.AMPLIFIED && f5 > 0.0F) {
                            f5 = 1.0F + f5 * 2.0F;
                            f6 = 1.0F + f6 * 4.0F;
                        }

                        float f7 = biomeWeights[j1 + 2 + (k1 + 2) * 5] / (f5 + 2.0F);

                        if (biome1.getBaseHeight() > biome.getBaseHeight()) {
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
                double d7 = depthRegion[j] / 8000.0D;

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
                d8 = d8 * (double)  settings.baseSize / 8.0D;
                double d0 = (double)settings.baseSize + d8 * 4.0D;

                for (int l1 = 0; l1 < 33; ++l1) {
                    double d1 = ((double)l1 - d0) * (double)settings.stretchY * 128.0D / 256.0D / d9;

                    if (d1 < 0.0D) {
                        d1 *= 4.0D;
                    }

                    double d2 = minLimitRegion[i] / (double)settings.lowerLimitScale;
                    double d3 = maxLimitRegion[i] / (double)settings.upperLimitScale;
                    double d4 = (mainNoiseRegion[i] / 10.0D + 1.0D) / 2.0D;
                    double d5 = MathHelper.clampedLerp(d2, d3, d4) - d1;

                    if (l1 > 29) {
                        double d6 = (float)(l1 - 29) / 3.0F;
                        d5 = d5 * (1.0D - d6) + -10.0D * d6;
                    }

                    heightMap[i] = d5;
                    ++i;
                }
            }
        }
    }


    @Override
    public void populate(int x, int z) {
        BlockFalling.fallInstantly = true;
        int wX = x * 16;
        int wZ = z * 16;

        BlockPos.MutableBlockPos blockpos1 = new BlockPos.MutableBlockPos(wX+16, 0, wZ+16);

        World lWorld = this.world;

        Biome biome = lWorld.getBiome(blockpos1);
        Random rand = this.rand;
        rand.setSeed(lWorld.getSeed());
        long k = rand.nextLong() / 2L * 2L + 1L;
        long l = rand.nextLong() / 2L * 2L + 1L;
        rand.setSeed((long)x * k + (long)z * l ^ lWorld.getSeed());

        ChunkPos chunkpos = new ChunkPos(x, z);

        ForgeEventFactory.onChunkPopulate(true, this, lWorld, rand, x, z, false);

        if (biome != Biomes.DESERT && biome != Biomes.DESERT_HILLS && this.settings.useWaterLakes && rand.nextInt(this.settings.waterLakeChance) == 0)
            if (TerrainGen.populate(this, lWorld, rand, x, z, false, PopulateChunkEvent.Populate.EventType.LAKE)) {
                int cX = rand.nextInt(16) + 8;
                int cY = rand.nextInt(256);
                int cZ = rand.nextInt(16) + 8;
                (new WorldGenLakes(Blocks.WATER)).generate(lWorld, rand, blockpos1.setPos(cX+wX, cY, cZ+wZ));
            }

        if (rand.nextInt(this.settings.lavaLakeChance / 10) == 0 && this.settings.useLavaLakes)
            if (TerrainGen.populate(this, lWorld, rand, x, z, false, PopulateChunkEvent.Populate.EventType.LAVA)) {
                int cX = rand.nextInt(16) + 8;
                int cY = rand.nextInt(rand.nextInt(248) + 8);
                int cZ = rand.nextInt(16) + 8;

                if (cY < lWorld.getSeaLevel() || rand.nextInt(this.settings.lavaLakeChance / 8) == 0) {
                    if(maybeAsyncPopulate()){
                        BlockPos lavaPos = blockpos1.setPos(cX+wX, cY, cZ+wZ);
                        ((WorldServer)lWorld).addSyncTask(() -> (new WorldGenLakes(Blocks.LAVA)).generate(lWorld, rand, lavaPos));
                    } else {
                        (new WorldGenLakes(Blocks.LAVA)).generate(lWorld, rand, blockpos1.setPos(cX+wX, cY, cZ+wZ));
                    }
                }
            }

        if(true){
            new WorldGenStalactite().generate(lWorld, rand, blockpos1.setPos(8+wX,8,8+wZ));
        }

        if (this.settings.useDungeons) {
            if (TerrainGen.populate(this, lWorld, rand, x, z, false,
                    PopulateChunkEvent.Populate.EventType.DUNGEON)) {

                WorldGenDungeons gen = null;

                for (int i = 0; i < this.settings.dungeonChance; ++i) {
                    int cX = rand.nextInt(16) + 8;
                    int cY = rand.nextInt(256);
                    int cZ = rand.nextInt(16) + 8;
                    if (gen == null) gen = new WorldGenDungeons();
                    gen.generate(lWorld, rand, blockpos1.setPos(cX + wX, cY, cZ + wZ));
                }
            }
        }

        biome.decorate(lWorld, rand, blockpos1.setPos(wX, 0, wZ));


        Biome finalBiome = biome;
        ((WorldServer)lWorld).addSyncTask(() -> {
            World lWorld2 = this.world;
            boolean flag0 = false;
            if (mapFeaturesEnabled) {
                if (settings.useMineShafts) {
                    mineshaftGenerator.generateStructure(lWorld2, rand, chunkpos);
                }
                if (settings.useVillages) {
                    flag0 = villageGenerator.generateStructure(lWorld2, rand, chunkpos);
                }
                if (settings.useStrongholds) {
                    strongholdGenerator.generateStructure(lWorld2, rand, chunkpos);
                }
                if (settings.useTemples) {
                    scatteredFeatureGenerator.generateStructure(lWorld2, rand, chunkpos);
                }
                if (settings.useMonuments) {
                    oceanMonumentGenerator.generateStructure(lWorld2, rand, chunkpos);
                }
                if (settings.useMansions) {
                    woodlandMansionGenerator.generateStructure(lWorld2, rand, chunkpos);
                }
            }
            if (TerrainGen.populate( ChunkGeneratorOverworld.this, lWorld2, rand, x, z, flag0, PopulateChunkEvent.Populate.EventType.ANIMALS))
                WorldEntitySpawner.performWorldGenSpawning(lWorld2, finalBiome, wX + 8, wZ + 8, 16, 16, rand);
        });

        if (TerrainGen.populate(this, lWorld, rand, x, z, false, PopulateChunkEvent.Populate.EventType.ICE)) {
            Chunk chunk = lWorld.getChunk(x, z);
            BiomeProvider bpw = lWorld.provider.getBiomeProvider();
            IBlockState prevState, newState;

            BlockPos.MutableBlockPos blockpos2 = new BlockPos.MutableBlockPos();

            for (int dX = 0; dX < 16; ++dX) {
                int absX = wX+dX;
                for (int dZ = 0; dZ < 16; ++dZ) {
                    int absZ = wZ+dZ;

                    Biome biome0 = chunk.getBiome(absX, absZ, bpw);
                    blockpos1.setPosB(absX, chunk.getPrecipitationHeightY(absX, absZ), absZ);
                    //BlockPos blockpos1 = chunk.getPrecipitationHeight(absX, absZ);
                    blockpos2.setPos(blockpos1.getX(), blockpos1.getY()-1, blockpos1.getZ());

                    if (biome0.getTemperature(blockpos2) < 0.15f && canBlockFreezeBody(chunk, blockpos2)) {
                        prevState = chunk.setBlockState(blockpos2, newState = Blocks.ICE.getDefaultState());
                        if(prevState != null)
                            lWorld.markAndNotifyBlock(blockpos2, chunk, prevState, newState, 2);
                    }

                    if (biome0.getTemperature(blockpos1) < 0.15f && canSnowAtBody(lWorld, chunk, blockpos1)) {
                        prevState = chunk.setBlockState(blockpos1, newState = Blocks.SNOW_LAYER.getDefaultState());

                        if(prevState != null)
                            lWorld.markAndNotifyBlock(blockpos1, chunk, prevState, newState, 2);
                    }
                }
            }
        }

        ForgeEventFactory.onChunkPopulate(false, this, lWorld, rand, x, z, false);

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
                world.localSelector.getBiome(pos).getSpawnableList(creatureType) //safe
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
        else if ("Mansion".equals(structureName) && this.woodlandMansionGenerator != null) {
            return this.woodlandMansionGenerator.isInsideStructure(pos);
        } else if ("Monument".equals(structureName) && this.oceanMonumentGenerator != null) {
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
        else if ("Mansion".equals(structureName) && this.woodlandMansionGenerator != null) {
            return this.woodlandMansionGenerator.getNearestStructurePos(worldIn, position, findUnexplored);
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

            if (this.settings.useMansions) {
                this.woodlandMansionGenerator.generate(this.world, x, z, (ChunkPrimer)null);
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