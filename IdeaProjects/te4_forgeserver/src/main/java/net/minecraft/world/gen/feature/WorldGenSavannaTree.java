package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import plus.fserver.server.elca.ChunkSelector;


public class WorldGenSavannaTree extends WorldGenAbstractTree {
    private final IBlockState wood;
    private final IBlockState leaves;
    private final int height;
    private final int randHeight;

    public WorldGenSavannaTree(boolean notify) {
        super(notify);
        wood   = Blocks.LOG2.getDefaultState().withProperty(BlockNewLog.VARIANT, BlockPlanks.EnumType.ACACIA);
        leaves = Blocks.LEAVES2.getDefaultState().withProperty(BlockNewLeaf.VARIANT, BlockPlanks.EnumType.ACACIA).withProperty(BlockLeaves.CHECK_DECAY, Boolean.FALSE);
        height = 5;
        randHeight = 3;
    }


    public WorldGenSavannaTree(boolean notify, IBlockState wood, IBlockState leaves, int height, int randHeight){
        super(notify);
        this.wood = wood;
        this.leaves = leaves;
        this.height = height;
        this.randHeight = randHeight;
    }


    public boolean generate(World worldIn, Random rand, BlockPos position) {
        int i = rand.nextInt(randHeight) + rand.nextInt(randHeight) + height;
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
                        if (j >= 0 && j < 256) {
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
                boolean isSoil = state.getBlock().canSustainPlant(state, worldIn, down, EnumFacing.UP, ((BlockSapling)Blocks.SAPLING));

                if (isSoil && position.getY() < worldIn.getHeight() - i - 1) {
                    state.getBlock().onPlantGrow(state, worldIn, down, position);
                    EnumFacing enumfacing = EnumFacing.Plane.HORIZONTAL.random(rand);
                    int k2 = i - rand.nextInt(4) - 1;
                    int l2 = 3 - rand.nextInt(3);
                    int i3 = position.getX();
                    int j1 = position.getZ();
                    int k1 = 0;

                    for (int l1 = 0; l1 < i; ++l1) {
                        int i2 = position.getY() + l1;

                        if (l1 >= k2 && l2 > 0) {
                            i3 += enumfacing.getXOffset();
                            j1 += enumfacing.getZOffset();
                            --l2;
                        }

                        BlockPos blockpos = new BlockPos(i3, i2, j1);
                        state = selector.getState(blockpos);

                        if (state.getBlock().isAir(state, worldIn, blockpos) || state.getBlock().isLeaves(state, worldIn, blockpos)) {
                            this.setBlockAndNotifyAdequately(selector, blockpos, wood);

                            k1 = i2;
                        }
                    }

                    BlockPos blockpos2 = new BlockPos(i3, k1, j1);

                    for (int j3 = -3; j3 <= 3; ++j3) {
                        for (int i4 = -3; i4 <= 3; ++i4) {
                            if (Math.abs(j3) != 3 || Math.abs(i4) != 3) {
                                this.placeLeafAt(selector, worldIn, blockpos2.add(j3, 0, i4));
                            }
                        }
                    }

                    blockpos2 = blockpos2.up();

                    for (int k3 = -1; k3 <= 1; ++k3) {
                        for (int j4 = -1; j4 <= 1; ++j4) {
                            this.placeLeafAt(selector, worldIn, blockpos2.add(k3, 0, j4));
                        }
                    }
                    this.placeLeafAt(selector, worldIn, blockpos2.east(2));
                    this.placeLeafAt(selector, worldIn, blockpos2.west(2));
                    this.placeLeafAt(selector, worldIn, blockpos2.south(2));
                    this.placeLeafAt(selector, worldIn, blockpos2.north(2));
                    i3 = position.getX();
                    j1 = position.getZ();
                    EnumFacing enumfacing1 = EnumFacing.Plane.HORIZONTAL.random(rand);

                    if (enumfacing1 != enumfacing) {
                        int l3 = k2 - rand.nextInt(2) - 1;
                        int k4 = 1 + rand.nextInt(3);
                        k1 = 0;

                        for (int l4 = l3; l4 < i && k4 > 0; --k4) {
                            if (l4 >= 1) {
                                int j2 = position.getY() + l4;
                                i3 += enumfacing1.getXOffset();
                                j1 += enumfacing1.getZOffset();
                                BlockPos blockpos1 = new BlockPos(i3, j2, j1);
                                state = selector.getState(blockpos1);

                                if (state.getBlock().isAir(state, worldIn, blockpos1) || state.getBlock().isLeaves(state, worldIn, blockpos1)) {
                                    this.setBlockAndNotifyAdequately(selector, blockpos1, wood);
                                    k1 = j2;
                                }
                            }
                            ++l4;
                        }
                        if (k1 > 0) {
                            BlockPos blockpos3 = new BlockPos(i3, k1, j1);

                            for (int i5 = -2; i5 <= 2; ++i5) {
                                for (int k5 = -2; k5 <= 2; ++k5) {
                                    if (Math.abs(i5) != 2 || Math.abs(k5) != 2) {
                                        this.placeLeafAt(selector, worldIn, blockpos3.add(i5, 0, k5));
                                    }
                                }
                            }
                            blockpos3 = blockpos3.up();
                            for (int j5 = -1; j5 <= 1; ++j5) {
                                for (int l5 = -1; l5 <= 1; ++l5) {
                                    this.placeLeafAt(selector, worldIn, blockpos3.add(j5, 0, l5));
                                }
                            }
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


    private void placeLeafAt(ChunkSelector selector, World worldIn, BlockPos pos) {
        IBlockState state = selector.getState(pos);
        if (state.getBlock().isAir(state, worldIn, pos) || state.getBlock().isLeaves(state, worldIn, pos)) {
            this.setBlockAndNotifyAdequately(selector, pos, leaves);
        }
    }
}