package net.minecraft.world.biome;

import java.util.Random;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.BlockFlower.EnumFlowerType;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
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
import plus.fserver.server.elca.ChunkSelector;
import plus.fserver.utls.math.BlockPosDelta;
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


    protected void genDecorations(final Biome biome, final World world, final Random rand, final BlockPos chunkPos) {
        ChunkPos fPos = new ChunkPos(chunkPos);

        MinecraftForge.EVENT_BUS.postF(new DecorateBiomeEvent.Pre(world, rand, fPos));

        ChunkSelector selector = new ChunkSelector(world);
        WorldGenerator gen = new WorldGenDeadBush();

        this.generateOres(selector, world, rand, chunkPos);
        BlockPosDelta cRel = new BlockPosDelta(chunkPos);

        int rX, rY, rZ, i;

        for (i = 0; i < this.sandPatchesPerChunk; ++i) {
            rX = (rand.nextInt() & 15) + 8;
            rZ = (rand.nextInt() & 15) + 8;
            this.sandGen.generate(world, selector, rand, selector.getTopSolidOrLiquidBlock(cRel.delta(rX, 0, rZ)));
        }

        for (i = 0; i < this.clayPerChunk; ++i) {
            rX = (rand.nextInt() & 15) + 8;
            rZ = (rand.nextInt() & 15) + 8;
            this.clayGen.generate(world, selector, rand, selector.getTopSolidOrLiquidBlock(cRel.delta(rX, 0, rZ)));
        }

        for (i = 0; i < this.gravelPatchesPerChunk; ++i) {
            rX = (rand.nextInt() & 15) + 8;
            rZ = (rand.nextInt() & 15) + 8;
            this.gravelGen.generate(world, selector, rand, selector.getTopSolidOrLiquidBlock(cRel.delta(rX, 0, rZ)));
        }

        int k1 = this.treesPerChunk;

        if (rand.nextFloat() < this.extraTreeChance) {
            ++k1;
        }

        for (i = 0; i < k1; ++i) {
            rX = (rand.nextInt() & 15) + 8;
            rZ = (rand.nextInt() & 15) + 8;
            WorldGenAbstractTree threeGen = biome.getRandomTreeFeature(rand);
            threeGen.setDecorationDefaults();
            BlockPos blockpos = selector.getHeightMut(cRel.delta(rX, 0, rZ));

            if (threeGen.generate(world, selector, rand, blockpos)) {
                threeGen.generateSaplings(world, rand, blockpos);
            }
        }

        for (i = 0; i < this.bigMushroomsPerChunk; ++i) {
            rX = (rand.nextInt() & 15) + 8;
            rZ = (rand.nextInt() & 15) + 8;
            this.bigMushroomGen.generate(world, selector, rand, selector.getHeightMut(cRel.delta(rX, 0, rZ)));
        }

        for (i = 0; i < this.flowersPerChunk; ++i) {
            rX = (rand.nextInt() & 15) + 8;
            rZ = (rand.nextInt() & 15) + 8;
            rY = selector.getHeightY(cRel.delta(rX, 0, rZ)) + 32;

            if (rY > 0) {
                int k17 = rand.nextInt(rY);
                BlockPos blockpos1 = cRel.delta(rX, k17, rZ);
                BlockFlower.EnumFlowerType flowerType = biome.pickRandomFlower(rand, blockpos1);
                BlockFlower blockflower = flowerType.getBlockType().getBlock();

                //todo fix async
                if (blockflower.getDefaultState().getMaterial() != Material.AIR) {
                    this.flowerGen.setGeneratedBlock(blockflower, flowerType);
                    this.flowerGen.generate(world, selector, rand, blockpos1);
                }
            }
        }

        for (i = 0; i < this.grassPerChunk; ++i) {
            rX = (rand.nextInt() & 15) + 8;
            rZ = (rand.nextInt() & 15) + 8;
            rY = selector.getHeightY(cRel.delta(rX, 0, rZ)) * 2;

            if (rY > 0) {
                int l17 = rand.nextInt(rY);
                biome.getRandomWorldGenForGrass(rand).generate(world, selector, rand, cRel.delta(rX, l17, rZ));
            }
        }

        for (i = 0; i < this.deadBushPerChunk; ++i) {
            rX = (rand.nextInt() & 15) + 8;
            rZ = (rand.nextInt() & 15) + 8;
            rY = selector.getHeightY(cRel.delta(rX, 0, rZ)) * 2;

            if (rY > 0) {
                int i18 = rand.nextInt(rY);
                gen.generate(world, selector, rand, cRel.delta(rX, i18, rZ));
            }
        }

        for (i = 0; i < this.waterlilyPerChunk; ++i) {
            rX = (rand.nextInt() & 15) + 8;
            rZ = (rand.nextInt() & 15) + 8;
            rY = selector.getHeightY(cRel.delta(rX, 0, rZ)) * 2;

            if (rY > 0) {
                int j18 = rand.nextInt(rY);
                BlockPos blockpos4, blockpos7;

                for (blockpos4 = cRel.delta(rX, j18, rZ);blockpos4.getY() > 0;blockpos4 = blockpos7) {
                    blockpos7 = blockpos4.down();
                    if (!selector.isAirBlock(blockpos7)) {
                        break;
                    }
                }
                this.waterlilyGen.generate(world, selector, rand, blockpos4);
            }
        }

        for (i = 0; i < this.mushroomsPerChunk; ++i) {
            if (rand.nextInt(4) == 0) {
                rX = (rand.nextInt() & 15) + 8;
                rZ = (rand.nextInt() & 15) + 8;

                BlockPos blockpos2 = selector.getHeightMut(cRel.delta(rX, 0, rZ));

                this.mushroomBrownGen.generate(world, selector, rand, blockpos2);
            }

            if (rand.nextInt(8) == 0) {
                rX = (rand.nextInt() & 15) + 8;
                rZ = (rand.nextInt() & 15) + 8;
                rY = selector.getHeightY(cRel.delta(rX, 0, rZ)) * 2;

                if (rY > 0) {
                    this.mushroomRedGen.generate(world, selector, rand, cRel.delta(rX, rand.nextInt(rY), rZ));
                }
            }
        }

        if (rand.nextInt(4) == 0) {
            rX = (rand.nextInt() & 15) + 8;
            rZ = (rand.nextInt() & 15) + 8;
            rY = selector.getHeightY(cRel.delta(rX, 0, rZ)) * 2;

            if (rY > 0) {
                this.mushroomBrownGen.generate(world, selector, rand, cRel.delta(rX, rand.nextInt(rY), rZ));
            }
        }

        if (rand.nextInt(8) == 0) {
            rX = (rand.nextInt() & 15) + 8;
            rZ = (rand.nextInt() & 15) + 8;
            rY = selector.getHeightY(cRel.delta(rX, 0, rZ)) * 2;

            if (rY > 0) {
                this.mushroomRedGen.generate(world, selector, rand, cRel.delta(rX, rand.nextInt(rY), rZ));
            }
        }

        for (i = -10; i < this.reedsPerChunk; ++i) {
            rX = (rand.nextInt() & 15) + 8;
            rZ = (rand.nextInt() & 15) + 8;
            rY = selector.getHeightY(cRel.delta(rX, 0, rZ)) * 2;

            if (rY > 0) {
                this.reedGen.generate(world, selector, rand, cRel.delta(rX, rand.nextInt(rY), rZ));
            }
        }

        if (rand.nextInt(32) == 0) {
            rX = (rand.nextInt() & 15) + 8;
            rZ = (rand.nextInt() & 15) + 8;
            rY = selector.getHeightY(cRel.delta(rX, 0, rZ)) * 2;

            if (rY > 0) {
                (new WorldGenPumpkin()).generate(world, selector, rand, cRel.delta(rX, rand.nextInt(rY), rZ));
            }
        }

        for (i = 0; i < this.cactiPerChunk; ++i) {
            rX = (rand.nextInt() & 15) + 8;
            rZ = (rand.nextInt() & 15) + 8;
            rY = selector.getHeightY(cRel.delta(rX, 0, rZ)) * 2;

            if (rY > 0) {
                this.cactusGen.generate(world, selector, rand, cRel.delta(rX, rand.nextInt(rY), rZ));
            }
        }

        if (this.generateFalls) {
            gen = new WorldGenLiquids(Blocks.FLOWING_WATER);
            for (i = 0; i < 50; ++i) {
                rX = (rand.nextInt() & 15) + 8;
                rZ = (rand.nextInt() & 15) + 8;
                rY = rand.nextInt(248) + 8;

                gen.generate(world, selector, rand, cRel.delta(rX, rand.nextInt(rY), rZ));
            }
            gen = new WorldGenLiquids(Blocks.FLOWING_LAVA);

            for (i = 0; i < 20; ++i) {
                rX = (rand.nextInt() & 15) + 8;
                rZ = (rand.nextInt() & 15) + 8;
                rY = rand.nextInt(rand.nextInt(rand.nextInt(240) + 8) + 8);

                gen.generate(world, selector, rand, cRel.delta(rX, rY, rZ));
            }
        }
        MinecraftForge.EVENT_BUS.postF(new DecorateBiomeEvent.Post(world, rand, fPos));
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
        if(max == 0)return;
        ChunkSelector selector = new ChunkSelector(world);

        for(j = 0; j < max; ++j) {
            BlockPos blockpos = this.chunkPos.add(rand.nextInt(16), rand.nextInt(maxY - minY) + minY, rand.nextInt(16));
            gen.generate(world, selector, rand, blockpos);
        }
    }


    @Deprecated
    protected void genStandardOre2(World world, Random rand, int max, WorldGenerator gen, int yf1, int yf2) {
        if(max == 0)return;
        ChunkSelector selector = new ChunkSelector(world);
        for(int i = 0; i < max; ++i) {
            BlockPos blockpos = this.chunkPos.add(rand.nextInt(16), rand.nextInt(yf2) + rand.nextInt(yf2) + yf1 - yf2, rand.nextInt(16));
            gen.generate(world, selector, rand, blockpos);
        }
    }


    protected final void generateOres(final ChunkSelector selector, final World worldIn, final Random random, final BlockPos chunkPos) {
        MinecraftForge.ORE_GEN_BUS.postF(new OreGenEvent.Pre(worldIn, random, chunkPos));

        ChunkGenSettingsPlus settings = ((WorldServer)worldIn).getChunkGenSettings();

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

        MinecraftForge.ORE_GEN_BUS.postF(new OreGenEvent.Post(worldIn, random, chunkPos));
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