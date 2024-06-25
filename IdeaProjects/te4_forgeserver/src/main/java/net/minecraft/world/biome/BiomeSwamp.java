package net.minecraft.world.biome;

import java.util.Random;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.BlockFlower.EnumFlowerType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;
import net.minecraft.world.gen.feature.WorldGenFossils;
import net.minecraftforge.event.terraingen.TerrainGen;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent.Decorate.EventType;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


public class BiomeSwamp extends Biome {
    private static final WorldGenFossils GEN_FOSSILS = new WorldGenFossils();
    protected static final IBlockState WATER_LILY;

    protected BiomeSwamp(Biome.BiomeProperties properties) {
        super(properties);
        this.decorator.treesPerChunk = 2;
        this.decorator.flowersPerChunk = 1;
        this.decorator.deadBushPerChunk = 1;
        this.decorator.mushroomsPerChunk = 8;
        this.decorator.reedsPerChunk = 10;
        this.decorator.clayPerChunk = 1;
        this.decorator.waterlilyPerChunk = 4;
        this.decorator.sandPatchesPerChunk = 0;
        this.decorator.gravelPatchesPerChunk = 0;
        this.decorator.grassPerChunk = 5;
        this.spawnableMonsterList.add(new Biome.SpawnListEntry(EntitySlime.class, 1, 1, 1));
    }


    @Override
    public WorldGenAbstractTree getRandomTreeFeature(Random p_150567_1_) {
        return SWAMP_FEATURE;
    }


    @Override
    public BlockFlower.EnumFlowerType pickRandomFlower(Random p_180623_1_, BlockPos p_180623_2_) {
        return EnumFlowerType.BLUE_ORCHID;
    }


    @Override
    public void genTerrainBlocks(World world, Random rand, ChunkPrimer primer, int bX, int bZ, double seed) {
        double d0 = GRASS_COLOR_NOISE.getValue((double) bX * 0.25, (double)bZ * 0.25);
        if (d0 > 0.0) {
            int x = bX & 15;
            int z = bZ & 15;

            for(int y = 255; y >= 0; --y) {
                if (primer.getBlockState(z, y, x).getMaterial() != Material.AIR) {
                    if (y == 62 && primer.getBlockState(z, y, x).getBlock() != Blocks.WATER) {
                        primer.setBlockState(z, y, x, WATER);
                        if (d0 < 0.12) {
                            primer.setBlockState(z, y + 1, x, WATER_LILY);
                        }
                    }
                    break;
                }
            }
        }

        this.generateBiomeTerrain(world, rand, primer, bX, bZ, seed);
    }


    @Override
    public void decorate(World world, Random rand, BlockPos pos) {
        super.decorate(world, rand, pos);
        if (TerrainGen.decorate(world, rand, new ChunkPos(pos), EventType.FOSSIL) && rand.nextInt(64) == 0) {
            GEN_FOSSILS.generate(world, rand, pos);
        }
    }


    @SideOnly(Side.CLIENT)
    public int getGrassColorAtPos(BlockPos p_180627_1_) {
        double d0 = GRASS_COLOR_NOISE.getValue((double)p_180627_1_.getX() * 0.0225, (double)p_180627_1_.getZ() * 0.0225);
        return this.getModdedBiomeGrassColor(d0 < -0.1 ? 5011004 : 6975545);
    }


    @Override
    @SideOnly(Side.CLIENT)
    public int getFoliageColorAtPos(BlockPos pos) {
        return this.getModdedBiomeFoliageColor(6975545);
    }


    @Override
    public void addDefaultFlowers() {
        this.addFlower(Blocks.RED_FLOWER.getDefaultState().withProperty(Blocks.RED_FLOWER.getTypeProperty(), EnumFlowerType.BLUE_ORCHID), 10);
    }


    static {
        WATER_LILY = Blocks.WATERLILY.getDefaultState();
    }
}