package net.minecraft.world.biome;

import java.util.Random;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.BlockFlower.EnumFlowerType;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.ChunkGeneratorSettings;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;
import net.minecraft.world.gen.feature.WorldGenBigMushroom;
import net.minecraft.world.gen.feature.WorldGenBush;
import net.minecraft.world.gen.feature.WorldGenCactus;
import net.minecraft.world.gen.feature.WorldGenClay;
import net.minecraft.world.gen.feature.WorldGenDeadBush;
import net.minecraft.world.gen.feature.WorldGenFlowers;
import net.minecraft.world.gen.feature.WorldGenLiquids;
import net.minecraft.world.gen.feature.WorldGenPumpkin;
import net.minecraft.world.gen.feature.WorldGenReed;
import net.minecraft.world.gen.feature.WorldGenSand;
import net.minecraft.world.gen.feature.WorldGenWaterlily;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.event.terraingen.OreGenEvent;
import plus.fserver.api.ElcaWorldInfo;
import plus.fserver.server.elca.ChunkSelector;
import plus.fserver.world.generator.ChunkGenSettingsPlus;


public class BiomeDecorator {
    public boolean decorating;
    public BlockPos chunkPos;
    public ChunkGeneratorSettings chunkProviderSettings;
    public WorldGenerator clayGen = new WorldGenClay(4);
    public WorldGenerator sandGen;
    public WorldGenerator gravelGen;
    public WorldGenerator dirtGen;
    public WorldGenerator gravelOreGen;
    public WorldGenerator graniteGen;
    public WorldGenerator dioriteGen;
    public WorldGenerator andesiteGen;
    public WorldGenerator coalGen;
    public WorldGenerator ironGen;
    public WorldGenerator goldGen;
    public WorldGenerator redstoneGen;
    public WorldGenerator diamondGen;
    public WorldGenerator lapisGen;
    public WorldGenFlowers flowerGen;
    public WorldGenerator mushroomBrownGen;
    public WorldGenerator mushroomRedGen;
    public WorldGenerator bigMushroomGen;
    public WorldGenerator reedGen;
    public WorldGenerator cactusGen;
    public WorldGenerator waterlilyGen;
    public int waterlilyPerChunk;
    public int treesPerChunk;
    public float extraTreeChance;
    public int flowersPerChunk;
    public int grassPerChunk;
    public int deadBushPerChunk;
    public int mushroomsPerChunk;
    public int reedsPerChunk;
    public int cactiPerChunk;
    public int gravelPatchesPerChunk;
    public int sandPatchesPerChunk;
    public int clayPerChunk;
    public int bigMushroomsPerChunk;
    public boolean generateFalls;

    public BiomeDecorator() {
        this.sandGen = new WorldGenSand(Blocks.SAND, 7);
        this.gravelGen = new WorldGenSand(Blocks.GRAVEL, 6);
        this.flowerGen = new WorldGenFlowers(Blocks.YELLOW_FLOWER, EnumFlowerType.DANDELION);
        this.mushroomBrownGen = new WorldGenBush(Blocks.BROWN_MUSHROOM);
        this.mushroomRedGen = new WorldGenBush(Blocks.RED_MUSHROOM);
        this.bigMushroomGen = new WorldGenBigMushroom();
        this.reedGen = new WorldGenReed();
        this.cactusGen = new WorldGenCactus();
        this.waterlilyGen = new WorldGenWaterlily();
        this.extraTreeChance = 0.1F;
        this.flowersPerChunk = 2;
        this.grassPerChunk = 1;
        this.gravelPatchesPerChunk = 1;
        this.sandPatchesPerChunk = 3;
        this.clayPerChunk = 1;
        this.generateFalls = true;
    }


    public void decorate(final World worldIn, final Random random, final Biome biome, final BlockPos chunkPos) {
        genDecorations(biome, worldIn, random, chunkPos);
    }


    protected void genDecorations(final Biome biome, final World worldIn, final Random random, final BlockPos chunkPos) {
        ChunkPos fPos = new ChunkPos(chunkPos);

        MinecraftForge.EVENT_BUS.post(new DecorateBiomeEvent.Pre(worldIn, random, fPos));

        ChunkSelector selector = new ChunkSelector(worldIn);
        WorldGenerator gen = new WorldGenDeadBush();

        this.generateOres(selector, worldIn, random, chunkPos);

        for (int i = 0; i < this.sandPatchesPerChunk; ++i) {
            int j = random.nextInt(16) + 8;
            int k = random.nextInt(16) + 8;
            this.sandGen.generate(worldIn, random, selector.getTopSolidOrLiquidBlock(chunkPos.add(j, 0, k)));
        }

        for (int i1 = 0; i1 < this.clayPerChunk; ++i1) {
            int l1 = random.nextInt(16) + 8;
            int i6 = random.nextInt(16) + 8;
            this.clayGen.generate(worldIn, random, selector.getTopSolidOrLiquidBlock(chunkPos.add(l1, 0, i6)));
        }

        for (int j1 = 0; j1 < this.gravelPatchesPerChunk; ++j1) {
            int i2 = random.nextInt(16) + 8;
            int j6 = random.nextInt(16) + 8;
            this.gravelGen.generate(worldIn, random, selector.getTopSolidOrLiquidBlock(chunkPos.add(i2, 0, j6)));
        }

        int k1 = this.treesPerChunk;

        if (random.nextFloat() < this.extraTreeChance) {
            ++k1;
        }

        for (int j2 = 0; j2 < k1; ++j2) {
            int k6 = random.nextInt(16) + 8;
            int l = random.nextInt(16) + 8;
            WorldGenAbstractTree threeGen = biome.getRandomTreeFeature(random);
            threeGen.setDecorationDefaults();
            BlockPos blockpos = selector.getHeight(chunkPos.add(k6, 0, l));

            if (threeGen.generate(worldIn, random, blockpos)) {
                threeGen.generateSaplings(worldIn, random, blockpos);
            }
        }

        for (int k2 = 0; k2 < this.bigMushroomsPerChunk; ++k2) {
            int l6 = random.nextInt(16) + 8;
            int k10 = random.nextInt(16) + 8;
            this.bigMushroomGen.generate(worldIn, selector, random, selector.getHeight(chunkPos.add(l6, 0, k10)));
        }

        for (int l2 = 0; l2 < this.flowersPerChunk; ++l2) {
            int i7 = random.nextInt(16) + 8;
            int l10 = random.nextInt(16) + 8;
            int j14 = selector.getHeight(chunkPos.add(i7, 0, l10)).getY() + 32;

            if (j14 > 0) {
                int k17 = random.nextInt(j14);
                BlockPos blockpos1 = chunkPos.add(i7, k17, l10);
                BlockFlower.EnumFlowerType flowerType = biome.pickRandomFlower(random, blockpos1);
                BlockFlower blockflower = flowerType.getBlockType().getBlock();

                //todo fix async
                if (blockflower.getDefaultState().getMaterial() != Material.AIR) {
                    this.flowerGen.setGeneratedBlock(blockflower, flowerType);
                    this.flowerGen.generate(worldIn, selector, random, blockpos1);
                }
            }
        }

        for (int i3 = 0; i3 < this.grassPerChunk; ++i3) {
            int j7 = random.nextInt(16) + 8;
            int i11 = random.nextInt(16) + 8;
            int k14 = selector.getHeight(chunkPos.add(j7, 0, i11)).getY() * 2;

            if (k14 > 0) {
                int l17 = random.nextInt(k14);
                biome.getRandomWorldGenForGrass(random).generate(worldIn, selector, random, chunkPos.add(j7, l17, i11));
            }
        }

        for (int j3 = 0; j3 < this.deadBushPerChunk; ++j3) {
            int k7 = random.nextInt(16) + 8;
            int j11 = random.nextInt(16) + 8;
            int l14 = selector.getHeight(chunkPos.add(k7, 0, j11)).getY() * 2;

            if (l14 > 0) {
                int i18 = random.nextInt(l14);
                gen.generate(worldIn, selector, random, chunkPos.add(k7, i18, j11));
            }
        }

        for (int k3 = 0; k3 < this.waterlilyPerChunk; ++k3) {
            int l7 = random.nextInt(16) + 8;
            int k11 = random.nextInt(16) + 8;
            int i15 = selector.getHeight(chunkPos.add(l7, 0, k11)).getY() * 2;

            if (i15 > 0) {
                int j18 = random.nextInt(i15);
                BlockPos blockpos4;
                BlockPos blockpos7;

                for (blockpos4 = chunkPos.add(l7, j18, k11);blockpos4.getY() > 0;blockpos4 = blockpos7) {
                    blockpos7 = blockpos4.down();
                    if (!selector.isAirBlock(blockpos7)) {
                        break;
                    }
                }
                this.waterlilyGen.generate(worldIn, selector, random, blockpos4);
            }
        }

        for (int l3 = 0; l3 < this.mushroomsPerChunk; ++l3) {
            if (random.nextInt(4) == 0) {
                int i8 = random.nextInt(16) + 8;
                int l11 = random.nextInt(16) + 8;
                BlockPos blockpos2 = selector.getHeight(chunkPos.add(i8, 0, l11));
                this.mushroomBrownGen.generate(worldIn, selector, random, blockpos2);
            }

            if (random.nextInt(8) == 0) {
                int j8 = random.nextInt(16) + 8;
                int i12 = random.nextInt(16) + 8;
                int j15 = selector.getHeight(chunkPos.add(j8, 0, i12)).getY() * 2;

                if (j15 > 0) {
                    int k18 = random.nextInt(j15);
                    BlockPos blockpos5 = chunkPos.add(j8, k18, i12);
                    this.mushroomRedGen.generate(worldIn, selector, random, blockpos5);
                }
            }
        }

        if (random.nextInt(4) == 0) {
            int i4 = random.nextInt(16) + 8;
            int k8 = random.nextInt(16) + 8;
            int j12 = selector.getHeight(chunkPos.add(i4, 0, k8)).getY() * 2;

            if (j12 > 0) {
                int k15 = random.nextInt(j12);
                this.mushroomBrownGen.generate(worldIn, selector, random, chunkPos.add(i4, k15, k8));
            }
        }

        if (random.nextInt(8) == 0) {
            int j4 = random.nextInt(16) + 8;
            int l8 = random.nextInt(16) + 8;
            int k12 = selector.getHeight(chunkPos.add(j4, 0, l8)).getY() * 2;
            if (k12 > 0) {
                int l15 = random.nextInt(k12);
                this.mushroomRedGen.generate(worldIn, selector, random, chunkPos.add(j4, l15, l8));
            }
        }

        for (int k4 = 0; k4 < this.reedsPerChunk; ++k4) {
            int i9 = random.nextInt(16) + 8;
            int l12 = random.nextInt(16) + 8;
            int i16 = selector.getHeight(chunkPos.add(i9, 0, l12)).getY() * 2;

            if (i16 > 0) {
                int l18 = random.nextInt(i16);
                this.reedGen.generate(worldIn, selector, random, chunkPos.add(i9, l18, l12));
            }
        }

        for (int l4 = 0; l4 < 10; ++l4) {
            int j9 = random.nextInt(16) + 8;
            int i13 = random.nextInt(16) + 8;
            int j16 = selector.getHeight(chunkPos.add(j9, 0, i13)).getY() * 2;

            if (j16 > 0) {
                int i19 = random.nextInt(j16);
                this.reedGen.generate(worldIn, selector, random, chunkPos.add(j9, i19, i13));
            }
        }

        if (random.nextInt(32) == 0) {
            int i5 = random.nextInt(16) + 8;
            int k9 = random.nextInt(16) + 8;
            int j13 = selector.getHeight(chunkPos.add(i5, 0, k9)).getY() * 2;
            if (j13 > 0) {
                int k16 = random.nextInt(j13);
                (new WorldGenPumpkin()).generate(worldIn, selector, random, chunkPos.add(i5, k16, k9));
            }
        }

        for (int j5 = 0; j5 < this.cactiPerChunk; ++j5) {
            int l9 = random.nextInt(16) + 8;
            int k13 = random.nextInt(16) + 8;
            int l16 = selector.getHeight(chunkPos.add(l9, 0, k13)).getY() * 2;

            if (l16 > 0) {
                int j19 = random.nextInt(l16);
                this.cactusGen.generate(worldIn, selector, random, chunkPos.add(l9, j19, k13));
            }
        }

        if (this.generateFalls) {
            gen = new WorldGenLiquids(Blocks.FLOWING_WATER);
            for (int k5 = 0; k5 < 50; ++k5) {
                int i10 = random.nextInt(16) + 8;
                int l13 = random.nextInt(16) + 8;
                int i17 = random.nextInt(248) + 8;

                int k19 = random.nextInt(i17);
                BlockPos blockpos6 = chunkPos.add(i10, k19, l13);
                gen.generate(worldIn, random, blockpos6);
            }
            gen = new WorldGenLiquids(Blocks.FLOWING_LAVA);

            for (int l5 = 0; l5 < 20; ++l5) {
                int j10 = random.nextInt(16) + 8;
                int i14 = random.nextInt(16) + 8;
                int j17 = random.nextInt(random.nextInt(random.nextInt(240) + 8) + 8);
                BlockPos blockpos3 = chunkPos.add(j10, j17, i14);
                gen.generate(worldIn, random, blockpos3);
            }
        }
        MinecraftForge.EVENT_BUS.post(new DecorateBiomeEvent.Post(worldIn, random, fPos));
    }


    @Deprecated
    protected void genDecorations(Biome biome, World world, Random rand) {
        genDecorations(biome, world, rand, this.chunkPos);
    }


    @Deprecated
    protected void generateOres(World world, Random rand) {
        generateOres(new ChunkSelector(world), world, rand, this.chunkPos);
    }


    @Deprecated
    protected void genStandardOre1(World world, Random rand, int max, WorldGenerator gen, int minY, int maxY) {
        int j;
        if (maxY < minY) {
            j = minY;
            minY = maxY;
            maxY = j;
        } else if (maxY == minY) {
            if (minY < 255) {
                ++maxY;
            } else {
                --minY;
            }
        }

        for(j = 0; j < max; ++j) {
            BlockPos blockpos = this.chunkPos.add(rand.nextInt(16), rand.nextInt(maxY - minY) + minY, rand.nextInt(16));
            gen.generate(world, rand, blockpos);
        }
    }


    @Deprecated
    protected void genStandardOre2(World world, Random rand, int max, WorldGenerator gen, int yf1, int yf2) {
        for(int i = 0; i < max; ++i) {
            BlockPos blockpos = this.chunkPos.add(rand.nextInt(16), rand.nextInt(yf2) + rand.nextInt(yf2) + yf1 - yf2, rand.nextInt(16));
            gen.generate(world, rand, blockpos);
        }
    }


    protected final void generateOres(final ChunkSelector selector, final World worldIn, final Random random, final BlockPos chunkPos) {
        MinecraftForge.ORE_GEN_BUS.post(new OreGenEvent.Pre(worldIn, random, chunkPos));

        ChunkGenSettingsPlus settings = ((ElcaWorldInfo) worldIn.getWorldInfo()).getChunkGenSettings();

        genOre1(selector, worldIn, chunkPos, random, settings.settings.dirtCount,    settings.dirtGen,     settings.settings.dirtMinHeight,    settings.settings.dirtMaxHeight    );
        genOre1(selector, worldIn, chunkPos, random, settings.settings.gravelCount,  settings.gravelOreGen,settings.settings.gravelMinHeight,  settings.settings.gravelMaxHeight  );
        genOre1(selector, worldIn, chunkPos, random, settings.settings.dioriteCount, settings.dioriteGen,  settings.settings.dioriteMinHeight, settings.settings.dioriteMaxHeight );
        genOre1(selector, worldIn, chunkPos, random, settings.settings.graniteCount, settings.graniteGen,  settings.settings.graniteMinHeight, settings.settings.graniteMaxHeight );
        genOre1(selector, worldIn, chunkPos, random, settings.settings.andesiteCount,settings.andesiteGen, settings.settings.andesiteMinHeight,settings.settings.andesiteMaxHeight);
        genOre1(selector, worldIn, chunkPos, random, settings.settings.coalCount,    settings.coalGen,     settings.settings.coalMinHeight,    settings.settings.coalMaxHeight    );
        genOre1(selector, worldIn, chunkPos, random, settings.settings.ironCount,    settings.ironGen,     settings.settings.ironMinHeight,    settings.settings.ironMaxHeight    );
        genOre1(selector, worldIn, chunkPos, random, settings.settings.goldCount,    settings.goldGen,     settings.settings.goldMinHeight,    settings.settings.goldMaxHeight    );
        genOre1(selector, worldIn, chunkPos, random, settings.settings.redstoneCount,settings.redstoneGen, settings.settings.redstoneMinHeight,settings.settings.redstoneMaxHeight);
        genOre1(selector, worldIn, chunkPos, random, settings.settings.diamondCount, settings.diamondGen,  settings.settings.diamondMinHeight, settings.settings.diamondMaxHeight );
        genOre2(selector, worldIn, chunkPos, random, settings.settings.lapisCount,   settings.lapisGen,    settings.settings.lapisCenterHeight,settings.settings.lapisSpread      );

        MinecraftForge.ORE_GEN_BUS.post(new OreGenEvent.Post(worldIn, random, chunkPos));
    }


    protected final void genOre1(final ChunkSelector selector, final World worldIn, final BlockPos chunkPos, final Random random, int blockCount, WorldGenerator generator, int minHeight, int maxHeight) {
        if (maxHeight < minHeight) {
            int i = minHeight;
            minHeight = maxHeight;
            maxHeight = i;
        } else if (maxHeight == minHeight) {
            if (minHeight < 255) {
                ++maxHeight;
            } else {
                --minHeight;
            }
        }
        for (int i = 0; i < blockCount; ++i) {
            int rNum = random.nextInt();
            BlockPos blockpos = chunkPos.add(
                    rNum & 15,
                    random.nextInt(maxHeight - minHeight) + minHeight,
                    (rNum >> 4) &15
            );
            generator.generate(worldIn, selector, random, blockpos);
        }
    }


    protected final void genOre2(final ChunkSelector selector, final World worldIn, final BlockPos chunkPos, final Random random, int blockCount, WorldGenerator generator, int centerHeight, int spread) {
        for (int i = 0; i < blockCount; ++i) {
            BlockPos blockpos = chunkPos.add(random.nextInt(16), random.nextInt(spread) + random.nextInt(spread) + centerHeight - spread, random.nextInt(16));
            generator.generate(worldIn, selector, random, blockpos);
        }
    }
}