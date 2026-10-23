package net.minecraft.world.gen.feature;

import java.util.Random;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import plus.fserver.api.Keep;
import plus.fserver.server.ElcaThread;
import plus.fserver.server.elca.ChunkSelector;


public class WorldGenMegaPineTree extends WorldGenHugeTrees {
    private static final IBlockState TRUNK = Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.SPRUCE);
    private static final IBlockState LEAF = Blocks.LEAVES.getDefaultState().withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.SPRUCE).withProperty(BlockLeaves.CHECK_DECAY, Boolean.valueOf(false));
    private static final IBlockState PODZOL = Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.PODZOL);
    private final boolean useBaseHeight;

    @Keep
    public WorldGenMegaPineTree(boolean notify, boolean useBaseHeight) {
        super(notify, 13, 15, TRUNK, LEAF);
        this.useBaseHeight = useBaseHeight;
    }


    @Override
    public boolean generate(World world, Random rand, BlockPos pos) {
        return generate(world, ElcaThread.cache(world), rand, pos);
    }


    @Override
    public boolean generate(World world, ChunkSelector selector, Random rand, BlockPos pos) {
        int i = this.getHeight(rand);
        if (!this.ensureGrowable(selector, world, pos, i)) {
            return false;
        } else {
            IBlockState wood = this.woodMetadata;
            boolean notify = needNotify();
            this.createCrown(selector, pos.getX(), pos.getZ(), pos.getY() + i, 0, rand);

            for (int dy = 0; dy < i; ++dy) {
                BlockPos upped;
                if (isAirLeaves(selector, world, upped = pos.up(dy))) {
                    setBlockAndNotifyAdequately(selector, upped, wood, notify);
                }
                if (dy < i - 1) {
                    if (isAirLeaves(selector, world, pos.add(1, dy, 0))) {
                        setBlockAndNotifyAdequately(selector, pos.add(1, dy, 0), wood, notify);
                    }

                    if (isAirLeaves(selector, world, pos.add(1, dy, 1))) {
                        setBlockAndNotifyAdequately(selector, pos.add(1, dy, 1), wood, notify);
                    }

                    if (isAirLeaves(selector, world, pos.add(0, dy, 1))) {
                        setBlockAndNotifyAdequately(selector, pos.add(0, dy, 1), wood, notify);
                    }
                }
            }
            return true;
        }
    }


    private void createCrown(ChunkSelector selector, int x, int z, int y, int factor, Random rand) {
        int i = rand.nextInt(5) + (this.useBaseHeight ? this.baseHeight : 3);
        int j = 0;
        for (int k = y - i; k <= y; ++k) {
            int l = y - k;
            int i1 = factor + MathHelper.floor((float)l / (float)i * 3.5F);
            this.growLeavesLayerStrict(selector, new BlockPos(x, k, z), i1 + (l > 0 && i1 == j && (k & 1) == 0 ? 1 : 0));
            j = i1;
        }
    }


    @Override
    public void generateSaplings(World world, Random random, BlockPos pos) {
        ChunkSelector selector = ElcaThread.cache(world);
        boolean notify = needNotify();

        placePodzolCircle(selector, world, pos.west().north(), notify);
        placePodzolCircle(selector, world, pos.east(2).north(), notify);
        placePodzolCircle(selector, world, pos.west().south(2), notify);
        placePodzolCircle(selector, world, pos.east(2).south(2), notify);

        for (int i = 0; i < 5; ++i) {
            int j = random.nextInt(64);
            int k = j % 8;
            int l = j / 8;
            if (k == 0 || k == 7 || l == 0 || l == 7) {
                placePodzolCircle(selector, world, pos.add(-3 + k, 0, -3 + l), notify);
            }
        }
    }


    private static void placePodzolCircle(ChunkSelector selector, World worldIn, BlockPos center, boolean notify) {
        for (int i = -2; i <= 2; ++i) {
            for (int j = -2; j <= 2; ++j) {
                if (Math.abs(i) != 2 || Math.abs(j) != 2) {
                    placePodzolAt(selector, worldIn, center.add(i, 0, j), notify);
                }
            }
        }
    }


    private static void placePodzolAt(ChunkSelector selector, World worldIn, BlockPos pos, boolean notify) {
        for (int i = 2; i >= -3; --i) {
            BlockPos blockpos = pos.up(i);
            IBlockState iblockstate = selector.getState(blockpos);
            Block block = iblockstate.getBlock();

            if (block.canSustainPlant(iblockstate, worldIn, blockpos, EnumFacing.UP, ((BlockSapling)Blocks.SAPLING))) {
                setBlockAndNotifyAdequately(selector, blockpos, PODZOL, notify);
                break;
            }
            if (iblockstate.getMaterial() != Material.AIR && i < 0) {
                break;
            }
        }
    }


    private boolean isAirLeaves(ChunkSelector selector, World world, BlockPos pos) {
        IBlockState state = selector.getState(pos);
        return state.getBlock().isAir(state, world, pos) || state.getBlock().isLeaves(state, world, pos);
    }
}