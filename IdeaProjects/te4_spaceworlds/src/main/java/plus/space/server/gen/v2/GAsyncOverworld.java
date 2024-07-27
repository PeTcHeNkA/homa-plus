package plus.space.server.gen.v2;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockIDIndexes;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.*;
import net.minecraft.world.gen.structure.*;
import net.minecraftforge.event.terraingen.InitMapGenEvent;
import net.minecraftforge.event.terraingen.InitNoiseGensEvent;
import net.minecraftforge.event.terraingen.TerrainGen;
import plus.fserver.world.generator.GenSession;
import plus.fserver.world.generator.biome.BiomeProviderEx;
import plus.fserver.world.generator.map.EMapGenBase;
import plus.fserver.world.generator.map.EMapGenCaves;
import plus.fserver.world.generator.map.EMapGenRavine;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;


public class GAsyncOverworld implements CChunkGenerator.Generator{
    private NoiseGeneratorOctaves minLimitPerlinNoise;
    private NoiseGeneratorOctaves maxLimitPerlinNoise;
    private NoiseGeneratorOctaves mainPerlinNoise;
    private NoiseGeneratorPerlin surfaceNoise;
    private NoiseGeneratorOctaves depthNoise;
    private final World world;
    private final boolean mapFeaturesEnabled;
    private final WorldType terrainType;
    private ChunkGeneratorSettings settings;
    private IBlockState oceanBlock = Blocks.WATER.getDefaultState();
    private final EMapGenBase caveGenerator = new EMapGenCaves();
    private final EMapGenBase ravineGenerator = new EMapGenRavine();

    protected MapGenStronghold strongholdGenerator = new MapGenStronghold();
    protected MapGenVillage villageGenerator = new MapGenVillage();
    protected MapGenMineshaft mineshaftGenerator = new MapGenMineshaft();
    protected MapGenScatteredFeature scatteredFeatureGenerator = new MapGenScatteredFeature();
    protected StructureOceanMonument oceanMonumentGenerator = new StructureOceanMonument();
    protected WoodlandMansionMapGen woodlandMansionGenerator = new WoodlandMansionMapGen(this);

    private final IChunkGenerator parent;


    public GAsyncOverworld(Random rand, IChunkGenerator parent, World worldIn, boolean mapFeaturesEnabledIn, String generatorOptions) {
        this.parent = parent;
        if(this.mapFeaturesEnabled = mapFeaturesEnabledIn){
            strongholdGenerator = (MapGenStronghold)net.minecraftforge.event.terraingen.TerrainGen.getModdedMapGen(strongholdGenerator, net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.STRONGHOLD);
            villageGenerator = (MapGenVillage)net.minecraftforge.event.terraingen.TerrainGen.getModdedMapGen(villageGenerator, net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.VILLAGE);
            mineshaftGenerator = (MapGenMineshaft)net.minecraftforge.event.terraingen.TerrainGen.getModdedMapGen(mineshaftGenerator, net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.MINESHAFT);
            scatteredFeatureGenerator = (MapGenScatteredFeature)net.minecraftforge.event.terraingen.TerrainGen.getModdedMapGen(scatteredFeatureGenerator, net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.SCATTERED_FEATURE);

            oceanMonumentGenerator = (StructureOceanMonument)net.minecraftforge.event.terraingen.TerrainGen.getModdedMapGen(oceanMonumentGenerator, net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.OCEAN_MONUMENT);
            woodlandMansionGenerator = (WoodlandMansionMapGen)TerrainGen.getModdedMapGen(woodlandMansionGenerator, InitMapGenEvent.EventType.WOODLAND_MANSION);
        }
        this.world = worldIn;

        this.terrainType = worldIn.getWorldInfo().getTerrainType();

        //this.seed = seed;
        this.minLimitPerlinNoise = new NoiseGeneratorOctaves(rand, 16);
        this.maxLimitPerlinNoise = new NoiseGeneratorOctaves(rand, 16);
        this.mainPerlinNoise = new NoiseGeneratorOctaves(rand, 8);
        this.surfaceNoise = new NoiseGeneratorPerlin(rand, 4);
        NoiseGeneratorOctaves scaleNoise = new NoiseGeneratorOctaves(rand, 0);
        this.depthNoise = new NoiseGeneratorOctaves(rand, 16);
        NoiseGeneratorOctaves forestNoise = new NoiseGeneratorOctaves(rand, 0);

        if (generatorOptions != null) {
            this.settings = ChunkGeneratorSettings.Factory.jsonToFactory(generatorOptions).build();
            this.oceanBlock = this.settings.useLavaOceans ? Blocks.LAVA.getDefaultState() : Blocks.WATER.getDefaultState();
            worldIn.setSeaLevel(this.settings.seaLevel);
        }

        InitNoiseGensEvent.ContextOverworld ctx =
                new net.minecraftforge.event.terraingen.InitNoiseGensEvent.ContextOverworld(minLimitPerlinNoise, maxLimitPerlinNoise, mainPerlinNoise, surfaceNoise, scaleNoise, depthNoise, forestNoise);
        ctx = TerrainGen.getModdedNoiseGenerators(worldIn, rand, ctx);
        this.minLimitPerlinNoise = ctx.getLPerlin1();
        this.maxLimitPerlinNoise = ctx.getLPerlin2();
        this.mainPerlinNoise = ctx.getPerlin();
        this.surfaceNoise = ctx.getHeight();
        this.depthNoise = ctx.getDepth();
    }


    private final ConcurrentLinkedQueue<GenSession> sessionPool = new ConcurrentLinkedQueue<>();

    private GenSession makeGenSession() {
        GenSession session = sessionPool.poll();
        return session==null ? new GenSession() : session;
    }

    private void freeSession(GenSession session) {
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


    @Override
    public void setBlocksInChunk(int x, int z, ChunkPrimer primer) {
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

        for (int i = 0; i < 4; ++i) {
            int j = i * 5;
            int k = (i + 1) * 5;

            for (int l = 0; l < 4; ++l) {
                int i1 = (j + l) * 33;
                int j1 = (j + l + 1) * 33;
                int k1 = (k + l) * 33;
                int l1 = (k + l + 1) * 33;

                for (int i2 = 0; i2 < 32; ++i2) {
                    double d1 = session.heightMap[i1 + i2];
                    double d2 = session.heightMap[j1 + i2];
                    double d3 = session.heightMap[k1 + i2];
                    double d4 = session.heightMap[l1 + i2];
                    double d5 = (session.heightMap[i1 + i2 + 1] - d1) * 0.125D;
                    double d6 = (session.heightMap[j1 + i2 + 1] - d2) * 0.125D;
                    double d7 = (session.heightMap[k1 + i2 + 1] - d3) * 0.125D;
                    double d8 = (session.heightMap[l1 + i2 + 1] - d4) * 0.125D;

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
                                } else if (i2 * 8 + j2 < this.settings.seaLevel) {
                                    primer.setBlockState(i * 4 + k2, i2 * 8 + j2, l * 4 + l2, this.oceanBlock);
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
        if (!net.minecraftforge.event.ForgeEventFactory.onReplaceBiomeBlocks(parent, x, z, primer, this.world)) return;
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
        BiomeProvider provider = this.world.getBiomeProvider();
        GenSession session = makeGenSession();

        session.rand.setSeed((long)x * 341873128712L + (long)z * 132897987541L);
        ChunkPrimer chunkprimer = new ChunkPrimer();
        this.setBlocksInChunk(session, x, z, chunkprimer);

        if(provider instanceof BiomeProviderEx){
            session.biomesForGeneration = ((BiomeProviderEx)provider)
                    .getBiomes(session, session.biomesForGeneration, x * 16, z * 16, 16, 16, true);
        } else {
            session.biomesForGeneration = provider
                    .getBiomes(session.biomesForGeneration, x * 16, z * 16, 16, 16);
        }

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

            if (this.settings.useMansions) {
                this.woodlandMansionGenerator.generate(this.world, x, z, chunkprimer);
            }
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


    private void generateHeightmap(GenSession session, int x, int y, int z) {
        session.depthRegion = this.depthNoise.generateNoiseOctaves(session.depthRegion, x, z, 5, 5, (double)this.settings.depthNoiseScaleX, this.settings.depthNoiseScaleZ, this.settings.depthNoiseScaleExponent);
        float f0 = this.settings.coordinateScale;
        float f1 = this.settings.heightScale;
        session.mainNoiseRegion = this.mainPerlinNoise.generateNoiseOctaves(session.mainNoiseRegion, x, y, z, 5, 33, 5, f0 / this.settings.mainNoiseScaleX, f1 / this.settings.mainNoiseScaleY, f0 / this.settings.mainNoiseScaleZ);
        session.minLimitRegion = this.minLimitPerlinNoise.generateNoiseOctaves(session.minLimitRegion, x, y, z, 5, 33, 5, f0, f1, f0);
        session.maxLimitRegion = this.maxLimitPerlinNoise.generateNoiseOctaves(session.maxLimitRegion, x, y, z, 5, 33, 5, f0, f1, f0);

        int i = 0, j = 0;
        float f2, f3, f4, f5;

        for (int k = 0; k < 5; ++k) {
            for (int l = 0; l < 5; ++l) {
                f2 = 0.0F;
                f3 = 0.0F;
                f4 = 0.0F;
                Biome biome = session.biomesForGeneration[k + 2 + (l + 2) * 10];

                for (int j1 = -2; j1 <= 2; ++j1) {
                    for (int k1 = -2; k1 <= 2; ++k1) {
                        Biome biome1 = session.biomesForGeneration[k + j1 + 2 + (l + k1 + 2) * 10];
                        f5 = this.settings.biomeDepthOffSet + biome1.getBaseHeight() * this.settings.biomeDepthWeight;
                        f0 = this.settings.biomeScaleOffset + biome1.getHeightVariation() * this.settings.biomeScaleWeight;

                        if (this.terrainType == WorldType.AMPLIFIED && f5 > 0.0F) {
                            f5 = 1.0F + f5 * 2.0F;
                            f0 = 1.0F + f0 * 4.0F;
                        }

                        f0 = session.biomeWeights[j1 + 2 + (k1 + 2) * 5] / (f5 + 2.0F);

                        if (biome1.getBaseHeight() > biome.getBaseHeight()) {
                            f0 /= 2.0F;
                        }

                        f2 += f0 * f0;
                        f3 += f5 * f0;
                        f4 += f0;
                    }
                }
                f2 = f2 / f4;
                f3 = f3 / f4;
                f2 = f2 * 0.9F + 0.1F;
                f3 = (f3 * 4.0F - 1.0F) / 8.0F;
                double temp0 = session.depthRegion[j] / 8000.0D;

                if (temp0 < 0.0D) {
                    temp0 = -temp0 * 0.3D;
                }

                temp0 = temp0 * 3.0D - 2.0D;

                if (temp0 < 0.0D) {
                    temp0 = temp0 / 2.0D;

                    if (temp0 < -1.0D) {
                        temp0 = -1.0D;
                    }

                    temp0 = temp0 / 1.4D;
                    temp0 = temp0 / 2.0D;
                } else {
                    if (temp0 > 1.0D) {
                        temp0 = 1.0D;
                    }
                    temp0 = temp0 / 8.0D;
                }

                ++j;
                double temp1 = f3;
                double boost1 = (double)this.settings.stretchY * 128.0D / 256.0D / f2;

                temp1 = temp1 + temp0 * 0.2D;
                temp1 = temp1 * (double)this.settings.baseSize / 8.0D;
                double delta = (double)this.settings.baseSize + temp1 * 4.0D;

                for (int l1 = 0; l1 < 33; ++l1) {
                    temp1 = ((double)l1 - delta) * boost1;

                    if (temp1 < 0.0D) {
                        temp1 *= 4.0D;
                    }

                    temp0 = session.minLimitRegion[i] / (double)this.settings.lowerLimitScale;
                    double temp2 = session.maxLimitRegion[i] / (double)this.settings.upperLimitScale;
                    double temp3 = (session.mainNoiseRegion[i] / 10.0D + 1.0D) / 2.0D;
                    temp3 = MathHelper.clampedLerp(temp0, temp2, temp3) - temp1;

                    if (l1 > 29) {
                        temp2 = (float)(l1 - 29) / 3.0F;
                        temp3 = temp3 * (1.0D - temp2) + -10.0D * temp2;
                    }

                    session.heightMap[i] = temp3;
                    ++i;
                }
            }
        }
    }


    @Override
    public boolean maybeAsync() {
        return true;
    }
}
