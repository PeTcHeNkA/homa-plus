package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.BlockOldLeaf;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import plus.fserver.api.Keep;
import plus.fserver.server.elca.ChunkSelector;


public class WorldGenBirchTree extends WorldGenAbstractTree {
    private static final IBlockState LOG = Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.BIRCH);
    private static final IBlockState LEAF = Blocks.LEAVES.getDefaultState().withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.BIRCH).withProperty(BlockOldLeaf.CHECK_DECAY, Boolean.valueOf(false));
    private final boolean useExtraRandomHeight;

    @Keep
    public WorldGenBirchTree(boolean notify, boolean useExtraRandomHeightIn) {
        super(notify);
        this.useExtraRandomHeight = useExtraRandomHeightIn;
    }


    public boolean generate(World worldIn, Random rand, BlockPos position) {
        int i = rand.nextInt(3) + 5;

        if (this.useExtraRandomHeight) {
            i += rand.nextInt(7);
        }

        boolean flag = true;

        if (position.getY() >= 1 && position.getY() + i + 1 <= 256) {
            BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();
            ChunkSelector selector = new ChunkSelector(worldIn);
            for (int j = position.getY(); j <= position.getY() + 1 + i; ++j) {
                int k = 1;

                if (j == position.getY()) {
                    k = 0;
                }

                if (j >= position.getY() + 1 + i - 2) {
                    k = 2;
                }

                for (int l = position.getX() - k; l <= position.getX() + k && flag; ++l) {
                    for (int i1 = position.getZ() - k; i1 <= position.getZ() + k && flag; ++i1) {
                        if (j >= 0 && j < worldIn.getHeight()) {
                            if (!this.isReplaceable(selector, worldIn, blockPos.setPos(l, j, i1))) {
                                flag = false;
                            }
                        } else {
                            flag = false;
                        }
                    }
                }
            }
            if (!flag) {
                return false;
            } else {
                BlockPos down = position.down();
                IBlockState state = selector.getState(down);
                boolean isSoil = state.getBlock().canSustainPlant(state, worldIn, down, net.minecraft.util.EnumFacing.UP, (net.minecraft.block.BlockSapling)Blocks.SAPLING);

                if (isSoil && position.getY() < worldIn.getHeight() - i - 1) {
                    state.getBlock().onPlantGrow(state, worldIn, down, position);

                    for (int i2 = position.getY() - 3 + i; i2 <= position.getY() + i; ++i2) {
                        int k2 = i2 - (position.getY() + i);
                        int l2 = 1 - k2 / 2;

                        for (int i3 = position.getX() - l2; i3 <= position.getX() + l2; ++i3) {
                            int j1 = i3 - position.getX();

                            for (int k1 = position.getZ() - l2; k1 <= position.getZ() + l2; ++k1) {
                                int l1 = k1 - position.getZ();

                                if (Math.abs(j1) != l2 || Math.abs(l1) != l2 || rand.nextInt(2) != 0 && k2 != 0) {
                                    BlockPos blockpos = new BlockPos(i3, i2, k1);
                                    IBlockState state2 = selector.getState(blockpos);

                                    if (state2.getBlock().isAir(state2, worldIn, blockpos) || state2.getBlock().isAir(state2, worldIn, blockpos)) {
                                        this.setBlockAndNotifyAdequately(selector, blockpos, LEAF);
                                    }
                                }
                            }
                        }
                    }


                    for (int j2 = 0; j2 < i; ++j2) {
                        BlockPos upN = position.up(j2);
                        IBlockState state2 = selector.getState(upN);

                        if (state2.getBlock().isAir(state2, worldIn, upN) || state2.getBlock().isLeaves(state2, worldIn, upN)) {
                            this.setBlockAndNotifyAdequately(selector, position.up(j2), LOG);
                        }
                    }
                    return true;
                } else {
                    return false;
                }
            }
        } else {
            return false;
        }
    }
}