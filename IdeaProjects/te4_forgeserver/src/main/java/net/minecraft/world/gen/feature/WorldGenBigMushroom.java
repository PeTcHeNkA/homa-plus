package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHugeMushroom;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import plus.fserver.api.Keep;
import plus.fserver.server.elca.ChunkSelector;


public class WorldGenBigMushroom extends WorldGenerator {
    private final Block mushroomType;

    public WorldGenBigMushroom(Block block) {
        super(true);
        this.mushroomType = block;
    }

    @Keep
    public WorldGenBigMushroom() {
        super(false);
        this.mushroomType = null;
    }


    @Override
    public boolean generate(World worldIn, Random rand, BlockPos position) {
        return generate(worldIn, null, rand, position);
    }


    @Override
    public boolean generate(World world, @Nullable ChunkSelector selector, Random rand, BlockPos position) {
        Block block = this.mushroomType;
        boolean notify = needNotify();

        if (block == null) {
            block = rand.nextBoolean() ? Blocks.BROWN_MUSHROOM_BLOCK : Blocks.RED_MUSHROOM_BLOCK;
        }

        int i = rand.nextInt(3) + 4;

        if (rand.nextInt(12) == 0) {
            i *= 2;
        }

        boolean flag = true;

        if (position.getY() >= 1 && position.getY() + i + 1 < 256) {
            if(selector == null) selector = new ChunkSelector(world);
            for (int j = position.getY(); j <= position.getY() + 1 + i; ++j) {
                int k = 3;

                if (j <= position.getY() + 3) {
                    k = 0;
                }

                BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();

                for (int l = position.getX() - k; l <= position.getX() + k && flag; ++l) {
                    for (int i1 = position.getZ() - k; i1 <= position.getZ() + k && flag; ++i1) {
                        if (j >= 0 && j < 256) {
                            IBlockState state = selector.getState(blockPos.setPos(l, j, i1));

                            if (!state.getBlock().isAir(state, world, blockPos) && !state.getBlock().isLeaves(state, world, blockPos)) {
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
                Block block1 = selector.getState(position.down()).getBlock();

                if (block1 != Blocks.DIRT && block1 != Blocks.GRASS && block1 != Blocks.MYCELIUM) {
                    return false;
                } else {
                    int k2 = position.getY() + i;

                    if (block == Blocks.RED_MUSHROOM_BLOCK) {
                        k2 = position.getY() + i - 3;
                    }

                    for (int l2 = k2; l2 <= position.getY() + i; ++l2) {
                        int j3 = 1;

                        if (l2 < position.getY() + i) {
                            ++j3;
                        }

                        if (block == Blocks.BROWN_MUSHROOM_BLOCK) {
                            j3 = 3;
                        }

                        int k3 = position.getX() - j3;
                        int l3 = position.getX() + j3;
                        int j1 = position.getZ() - j3;
                        int k1 = position.getZ() + j3;

                        for (int l1 = k3; l1 <= l3; ++l1) {
                            for (int i2 = j1; i2 <= k1; ++i2) {
                                int j2 = 5;

                                if (l1 == k3) {
                                    --j2;
                                } else if (l1 == l3) {
                                    ++j2;
                                }

                                if (i2 == j1) {
                                    j2 -= 3;
                                } else if (i2 == k1) {
                                    j2 += 3;
                                }

                                BlockHugeMushroom.EnumType type = BlockHugeMushroom.EnumType.byMetadata(j2);

                                if (block == Blocks.BROWN_MUSHROOM_BLOCK || l2 < position.getY() + i) {
                                    if ((l1 == k3 || l1 == l3) && (i2 == j1 || i2 == k1)) {
                                        continue;
                                    }

                                    if (l1 == position.getX() - (j3 - 1) && i2 == j1) {
                                        type = BlockHugeMushroom.EnumType.NORTH_WEST;
                                    }

                                    if (l1 == k3 && i2 == position.getZ() - (j3 - 1)) {
                                        type = BlockHugeMushroom.EnumType.NORTH_WEST;
                                    }

                                    if (l1 == position.getX() + (j3 - 1) && i2 == j1) {
                                        type = BlockHugeMushroom.EnumType.NORTH_EAST;
                                    }

                                    if (l1 == l3 && i2 == position.getZ() - (j3 - 1)) {
                                        type = BlockHugeMushroom.EnumType.NORTH_EAST;
                                    }

                                    if (l1 == position.getX() - (j3 - 1) && i2 == k1) {
                                        type = BlockHugeMushroom.EnumType.SOUTH_WEST;
                                    }

                                    if (l1 == k3 && i2 == position.getZ() + (j3 - 1)) {
                                        type = BlockHugeMushroom.EnumType.SOUTH_WEST;
                                    }

                                    if (l1 == position.getX() + (j3 - 1) && i2 == k1) {
                                        type = BlockHugeMushroom.EnumType.SOUTH_EAST;
                                    }

                                    if (l1 == l3 && i2 == position.getZ() + (j3 - 1)) {
                                        type = BlockHugeMushroom.EnumType.SOUTH_EAST;
                                    }
                                }

                                if (type == BlockHugeMushroom.EnumType.CENTER && l2 < position.getY() + i) {
                                    type = BlockHugeMushroom.EnumType.ALL_INSIDE;
                                }

                                if (position.getY() >= position.getY() + i - 1 || type != BlockHugeMushroom.EnumType.ALL_INSIDE) {
                                    BlockPos blockpos = new BlockPos(l1, l2, i2);
                                    IBlockState state = selector.getState(blockpos);

                                    if (state.getBlock().canBeReplacedByLeaves(state, world, blockpos)) {
                                        setBlockAndNotifyAdequately(selector, blockpos, block.getDefaultState().withProperty(BlockHugeMushroom.VARIANT, type), notify);
                                    }
                                }
                            }
                        }
                    }
                    BlockPos temp;
                    for (int i3 = 0; i3 < i; ++i3) {
                        IBlockState iblockstate = selector.getState(temp = position.up(i3));

                        if (iblockstate.getBlock().canBeReplacedByLeaves(iblockstate, world, temp)) {
                            setBlockAndNotifyAdequately(selector, temp, block.getDefaultState().withProperty(BlockHugeMushroom.VARIANT, BlockHugeMushroom.EnumType.STEM), notify);
                        }
                    }
                    return true;
                }
            }
        } else {
            return false;
        }
    }
}