package net.minecraft.world.gen.feature;

import java.util.Random;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockIDIndexes;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import plus.fserver.server.elca.ChunkSelector;


public class WorldGenTrees extends WorldGenAbstractTree {
    private static final IBlockState DEFAULT_TRUNK = Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.OAK);
    private static final IBlockState DEFAULT_LEAF = Blocks.LEAVES.getDefaultState().withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.OAK).withProperty(BlockLeaves.CHECK_DECAY, Boolean.valueOf(false));
    private final int minTreeHeight;
    private final boolean vinesGrow;
    private final IBlockState metaWood;
    private final IBlockState metaLeaves;

    public WorldGenTrees(boolean notify) {
        this(notify, 4, DEFAULT_TRUNK, DEFAULT_LEAF, false);
    }


    public WorldGenTrees(boolean notify, int minTreeHeightIn, IBlockState woodMeta, IBlockState metaLeaves, boolean growVines) {
        super(notify);
        this.minTreeHeight = minTreeHeightIn;
        this.metaWood = woodMeta;
        this.metaLeaves = metaLeaves;
        this.vinesGrow = growVines;
    }


    public boolean generate(World worldIn, Random rand, BlockPos position) {
        int factor = rand.nextInt(3) + this.minTreeHeight;

        if (position.getY() >= 1 && position.getY() + factor + 1 <= worldIn.getHeight()) {
            boolean flag = true;
            ChunkSelector selector = new ChunkSelector(worldIn);
            BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();

            for (int j = position.getY(); j <= position.getY() + 1 + factor; ++j) {
                int k = 1;
                if (j == position.getY())
                    k = 0;

                if (j >= position.getY() + 1 + factor - 2)
                    k = 2;

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
                BlockPos temp;
                IBlockState state = selector.getState(temp = position.down());

                if (state.getBlock().canSustainPlant(state, worldIn, temp, EnumFacing.UP, (BlockSapling)Blocks.SAPLING)
                        && position.getY() < worldIn.getHeight() - factor - 1) {
                    state.getBlock().onPlantGrow(state, worldIn, temp, position);

                    for (int i3 = position.getY() - 3 + factor; i3 <= position.getY() + factor; ++i3) {
                        int i4 = i3 - (position.getY() + factor);
                        int j1 = 1 - i4 / 2;

                        for (int k1 = position.getX() - j1; k1 <= position.getX() + j1; ++k1) {
                            int l1 = k1 - position.getX();

                            for (int i2 = position.getZ() - j1; i2 <= position.getZ() + j1; ++i2) {
                                int j2 = i2 - position.getZ();

                                if (Math.abs(l1) != j1 || Math.abs(j2) != j1 || rand.nextInt(2) != 0 && i4 != 0) {
                                    state = selector.getState(blockPos.setPos(k1, i3, i2));

                                    if (state.getBlock().isAir(state, worldIn, blockPos) || state.getBlock().isLeaves(state, worldIn, blockPos) || state.getMaterial() == Material.VINE) {
                                        this.setBlockAndNotifyAdequately(selector, blockPos, this.metaLeaves);
                                    }
                                }
                            }
                        }
                    }

                    for (int j3 = 0; j3 < factor; ++j3) {
                        state = selector.getState(temp = position.up(j3));

                        if (state.getBlock().isAir(state, worldIn, temp) || state.getBlock().isLeaves(state, worldIn, temp) || state.getMaterial() == Material.VINE) {
                            this.setBlockAndNotifyAdequately(selector, position.up(j3), this.metaWood);

                            if (this.vinesGrow && j3 > 0) {

                                if (rand.nextInt(3) > 0 && selector.isAirBlock(position.add(-1, j3, 0))) {
                                    this.addVine(selector, position.add(-1, j3, 0), BlockVine.EAST);
                                }

                                if (rand.nextInt(3) > 0 && selector.isAirBlock(position.add(1, j3, 0))) {
                                    this.addVine(selector, position.add(1, j3, 0), BlockVine.WEST);
                                }

                                if (rand.nextInt(3) > 0 && selector.isAirBlock(position.add(0, j3, -1))) {
                                    this.addVine(selector, position.add(0, j3, -1), BlockVine.SOUTH);
                                }

                                if (rand.nextInt(3) > 0 && selector.isAirBlock(position.add(0, j3, 1))) {
                                    this.addVine(selector, position.add(0, j3, 1), BlockVine.NORTH);
                                }
                            }
                        }
                    }

                    if (this.vinesGrow) {
                        for (int k3 = position.getY() - 3 + factor; k3 <= position.getY() + factor; ++k3) {
                            int j4 = k3 - (position.getY() + factor);
                            int k4 = 2 - j4 / 2;

                            for (int l4 = position.getX() - k4; l4 <= position.getX() + k4; ++l4) {
                                for (int i5 = position.getZ() - k4; i5 <= position.getZ() + k4; ++i5) {
                                    blockPos.setPos(l4, k3, i5);

                                    state = selector.getState(blockPos);

                                    if (state.getBlock().isLeaves(state, worldIn, blockPos)) {
                                        if (rand.nextInt(4) == 0
                                                && selector.isAirBlock(temp = blockPos.west())) {
                                            this.addHangingVine(selector, temp, BlockVine.EAST);
                                        }
                                        if (rand.nextInt(4) == 0
                                                && selector.isAirBlock(temp = blockPos.east())) {
                                            this.addHangingVine(selector, temp, BlockVine.WEST);
                                        }
                                        if (rand.nextInt(4) == 0
                                                && selector.isAirBlock(temp = blockPos.north())) {
                                            this.addHangingVine(selector, temp, BlockVine.SOUTH);
                                        }
                                        if (rand.nextInt(4) == 0
                                                && selector.isAirBlock(temp = blockPos.south())) {
                                            this.addHangingVine(selector, temp, BlockVine.NORTH);
                                        }
                                    }
                                }
                            }
                        }
                        if (rand.nextInt(5) == 0 && factor > 5) {
                            EnumFacing facing;
                            for (int l3 = 0; l3 < 2; ++l3) {
                                for (EnumFacing enumfacing : BlockIDIndexes.FACING_HORIZONTAL) {
                                    if (rand.nextInt(4 - l3) == 0) {
                                        facing = enumfacing.getOpposite();
                                        this.placeCocoa(selector, rand.nextInt(3), position.add(facing.getXOffset(), factor - 5 + l3, facing.getZOffset()), enumfacing);
                                    }
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


    private void placeCocoa(ChunkSelector selector, int num, BlockPos pos, EnumFacing side) {
        this.setBlockAndNotifyAdequately(selector, pos, Blocks.COCOA.getDefaultState().withProperty(BlockCocoa.AGE, num).withProperty(BlockCocoa.FACING, side));
    }


    private void addVine(ChunkSelector selector, BlockPos pos, PropertyBool prop) {
        this.setBlockAndNotifyAdequately(selector, pos, Blocks.VINE.getDefaultState().withProperty(prop, Boolean.TRUE));
    }


    private void addHangingVine(ChunkSelector selector, BlockPos pos, PropertyBool prop) {
        this.addVine(selector, pos, prop);
        int i = 4;

        for (BlockPos blockpos = pos.down(); selector.isAirBlock(blockpos) && i > 0; --i) {
            this.addVine(selector, blockpos, prop);
            blockpos = blockpos.down();
        }
    }
}