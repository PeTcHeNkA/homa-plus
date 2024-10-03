package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockOldLeaf;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockVine;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import plus.fserver.api.Keep;
import plus.fserver.server.elca.ChunkSelector;


public class WorldGenSwamp extends WorldGenAbstractTree {
    private static final IBlockState TRUNK = Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.OAK);
    private static final IBlockState LEAF = Blocks.LEAVES.getDefaultState().withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.OAK).withProperty(BlockOldLeaf.CHECK_DECAY, Boolean.valueOf(false));

    @Keep
    public WorldGenSwamp() {
        super(false);
    }


    @Override
    public boolean generate(World worldIn, Random rand, BlockPos position) {
        return generate(worldIn, new ChunkSelector(worldIn), rand, position);
    }


    @Override
    public boolean generate(World worldIn, ChunkSelector selector, Random rand, BlockPos position0) {
        int i;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(position0);
        pos.setY(pos.getY()-1);
        selector.getChunk(pos);

        for (i = rand.nextInt(4) + 5;
             selector.getCurState(pos).getMaterial() == Material.WATER;
             pos.setY(pos.getY()-1));

        position0 = pos.toImmutable();

        boolean flag = true;

        if (position0.getY() >= 1 && position0.getY() + i + 1 <= 256) {
            for (int j = position0.getY(); j <= position0.getY() + 1 + i; ++j) {
                int k = 1;

                if (j == position0.getY()) {
                    k = 0;
                }

                if (j >= position0.getY() + 1 + i - 2) {
                    k = 3;
                }

                for (int l = position0.getX() - k; l <= position0.getX() + k && flag; ++l) {
                    for (int i1 = position0.getZ() - k; i1 <= position0.getZ() + k && flag; ++i1) {
                        if (j >= 0 && j < 256) {
                            IBlockState iblockstate = selector.getState(pos.setPos(l, j, i1));
                            Block block = iblockstate.getBlock();

                            if (!iblockstate.getBlock().isAir(iblockstate, worldIn, pos.setPos(l, j, i1)) && !iblockstate.getBlock().isLeaves(iblockstate, worldIn, pos.setPos(l, j, i1))) {
                                if (block != Blocks.WATER && block != Blocks.FLOWING_WATER) {
                                    flag = false;
                                }
                                else if (j > position0.getY()) {
                                    flag = false;
                                }
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
                boolean notify = needNotify();

                BlockPos temp = position0.down();
                IBlockState state = worldIn.getBlockState(temp);
                boolean isSoil = state.getBlock().canSustainPlant(state, worldIn, temp, net.minecraft.util.EnumFacing.UP, ((net.minecraft.block.BlockSapling)Blocks.SAPLING));

                if (isSoil && position0.getY() < worldIn.getHeight() - i - 1) {
                    state.getBlock().onPlantGrow(state, worldIn, position0.down(), position0);

                    for (int k1 = position0.getY() - 3 + i; k1 <= position0.getY() + i; ++k1) {
                        int j2 = k1 - (position0.getY() + i);
                        int l2 = 2 - j2 / 2;

                        for (int j3 = position0.getX() - l2; j3 <= position0.getX() + l2; ++j3) {
                            int k3 = j3 - position0.getX();

                            for (int i4 = position0.getZ() - l2; i4 <= position0.getZ() + l2; ++i4) {
                                int j1 = i4 - position0.getZ();

                                if (Math.abs(k3) != l2 || Math.abs(j1) != l2 || rand.nextInt(2) != 0 && j2 != 0) {
                                    pos.setPos(j3, k1, i4);
                                    //BlockPos blockpos = new BlockPos(j3, k1, i4);
                                    state = selector.getState(pos);

                                    if (state.getBlock().canBeReplacedByLeaves(state, worldIn, pos)) {
                                        setBlockAndNotifyAdequately(selector, pos, LEAF, notify);
                                    }
                                }
                            }
                        }
                    }

                    Block block2;
                    for (int l1 = 0; l1 < i; ++l1) {
                        temp = position0.up(l1);
                        state = selector.getState(temp);
                        block2 = state.getBlock();

                        if (block2.isAir(state, worldIn, temp) || block2.isLeaves(state, worldIn, temp) || block2 == Blocks.FLOWING_WATER || block2 == Blocks.WATER) {
                            setBlockAndNotifyAdequately(selector, position0.up(l1), TRUNK, notify);
                        }
                    }

                    for (int posY = position0.getY() - 3 + i; posY <= position0.getY() + i; ++posY) {
                        int k2 = posY - (position0.getY() + i);
                        int i3 = 2 - k2 / 2;

                        for (int posX = position0.getX() - i3; posX <= position0.getX() + i3; ++posX) {
                            for (int posZ = position0.getZ() - i3; posZ <= position0.getZ() + i3; ++posZ) {

                                if (selector.getState(pos).getMaterial() == Material.LEAVES) {
                                    pos.setPos(posX-1, posY, posZ);

                                    if (rand.nextInt(4) == 0 && selector.isAirBlock(pos)) {
                                        addVine(selector, pos, BlockVine.EAST, notify);
                                    }
                                    pos.setPos(posX+1, posY, posZ);

                                    if (rand.nextInt(4) == 0 && selector.isAirBlock(pos)) {
                                        addVine(selector, pos, BlockVine.WEST, notify);
                                    }
                                    pos.setPos(posX, posY, posZ-1);

                                    if (rand.nextInt(4) == 0 && selector.isAirBlock(pos)) {
                                        addVine(selector, pos, BlockVine.SOUTH, notify);
                                    }
                                    pos.setPos(posX, posY, posZ+1);

                                    if (rand.nextInt(4) == 0 && selector.isAirBlock(pos)) {
                                        addVine(selector, pos, BlockVine.NORTH, notify);
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


    private static void addVine(ChunkSelector selector, BlockPos.MutableBlockPos pos, PropertyBool prop, boolean notify) {
        IBlockState iblockstate = Blocks.VINE.getDefaultState().withProperty(prop, Boolean.TRUE);
        setBlockAndNotifyAdequately(selector, pos, iblockstate, notify);
        int i = 4;

        for (pos.setY(pos.getY()-1); selector.isAirBlock(pos) && i > 0; --i) {
            setBlockAndNotifyAdequately(selector, pos, iblockstate, notify);
            pos.setY(pos.getY()-1);
        }
    }
}