package net.minecraft.world.biome;

import net.minecraft.init.*;
import net.minecraft.block.*;
import net.minecraft.block.properties.*;
import net.minecraft.entity.passive.*;
import java.util.*;
import net.minecraft.world.gen.feature.*;
import net.minecraft.util.math.*;
import net.minecraftforge.event.terraingen.*;
import net.minecraft.world.chunk.*;
import net.minecraftforge.common.*;
import net.minecraft.block.state.pattern.*;
import net.minecraft.world.*;
import net.minecraft.block.state.*;
import plus.fserver.server.ElcaThread;
import plus.fserver.server.elca.ChunkSelector;


public class BiomeHills extends Biome {
    private final WorldGenerator silverfishSpawner;
    private final WorldGenTaiga2 spruceGenerator;
    private final Type type;

    public BiomeHills(final Type p_i46710_1_, final Biome.BiomeProperties properties) {
        super(properties);
        this.silverfishSpawner = new WorldGenMinable(Blocks.MONSTER_EGG.getDefaultState().withProperty((IProperty)BlockSilverfish.VARIANT, (Comparable)BlockSilverfish.EnumType.STONE), 9);
        this.spruceGenerator = new WorldGenTaiga2(false);
        if (p_i46710_1_ == Type.EXTRA_TREES) {
            this.decorator.treesPerChunk = 3;
        }
        this.spawnableCreatureList.add(new Biome.SpawnListEntry((Class)EntityLlama.class, 5, 4, 6));
        this.type = p_i46710_1_;
    }


    @Override
    public WorldGenAbstractTree getRandomTreeFeature(final Random rand) {
        return (rand.nextInt(3) > 0) ? this.spruceGenerator : super.getRandomTreeFeature(rand);
    }


    @Override
    public void decorate(final World worldIn, final Random rand, final BlockPos pos) {
        super.decorate(worldIn, rand, pos);
        MinecraftForge.ORE_GEN_BUS.post(new OreGenEvent.Pre(worldIn, rand, pos));
        final WorldGenerator emeralds = new EmeraldGenerator();
        ChunkSelector selector = ElcaThread.cache(worldIn);

        if (TerrainGen.generateOre(worldIn, rand, emeralds, pos, OreGenEvent.GenerateMinable.EventType.EMERALD)) {
            emeralds.generate(worldIn, selector, rand, pos);
        }
        for (int j1 = 0; j1 < 7; ++j1) {
            final int k1 = rand.nextInt(16);
            final int l1 = rand.nextInt(64);
            final int i2 = rand.nextInt(16);
            if (TerrainGen.generateOre(worldIn, rand, this.silverfishSpawner, pos.add(j1, k1, l1), OreGenEvent.GenerateMinable.EventType.SILVERFISH)) {
                this.silverfishSpawner.generate(worldIn, selector, rand, pos.add(k1, l1, i2));
            }
        }
        MinecraftForge.ORE_GEN_BUS.post(new OreGenEvent.Post(worldIn, rand, pos));
    }


    @Override
    public void genTerrainBlocks(final World worldIn, final Random rand, final ChunkPrimer chunkPrimerIn, final int x, final int z, final double noiseVal) {
        this.topBlock = Blocks.GRASS.getDefaultState();
        this.fillerBlock = Blocks.DIRT.getDefaultState();
        if ((noiseVal < -1.0 || noiseVal > 2.0) && this.type == Type.MUTATED) {
            this.topBlock = Blocks.GRAVEL.getDefaultState();
            this.fillerBlock = Blocks.GRAVEL.getDefaultState();
        }
        else if (noiseVal > 1.0 && this.type != Type.EXTRA_TREES) {
            this.topBlock = Blocks.STONE.getDefaultState();
            this.fillerBlock = Blocks.STONE.getDefaultState();
        }
        this.generateBiomeTerrain(worldIn, rand, chunkPrimerIn, x, z, noiseVal);
    }


    public enum Type {
        NORMAL,
        EXTRA_TREES,
        MUTATED;
    }


    private static class EmeraldGenerator extends CWorldGenerator {
        @Override
        public boolean generate(final World worldIn, ChunkSelector selector, final Random rand, final BlockPos position) {
            for (int count = 3 + rand.nextInt(6), i = 0; i < count; ++i) {
                final int offset = ForgeModContainer.fixVanillaCascading ? 8 : 0;
                final BlockPos blockpos = position.add(rand.nextInt(16) + offset, rand.nextInt(28) + 4, rand.nextInt(16) + offset);
                final IBlockState state = selector.getBlockState(blockpos);
                if (state.getBlock().isReplaceableOreGen(state, selector, blockpos, BlockMatcher.forBlock(Blocks.STONE))) {
                    selector.setBlockState(blockpos, Blocks.EMERALD_ORE.getDefaultState(), 18);
                }
            }
            return true;
        }
    }
}