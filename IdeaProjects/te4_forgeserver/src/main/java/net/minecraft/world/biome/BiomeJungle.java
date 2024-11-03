//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\ExtraApps\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package net.minecraft.world.biome;

import net.minecraft.block.state.*;
import net.minecraft.entity.passive.*;
import java.util.*;
import net.minecraft.world.*;
import net.minecraft.util.math.*;
import net.minecraftforge.event.terraingen.*;
import net.minecraft.world.gen.feature.*;
import net.minecraft.init.*;
import net.minecraft.block.*;
import plus.fserver.server.ElcaThread;
import plus.fserver.server.elca.ChunkSelector;


public class BiomeJungle extends Biome {
    private final boolean isEdge;
    private static final IBlockState JUNGLE_LOG;
    private static final IBlockState JUNGLE_LEAF;
    private static final IBlockState OAK_LEAF;

    public BiomeJungle(final boolean isEdgeIn, final Biome.BiomeProperties properties) {
        super(properties);
        this.isEdge = isEdgeIn;
        if (isEdgeIn) {
            this.decorator.treesPerChunk = 2;
        }
        else {
            this.decorator.treesPerChunk = 50;
        }
        this.decorator.grassPerChunk = 25;
        this.decorator.flowersPerChunk = 4;
        if (!isEdgeIn) {
            this.spawnableMonsterList.add(new Biome.SpawnListEntry(EntityOcelot.class, 2, 1, 1));
        }
        this.spawnableCreatureList.add(new Biome.SpawnListEntry(EntityParrot.class, 40, 1, 2));
        this.spawnableCreatureList.add(new Biome.SpawnListEntry(EntityChicken.class, 10, 4, 4));
    }


    @Override
    public WorldGenAbstractTree getRandomTreeFeature(final Random rand) {
        if (rand.nextInt(10) == 0) {
            return BiomeJungle.BIG_TREE_FEATURE;
        }
        if (rand.nextInt(2) == 0) {
            return new WorldGenShrub(BiomeJungle.JUNGLE_LOG, BiomeJungle.OAK_LEAF);
        }
        return (!this.isEdge && rand.nextInt(3) == 0) ? new WorldGenMegaJungle(false, 10, 20, BiomeJungle.JUNGLE_LOG, BiomeJungle.JUNGLE_LEAF) : new WorldGenTrees(false, 4 + rand.nextInt(7), BiomeJungle.JUNGLE_LOG, BiomeJungle.JUNGLE_LEAF, true);
    }


    @Override
    public WorldGenerator getRandomWorldGenForGrass(final Random rand) {
        return (rand.nextInt(4) == 0) ? new WorldGenTallGrass(BlockTallGrass.EnumType.FERN) : new WorldGenTallGrass(BlockTallGrass.EnumType.GRASS);
    }


    @Override
    public void decorate(final World worldIn, final Random rand, final BlockPos pos) {
        super.decorate(worldIn, rand, pos);
        final ChunkSelector selector = ElcaThread.cache(worldIn);

        final int i = rand.nextInt(16) + 8;
        final int j = rand.nextInt(16) + 8;
        int height = selector.getHeight(i, j) * 2;
        if (height < 1) {
            height = 1;
        }
        int k = rand.nextInt(height);
        if (TerrainGen.decorate(worldIn, rand, new ChunkPos(pos), pos.add(i, k, j), DecorateBiomeEvent.Decorate.EventType.PUMPKIN)) {
            new WorldGenMelon().generate(worldIn, selector, rand, pos.add(i, k, j));
        }

        if (TerrainGen.decorate(worldIn, rand, new ChunkPos(pos), DecorateBiomeEvent.Decorate.EventType.GRASS)) {
            final WorldGenVines genVines = new WorldGenVines();
            final BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();
            final int px = pos.getX() + 8, py = pos.getY() + 128, pz = pos.getZ() + 8;

            for (int j2 = 0; j2 < 50; ++j2) {
                k = rand.nextInt();

                genVines.generate(
                        worldIn, selector, rand,
                        mut.setPos((k & 15) + px, py, ((k >> 4) & 15) + pz)
                );
            }
        }
    }

    static {
        JUNGLE_LOG = Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.JUNGLE);
        JUNGLE_LEAF = Blocks.LEAVES.getDefaultState().withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.JUNGLE).withProperty(BlockLeaves.CHECK_DECAY, false);
        OAK_LEAF = Blocks.LEAVES.getDefaultState().withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.OAK).withProperty(BlockLeaves.CHECK_DECAY, false);
    }
}