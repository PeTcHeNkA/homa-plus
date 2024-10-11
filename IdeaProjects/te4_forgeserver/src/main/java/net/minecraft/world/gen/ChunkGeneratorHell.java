package net.minecraft.world.gen;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.annotation.Nullable;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockMatcher;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.feature.WorldGenBush;
import net.minecraft.world.gen.feature.WorldGenFire;
import net.minecraft.world.gen.feature.WorldGenGlowStone1;
import net.minecraft.world.gen.feature.WorldGenGlowStone2;
import net.minecraft.world.gen.feature.WorldGenHellLava;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraft.world.gen.structure.MapGenNetherBridge;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.terraingen.ChunkGeneratorEvent;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.event.terraingen.InitNoiseGensEvent;
import net.minecraftforge.event.terraingen.TerrainGen;
import net.minecraftforge.fml.common.eventhandler.Event;
import plus.fserver.event.NetherGenInitEvent;
import plus.fserver.world.generator.map.E2MapGenCavesHell;
import plus.tson.utl.alloc.ConcurrentCasAllocator;


public class ChunkGeneratorHell implements IChunkGenerator {
    protected static final IBlockState AIR = Blocks.AIR.getDefaultState();
    protected static final IBlockState NETHERRACK = Blocks.NETHERRACK.getDefaultState();
    protected static final IBlockState BEDROCK = Blocks.BEDROCK.getDefaultState();
    protected static final IBlockState LAVA = Blocks.LAVA.getDefaultState();
    protected static final IBlockState GRAVEL = Blocks.GRAVEL.getDefaultState();
    protected static final IBlockState SOUL_SAND = Blocks.SOUL_SAND.getDefaultState();
    private final World world;
    private final boolean generateStructures;
    private final Random rand;

    private NoiseGeneratorOctaves lperlinNoise1;
    private NoiseGeneratorOctaves lperlinNoise2;
    private NoiseGeneratorOctaves perlinNoise1;
    private NoiseGeneratorOctaves slowsandGravelNoiseGen;
    private NoiseGeneratorOctaves netherrackExculsivityNoiseGen;
    public NoiseGeneratorOctaves scaleNoise;
    public NoiseGeneratorOctaves depthNoise;
    private final WorldGenFire fireFeature = new WorldGenFire();
    private final WorldGenGlowStone1 lightGemGen = new WorldGenGlowStone1();
    private final WorldGenGlowStone2 hellPortalGen = new WorldGenGlowStone2();
    private final WorldGenerator quartzGen = new WorldGenMinable(Blocks.QUARTZ_ORE.getDefaultState(), 14, BlockMatcher.forBlock(Blocks.NETHERRACK));
    private final WorldGenerator magmaGen = new WorldGenMinable(Blocks.MAGMA.getDefaultState(), 33, BlockMatcher.forBlock(Blocks.NETHERRACK));
    private final WorldGenHellLava lavaTrapGen = new WorldGenHellLava(Blocks.FLOWING_LAVA, true);
    private final WorldGenHellLava hellSpringGen = new WorldGenHellLava(Blocks.FLOWING_LAVA, false);
    private final WorldGenBush brownMushroomFeature = new WorldGenBush(Blocks.BROWN_MUSHROOM);
    private final WorldGenBush redMushroomFeature = new WorldGenBush(Blocks.RED_MUSHROOM);
    private MapGenNetherBridge genNetherBridge = new MapGenNetherBridge();
    //private MapGenBase genNetherCaves = new MapGenCavesHell();
    private final E2MapGenCavesHell genNetherCaves = new E2MapGenCavesHell();

    public ChunkGeneratorHell(World worldIn, boolean structures, long seed) {
        this.world = worldIn;
        NetherGenInitEvent event = new NetherGenInitEvent(world, structures);
        editGenSettings(event);
        this.generateStructures = event.structures;

        //this.generateStructures = structures;
        rand = new Random(seed);
        this.lperlinNoise1 = new NoiseGeneratorOctaves(rand, 16);
        this.lperlinNoise2 = new NoiseGeneratorOctaves(rand, 16);
        this.perlinNoise1 = new NoiseGeneratorOctaves(rand, 8);
        this.slowsandGravelNoiseGen = new NoiseGeneratorOctaves(rand, 4);
        this.netherrackExculsivityNoiseGen = new NoiseGeneratorOctaves(rand, event.solidNoiseOctaves);
        this.scaleNoise = new NoiseGeneratorOctaves(rand, 10);
        this.depthNoise = new NoiseGeneratorOctaves(rand, 16);

        InitNoiseGensEvent.ContextHell ctx =
                new InitNoiseGensEvent.ContextHell(lperlinNoise1, lperlinNoise2, perlinNoise1, slowsandGravelNoiseGen, netherrackExculsivityNoiseGen, scaleNoise, depthNoise);
        ctx = TerrainGen.getModdedNoiseGenerators(worldIn, rand, ctx);
        this.lperlinNoise1 = ctx.getLPerlin1();
        this.lperlinNoise2 = ctx.getLPerlin2();
        this.perlinNoise1 = ctx.getPerlin();
        this.slowsandGravelNoiseGen = ctx.getPerlin2();
        this.netherrackExculsivityNoiseGen = ctx.getPerlin3();
        this.scaleNoise = ctx.getScale();
        this.depthNoise = ctx.getDepth();
        this.genNetherBridge = (MapGenNetherBridge)TerrainGen.getModdedMapGen(genNetherBridge, net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.NETHER_BRIDGE);

        //test
//        {
//            System.out.println("TODO REMOVE THIS");
//            event.liquidLvl = 80;
//            event.liquidState = Blocks.WATER.getDefaultState();
//            event.borderTop = false;
//
//            event.maxHeight   = 250;
//            event.perlinNoiseY  = 512*10;
//            event.perlinNoiseXZ = 512*10;
//            event.depthFactor   = 7;
//
//            event.noiseScaleZX  = 5;
//            event.noiseScaleY   = 5;
//            event.fillFactor    = 55;
//            event.minFillFactor = -75;
//            //event.heightAddFactor /= 2;
//        }

        world.setSeaLevel(this.liquidLvl = event.liquidLvl);
        perlinNoiseXZ = event.perlinNoiseXZ;
        perlinNoiseY  = event.perlinNoiseY;
        maxHeight     = event.maxHeight;
        minHeight     = event.minHeight;
        borderTop     = event.borderTop;
        borderDown    = event.borderDown;
        boundState    = event.boundState;
        fillState     = event.fillState;
        airState      = event.airState;
        gravelState   = event.gravelState;
        sandState     = event.sandState;
        liquidState   = event.liquidState;
        depthFactor   = event.depthFactor;
        fillChance    = event.fillChance;
        noiseScaleXZ  = event.noiseScaleZX;
        noiseScaleY   = event.noiseScaleY;
        fillFactor    = event.fillFactor;

        minFillFactor = event.minFillFactor;
        //heightAddFactor = event.heightAddFactor;
    }


    protected void editGenSettings(NetherGenInitEvent event){
        MinecraftForge.EVENT_BUS.postF(event);
    }


    private final int liquidLvl           ;
    private final double perlinNoiseY     ;
    private final double perlinNoiseXZ    ;
    private final double depthFactor      ;
    private final double fillChance       ;
    private final int maxHeight           ;
    private final int minHeight           ;
    private final double noiseScaleXZ     ;
    private final double noiseScaleY      ;
    private final boolean borderTop       ;
    private final boolean borderDown      ;
    private final double fillFactor       ;
    private final double minFillFactor    ;
    private final IBlockState boundState  ;
    private final IBlockState fillState   ;
    private final IBlockState airState    ;
    private final IBlockState gravelState ;
    private final IBlockState sandState   ;
    private final IBlockState liquidState ;


    public static class GenSession{
        public double[] slowsandNoise;
        public double[] gravelNoise;
        public double[] depthBuffer;
        public double[] buffer;
        public double[] pnr;
        public double[] ar;
        public double[] br;
        public double[] noiseData4;
        public double[] dr;
        public double[] adouble = new double[0];
        public final Random rand = new Random();
        public Biome[] biomes;
        public ChunkPrimer primer;

        public ChunkPrimer primer(){
            ChunkPrimer primer = this.primer;
            if(primer != null){
                primer.clear();
                return primer;
            }
            return this.primer = new ChunkPrimer();
        }
    }


    public void prepareHeights(GenSession session, int cX, int cZ, ChunkPrimer primer) {
        int seaLvl = liquidLvl, maxPy = ((maxHeight+1) >> 3)+1;

        session.buffer = this.getHeights(session, session.buffer, cX * 4, 0, cZ * 4, 5, maxPy, 5);

        double d1, d2, d3, d4, d5, d6, d7, d8, d9, d10, d11, d12, d13, d14;
        for (int num1 = 0, num2, num3, num4, num5, num6; num1 < 4; ++num1) {
            for (num2 = 0; num2 < 4; ++num2) {
                for (num3 = 0; num3 < maxPy-1; ++num3) {
                    d1 =  session.buffer[((num1    ) * 5 + num2    ) * maxPy + num3  ];
                    d2 =  session.buffer[((num1    ) * 5 + num2 + 1) * maxPy + num3  ];
                    d3 =  session.buffer[((num1 + 1) * 5 + num2    ) * maxPy + num3  ];
                    d4 =  session.buffer[((num1 + 1) * 5 + num2 + 1) * maxPy + num3++];
                    //after use num3 + 1
                    d5 = (session.buffer[((num1    ) * 5 + num2    ) * maxPy + num3  ] - d1) * 0.125D;
                    d6 = (session.buffer[((num1    ) * 5 + num2 + 1) * maxPy + num3  ] - d2) * 0.125D;
                    d7 = (session.buffer[((num1 + 1) * 5 + num2    ) * maxPy + num3  ] - d3) * 0.125D;
                    d8 = (session.buffer[((num1 + 1) * 5 + num2 + 1) * maxPy + num3--] - d4) * 0.125D;
                    //after use num3

                    for (num5 = 0; num5 < 8; ++num5) {
                        d9 = d1;
                        d10 = d2;
                        d11 = (d3 - d1) * 0.25D;
                        d12 = (d4 - d2) * 0.25D;

                        for (num6 = 0; num6 < 4; ++num6) {
                            d13 = d9;
                            d14 = (d10 - d9) * 0.25D;

                            for (num4 = 0; num4 < 4; ++num4) {
                                IBlockState iblockstate = airState;

                                if (num3 * 8 + num5 < seaLvl) {
                                    iblockstate = liquidState;
                                }

                                if (d13 > fillFactor || d13 < minFillFactor) {
                                    iblockstate = fillState;
                                }
                                primer.setBlockState(
                                        num6 + (num1 << 2), // * 2
                                        num5 + (num3 << 3),    // * 8
                                        num4 + (num2 << 2),    // * 2
                                        iblockstate
                                );
                                d13 += d14;
                            }
                            d9 += d11;
                            d10 += d12;
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


    public void buildSurfaces(GenSession session, int cX, int cZ, ChunkPrimer primer) {
        if (!ForgeEventFactory.onReplaceBiomeBlocks(this, cX, cZ, primer, this.world)) return;
        int i = this.world.getSeaLevel() + 1;
        session.slowsandNoise = this.slowsandGravelNoiseGen.generateNoiseOctaves(session.slowsandNoise, cX * 16, cZ * 16, 0, 16, 16, 1, 0.03125D, 0.03125D, 1.0D);
        session.gravelNoise = this.slowsandGravelNoiseGen.generateNoiseOctaves(session.gravelNoise, cX * 16, 109, cZ * 16, 16, 1, 16, 0.03125D, 1.0D, 0.03125D);
        session.depthBuffer = this.netherrackExculsivityNoiseGen.generateNoiseOctaves(session.depthBuffer, cX * 16, cZ * 16, 0, 16, 16, 1, 0.0625D, 0.0625D, 0.0625D);

        for (int gX = 0; gX < 16; ++gX) {
            for (int gZ = 0; gZ < 16; ++gZ) {
                boolean flag = session.slowsandNoise[gX + gZ * 16] + session.rand.nextDouble() * 0.2D > 0.0D;
                boolean flag1 = session.gravelNoise[gX + gZ * 16] + session.rand.nextDouble() * 0.2D > 0.0D;
                int depth = (int)(session.depthBuffer[gX + gZ * 16] / depthFactor + depthFactor + session.rand.nextDouble() * fillChance);
                int i1 = -1;
                IBlockState ground = fillState;
                IBlockState dirt = fillState;

                for (int dY = maxHeight; dY >= minHeight; --dY) {
                    if (
                            (!borderTop || (dY < maxHeight - session.rand.nextInt(5)))
                            &&
                            (!borderDown || (dY > session.rand.nextInt(5)))
                    ) {
                        IBlockState state = primer.getBlockState(gZ, dY, gX);

                        if (state.getMaterial() != Material.AIR || state != airState) {
                            if (state.getBlock() == fillState.getBlock()) {
                                if (i1 == -1) {
                                    if (depth <= 0) {
                                        ground = airState;
                                        dirt = fillState;
                                    } else if (dY >= i - 4 && dY <= i + 1) {
                                        ground = fillState;
                                        dirt = fillState;

                                        if (flag1) {
                                            ground = gravelState;
                                            dirt = fillState;
                                        }

                                        if (flag) {
                                            ground = sandState;
                                            dirt = sandState;
                                        }
                                    }

                                    if (dY < i && (ground == null || ground == airState || ground.getMaterial() == Material.AIR)) {
                                        ground = liquidState;
                                    }

                                    i1 = depth;

                                    if (dY >= i - 1) {
                                        primer.setBlockState(gZ, dY, gX, ground);
                                    } else {
                                        primer.setBlockState(gZ, dY, gX, dirt);
                                    }
                                } else if (i1 > 0) {
                                    --i1;
                                    primer.setBlockState(gZ, dY, gX, dirt);
                                }
                            }
                        } else {
                            i1 = -1;
                        }
                    } else {
                        primer.setBlockState(gZ, dY, gX, boundState);
                    }
                }
            }
        }
    }


    private final ConcurrentCasAllocator sessionPool = new ConcurrentCasAllocator(8,4);

    private GenSession makeGenSession() {
        Object session = sessionPool.tryAllocRaw();
        return session==null ? new ChunkGeneratorHell.GenSession() : (GenSession) session;
    }
    private void freeSession(GenSession session) {
        sessionPool.freeObj(session);
    }
    @Override
    public void cleanupCache() {
        sessionPool.clear(1);
    }


    @Override
    public Chunk generateChunk(int x, int z) {
        GenSession session = makeGenSession();
        session.rand.setSeed((long)x * 341873128712L + (long)z * 132897987541L);

        ChunkPrimer chunkprimer = session.primer();
        this.prepareHeights(session, x, z, chunkprimer);
        this.buildSurfaces(session, x, z, chunkprimer);

        this.genNetherCaves.generate(session.rand, this.world, x, z, chunkprimer);

        if (this.generateStructures) {
            this.genNetherBridge.generate(this.world, x, z, chunkprimer);
        }

        Chunk chunk = new Chunk(this.world, chunkprimer, x, z);
        Biome[] abiome = session.biomes =
                this.world.getBiomeProvider().getBiomes(session.biomes, x * 16, z * 16, 16, 16);
        byte[] abyte = chunk.getBiomeArray();

        for (int i = 0; i < abyte.length; ++i) {
            abyte[i] = (byte)Biome.getIdForBiome(abiome[i]);
        }

        freeSession(session);
        chunk.resetRelightChecks();
        return chunk;
    }


    private double[] getHeights(GenSession session, double[] heights, int xOff, int yOff, int zOff, int xSize, int ySize, int zSize) {
        if (heights == null) {
            heights = new double[xSize * ySize * zSize];
        }

        ChunkGeneratorEvent.InitNoiseField event = new ChunkGeneratorEvent.InitNoiseField(this, heights, xOff, yOff, zOff, xSize, ySize, zSize);
        MinecraftForge.EVENT_BUS.postF(event);
        if (event.getResult() == Event.Result.DENY) return event.getNoisefield();

        session.noiseData4 = this.scaleNoise.generateNoiseOctaves(session.noiseData4, xOff, yOff, zOff, xSize, 1, zSize, 1.0D, 0.0D, 1.0D);
        session.dr = this.depthNoise.generateNoiseOctaves(session.dr, xOff, yOff, zOff, xSize, 1, zSize, noiseScaleXZ, noiseScaleY, noiseScaleXZ);
        session.pnr = this.perlinNoise1.generateNoiseOctaves(session.pnr, xOff, yOff, zOff, xSize, ySize, zSize, 8.555150000000001D, 34.2206D, 8.555150000000001D);
        session.ar = this.lperlinNoise1.generateNoiseOctaves(session.ar, xOff, yOff, zOff, xSize, ySize, zSize, perlinNoiseXZ, perlinNoiseY, perlinNoiseXZ);
        session.br = this.lperlinNoise2.generateNoiseOctaves(session.br, xOff, yOff, zOff, xSize, ySize, zSize, perlinNoiseXZ, perlinNoiseY, perlinNoiseXZ);

        double[] adouble;
        if(session.adouble.length == ySize){
            adouble = session.adouble;
        } else {
            adouble = session.adouble = new double[ySize];
        }

        double dub1 = Math.PI * 6.0D / (double)ySize, dub2, dub3, dub4, dub5, dub6;
        int num1 = 0;

        for (boolean flag; num1 < ySize; ++num1) {
            adouble[num1] = Math.cos(num1 * dub1) * 2.0D;

            if (flag = (dub2 = num1) > ySize / 2) {
                dub2 = ySize - 1 - num1;
            }
            if ((flag && borderTop || !flag && borderDown) && dub2 < 4.0D) {
                dub2 = 4.0D - dub2;
                adouble[num1] -= dub2 * dub2 * dub2 * 10.0D;
            }
        }
        num1 = 0;

        for (int num2 = 0, num3, num4; num2 < xSize; ++num2) {
            for (num3 = 0; num3 < zSize; ++num3) {
                for (num4 = 0; num4 < ySize; ++num4) {
                    dub1 = adouble[num4];
                    dub2 = session.ar[num1] / 512.0D;
                    dub3 = session.br[num1] / 512.0D;
                    dub4 = (session.pnr[num1] / 10.0D + 1.0D) / 2.0D;

                    if (dub4 < 0.0D) {
                        dub5 = dub2;
                    } else if (dub4 > 1.0D) {
                        dub5 = dub3;
                    } else {
                        dub5 = dub2 + (dub3 - dub2) * dub4;
                    }

                    dub5 = dub5 - dub1;

                    if (num4 > ySize - 4) {
                        dub6 = (float) (num4 - (ySize - 4)) / 3.0F;
                        dub5 = dub5 * (1.0D - dub6) + -10.0D * dub6;
                    }
                    if (num4 < 0) {
                        dub6 = (0.0D - (double) num4) / 4.0D;
                        dub6 = MathHelper.clamp(dub6, 0.0D, 1.0D);
                        dub5 = dub5 * (1.0D - dub6) + -10.0D * dub6;
                    }
                    heights[num1] = dub5;
                    ++num1;
                }
            }
        }
        return heights;
    }


    @Override
    public void populate(int x, int z) {

        BlockFalling.fallInstantly = true;
        net.minecraftforge.event.ForgeEventFactory.onChunkPopulate(true, this, this.world, this.rand, x, z, false);
        int i = x * 16;
        int j = z * 16;
        BlockPos blockpos = new BlockPos(i, 0, j);
        Biome biome = this.world.getBiome(blockpos.add(16, 0, 16));
        ChunkPos chunkpos = new ChunkPos(x, z);
        this.genNetherBridge.generateStructure(this.world, this.rand, chunkpos);

        if (net.minecraftforge.event.terraingen.TerrainGen.populate(this, this.world, this.rand, x, z, false, net.minecraftforge.event.terraingen.PopulateChunkEvent.Populate.EventType.NETHER_LAVA))
            for (int k = 0; k < 8; ++k)
            {
                this.hellSpringGen.generate(this.world, this.rand, blockpos.add(this.rand.nextInt(16) + 8, this.rand.nextInt(120) + 4, this.rand.nextInt(16) + 8));
            }

        if (net.minecraftforge.event.terraingen.TerrainGen.populate(this, this.world, this.rand, x, z, false, net.minecraftforge.event.terraingen.PopulateChunkEvent.Populate.EventType.FIRE))
            for (int i1 = 0; i1 < this.rand.nextInt(this.rand.nextInt(10) + 1) + 1; ++i1)
            {
                this.fireFeature.generate(this.world, this.rand, blockpos.add(this.rand.nextInt(16) + 8, this.rand.nextInt(120) + 4, this.rand.nextInt(16) + 8));
            }

        if (net.minecraftforge.event.terraingen.TerrainGen.populate(this, this.world, this.rand, x, z, false, net.minecraftforge.event.terraingen.PopulateChunkEvent.Populate.EventType.GLOWSTONE))
        {
            for (int j1 = 0; j1 < this.rand.nextInt(this.rand.nextInt(10) + 1); ++j1)
            {
                this.lightGemGen.generate(this.world, this.rand, blockpos.add(this.rand.nextInt(16) + 8, this.rand.nextInt(120) + 4, this.rand.nextInt(16) + 8));
            }

            for (int k1 = 0; k1 < 10; ++k1)
            {
                this.hellPortalGen.generate(this.world, this.rand, blockpos.add(this.rand.nextInt(16) + 8, this.rand.nextInt(128), this.rand.nextInt(16) + 8));
            }
        }//Forge: End doGLowstone

        net.minecraftforge.event.ForgeEventFactory.onChunkPopulate(false, this, this.world, this.rand, x, z, false);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.terraingen.DecorateBiomeEvent.Pre(this.world, this.rand, chunkpos));

        if (net.minecraftforge.event.terraingen.TerrainGen.decorate(this.world, this.rand, chunkpos, net.minecraftforge.event.terraingen.DecorateBiomeEvent.Decorate.EventType.SHROOM))
        {
            if (this.rand.nextBoolean())
            {
                this.brownMushroomFeature.generate(this.world, this.rand, blockpos.add(this.rand.nextInt(16) + 8, this.rand.nextInt(128), this.rand.nextInt(16) + 8));
            }

            if (this.rand.nextBoolean())
            {
                this.redMushroomFeature.generate(this.world, this.rand, blockpos.add(this.rand.nextInt(16) + 8, this.rand.nextInt(128), this.rand.nextInt(16) + 8));
            }
        }


        if (net.minecraftforge.event.terraingen.TerrainGen.generateOre(this.world, this.rand, quartzGen, blockpos, net.minecraftforge.event.terraingen.OreGenEvent.GenerateMinable.EventType.QUARTZ))
            for (int l1 = 0; l1 < 16; ++l1)
            {
                this.quartzGen.generate(this.world, this.rand, blockpos.add(this.rand.nextInt(16), this.rand.nextInt(108) + 10, this.rand.nextInt(16)));
            }

        int i2 = this.world.getSeaLevel() / 2 + 1;

        if (net.minecraftforge.event.terraingen.TerrainGen.populate(this, this.world, this.rand, x, z, false, net.minecraftforge.event.terraingen.PopulateChunkEvent.Populate.EventType.NETHER_MAGMA))
            for (int l = 0; l < 4; ++l)
            {
                this.magmaGen.generate(this.world, this.rand, blockpos.add(this.rand.nextInt(16), i2 - 5 + this.rand.nextInt(10), this.rand.nextInt(16)));
            }

        if (net.minecraftforge.event.terraingen.TerrainGen.populate(this, this.world, this.rand, x, z, false, net.minecraftforge.event.terraingen.PopulateChunkEvent.Populate.EventType.NETHER_LAVA2))
            for (int j2 = 0; j2 < 16; ++j2)
            {
                int offset = net.minecraftforge.common.ForgeModContainer.fixVanillaCascading ? 8 : 0; // MC-117810
                this.lavaTrapGen.generate(this.world, this.rand, blockpos.add(this.rand.nextInt(16) + offset, this.rand.nextInt(108) + 10, this.rand.nextInt(16) + offset));
            }

        biome.decorate(this.world, this.rand, new BlockPos(i, 0, j));

        MinecraftForge.EVENT_BUS.post(new DecorateBiomeEvent.Post(this.world, this.rand, blockpos));

        BlockFalling.fallInstantly = false;
    }


    @Override
    public boolean generateStructures(Chunk chunkIn, int x, int z) {
        return false;
    }


    @Override
    public List<Biome.SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos) {
        if (creatureType == EnumCreatureType.MONSTER) {
            if (this.genNetherBridge.isInsideStructure(pos)) {
                return this.genNetherBridge.getSpawnList();
            }

            if (this.genNetherBridge.isPositionInStructure(this.world, pos) && this.world.getBlockState(pos.down()).getBlock() == Blocks.NETHER_BRICK) {
                return this.genNetherBridge.getSpawnList();
            }
        }
        Biome biome = this.world.getBiome(pos);
        return biome.getSpawnableList(creatureType);
    }


    @Nullable
    public BlockPos getNearestStructurePos(World worldIn, String structureName, BlockPos position, boolean findUnexplored) {
        return "Fortress".equals(structureName) && this.genNetherBridge != null ? this.genNetherBridge.getNearestStructurePos(worldIn, position, findUnexplored) : null;
    }


    @Override
    public boolean isInsideStructure(World worldIn, String structureName, BlockPos pos) {
        return "Fortress".equals(structureName) && this.genNetherBridge != null ? this.genNetherBridge.isInsideStructure(pos) : false;
    }


    @Override
    public void recreateStructures(Chunk chunkIn, int x, int z) {
        this.genNetherBridge.generate(this.world, x, z, null);
    }


    @Override
    public boolean maybeAsync() {
        return true;
    }


    @Override
    public boolean maybeAsyncPopulate() {
        return false;
    }
}