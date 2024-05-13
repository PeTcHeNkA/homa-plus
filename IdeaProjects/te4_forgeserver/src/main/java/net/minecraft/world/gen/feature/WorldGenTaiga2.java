package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockOldLeaf;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import plus.fserver.api.Keep;
import plus.fserver.server.elca.ChunkSelector;


public class WorldGenTaiga2 extends WorldGenAbstractTree {
    private static final IBlockState TRUNK = Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.SPRUCE);
    private static final IBlockState LEAF = Blocks.LEAVES.getDefaultState().withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.SPRUCE).withProperty(BlockLeaves.CHECK_DECAY, Boolean.valueOf(false));

    @Keep
    public WorldGenTaiga2(boolean flag) {
        super(flag);
    }


    @Override
    public boolean generate(World worldIn, Random rand, BlockPos position) {
        int num = rand.nextInt();
        int i = num & 3 + 6;
        int j = 1 + (num >> 2 & 1);
        int k = i - j;
        int l = 2 + (num >> 4 & 1);

        if (position.getY() >= 1 && position.getY() + i + 1 <= worldIn.getHeight()) {
            boolean flag = true;
            BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();
            ChunkSelector selector = new ChunkSelector(worldIn);

            for (int i1 = position.getY(); i1 <= position.getY() + 1 + i && flag; ++i1) {
                if (i1 - position.getY() < j) {
                    num = 0;
                } else {
                    num = l;
                }

                for (int k1 = position.getX() - num; k1 <= position.getX() + num && flag; ++k1) {
                    for (int l1 = position.getZ() - num; l1 <= position.getZ() + num && flag; ++l1) {
                        if (i1 >= 0 && i1 < worldIn.getHeight()) {
                            IBlockState state = selector.getState(blockPos.setPos(k1, i1, l1));

                            if (!state.getBlock().isAir(state, worldIn, blockPos.setPos(k1, i1, l1)) && !state.getBlock().isLeaves(state, worldIn, blockPos.setPos(k1, i1, l1))) {
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
                BlockPos temp = position.down();
                IBlockState state = worldIn.getBlockState(temp);

                if (state.getBlock().canSustainPlant(state, worldIn, temp, net.minecraft.util.EnumFacing.UP, (net.minecraft.block.BlockSapling)Blocks.SAPLING) && position.getY() < worldIn.getHeight() - i - 1) {
                    state.getBlock().onPlantGrow(state, worldIn, temp, position);
                    num = rand.nextInt(2);
                    int j3 = 1, k3 = 0;

                    for (int l3 = 0; l3 <= k; ++l3) {
                        int j4 = position.getY() + i - l3;

                        for (int i2 = position.getX() - num; i2 <= position.getX() + num; ++i2) {
                            int j2 = i2 - position.getX();

                            for (int k2 = position.getZ() - num; k2 <= position.getZ() + num; ++k2) {
                                int l2 = k2 - position.getZ();

                                if (Math.abs(j2) != num || Math.abs(l2) != num || num <= 0) {
                                    state = selector.getState(temp = new BlockPos(i2, j4, k2));

                                    if (state.getBlock().canBeReplacedByLeaves(state, worldIn, temp)) {
                                        this.setBlockAndNotifyAdequately(selector, temp, LEAF);
                                    }
                                }
                            }
                        }
                        if (num >= j3) {
                            num = k3;
                            k3 = 1;
                            ++j3;
                            if (j3 > l) {
                                j3 = l;
                            }
                        } else {
                            ++num;
                        }
                    }

                    num = rand.nextInt(3);

                    for (int k4 = 0; k4 < i - num; ++k4) {
                        temp = position.up(k4);
                        state = selector.getState(temp);

                        if (state.getBlock().isAir(state, worldIn, temp) || state.getBlock().isLeaves(state, worldIn, temp)) {
                            this.setBlockAndNotifyAdequately(selector, temp, TRUNK);
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