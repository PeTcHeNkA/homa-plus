package net.minecraft.world.gen.feature;

import java.util.Random;

import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import plus.fserver.server.elca.ChunkSelector;


public class WorldGenTaiga1 extends WorldGenAbstractTree {
    private static final IBlockState TRUNK = Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.SPRUCE);
    private static final IBlockState LEAF = Blocks.LEAVES.getDefaultState().withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.SPRUCE).withProperty(BlockLeaves.CHECK_DECAY, Boolean.valueOf(false));

    public WorldGenTaiga1() {
        super(false);
    }


    public boolean generate(World worldIn, Random rand, BlockPos position) {
        int num1 = rand.nextInt(5) + 7;
        int num2 = num1 - rand.nextInt(2) - 3;
        int num3 = 1 + rand.nextInt(num1 - num2 + 1);

        if (position.getY() >= 1 && position.getY() + num1 + 1 <= 256) {
            boolean flag = true;

            BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();
            ChunkSelector selector = new ChunkSelector(worldIn);

            for (int i1 = position.getY(), j1; i1 <= position.getY() + 1 + num1 && flag; ++i1) {

                if (i1 - position.getY() < num2) {
                    j1 = 0;
                } else {
                    j1 = num3;
                }

                for (int k1 = position.getX() - j1; k1 <= position.getX() + j1 && flag; ++k1) {
                    for (int l1 = position.getZ() - j1; l1 <= position.getZ() + j1 && flag; ++l1) {
                        if (i1 >= 0 && i1 < 256) {
                            if (!this.isReplaceable(selector, worldIn, blockPos.setPos(k1, i1, l1))) {
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
                IBlockState state = selector.getState(temp);
                boolean isSoil = state.getBlock().canSustainPlant(state, worldIn, temp, EnumFacing.UP, (BlockSapling)Blocks.SAPLING);

                if (isSoil && position.getY() < 256 - num1 - 1) {
                    state.getBlock().onPlantGrow(state, worldIn, temp, position);
                    int k2 = 0;

                    for (int posY = position.getY() + num1; posY >= position.getY() + num2; --posY) {
                        for (int posX = position.getX() - k2; posX <= position.getX() + k2; ++posX) {
                            int k3 = posX - position.getX();

                            for (int posZ = position.getZ() - k2; posZ <= position.getZ() + k2; ++posZ) {
                                int j2 = posZ - position.getZ();

                                if (Math.abs(k3) != k2 || Math.abs(j2) != k2 || k2 <= 0) {
                                    BlockPos blockpos = new BlockPos(posX, posY, posZ);
                                    state = selector.getState(blockpos);

                                    if (state.getBlock().canBeReplacedByLeaves(state, worldIn, blockpos)) {
                                        this.setBlockAndNotifyAdequately(selector, blockpos, LEAF);
                                    }
                                }
                            }
                        }

                        if (k2 >= 1 && posY == position.getY() + num2 + 1) {
                            --k2;
                        } else if (k2 < num3) {
                            ++k2;
                        }
                    }

                    for (int i3 = 0; i3 < num1 - 1; ++i3) {
                        temp = position.up(i3);
                        if(temp.getY()>255 || temp.getY()<1)continue;
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