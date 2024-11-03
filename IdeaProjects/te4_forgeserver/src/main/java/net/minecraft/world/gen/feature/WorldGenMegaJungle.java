package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.BlockVine;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import plus.fserver.api.Keep;
import plus.fserver.server.ElcaThread;
import plus.fserver.server.elca.ChunkSelector;


public class WorldGenMegaJungle extends WorldGenHugeTrees {
    @Keep
    public WorldGenMegaJungle(boolean notify, int baseHeightIn, int extraRandomHeightIn, IBlockState woodMetadataIn, IBlockState leaves) {
        super(notify, baseHeightIn, extraRandomHeightIn, woodMetadataIn, leaves);
    }


    @Override
    public boolean generate(World world, Random rand, BlockPos pos) {
        return generate(world, ElcaThread.cache(world), rand, pos);
    }


    @Override
    public boolean generate(World worldIn, ChunkSelector selector, Random rand, BlockPos position) {
        int i = this.getHeight(rand);
        if (!this.ensureGrowable(selector, worldIn, position, i)) {
            return false;
        } else {
            boolean notify = this.needNotify();
            IBlockState wood = this.woodMetadata;
            this.createCrown(selector, position.up(i), 2);

            for (int j = position.getY() + i - 2 - rand.nextInt(4); j > position.getY() + i / 2; j -= 2 + rand.nextInt(4)) {
                float f = rand.nextFloat() * ((float)Math.PI * 2F);
                int k = position.getX() + (int)(0.5F + MathHelper.cos(f) * 4.0F);
                int l = position.getZ() + (int)(0.5F + MathHelper.sin(f) * 4.0F);

                for (int i1 = 0; i1 < 5; ++i1) {
                    k = position.getX() + (int)(1.5F + MathHelper.cos(f) * (float)i1);
                    l = position.getZ() + (int)(1.5F + MathHelper.sin(f) * (float)i1);
                    setBlockAndNotifyAdequately(selector, new BlockPos(k, j - 3 + i1 / 2, l), wood, notify);
                }

                int j2 = 1 + rand.nextInt(2);

                for (int k1 = j - j2; k1 <= j; ++k1) {
                    int l1 = k1 - j;
                    this.growLeavesLayer(selector, new BlockPos(k, k1, l), 1 - l1);
                }
            }

            for (int i2 = 0; i2 < i; ++i2) {
                BlockPos blockpos = position.up(i2);

                if (this.isAirLeaves(selector, worldIn, blockpos)) {
                    setBlockAndNotifyAdequately(selector, blockpos, wood, notify);

                    if (i2 > 0) {
                        placeVine(selector, rand, blockpos.west(), BlockVine.EAST, notify);
                        placeVine(selector, rand, blockpos.north(), BlockVine.SOUTH, notify);
                    }
                }

                if (i2 < i - 1) {
                    BlockPos blockpos1 = blockpos.east();

                    if (this.isAirLeaves(selector, worldIn, blockpos1)) {
                        setBlockAndNotifyAdequately(selector, blockpos1, wood, notify);

                        if (i2 > 0) {
                            placeVine(selector, rand, blockpos1.east(), BlockVine.WEST, notify);
                            placeVine(selector, rand, blockpos1.north(), BlockVine.SOUTH, notify);
                        }
                    }
                    BlockPos blockpos2 = blockpos.south().east();

                    if (this.isAirLeaves(selector, worldIn, blockpos2)) {
                        setBlockAndNotifyAdequately(selector, blockpos2, wood, notify);

                        if (i2 > 0) {
                            placeVine(selector, rand, blockpos2.east(), BlockVine.WEST, notify);
                            placeVine(selector, rand, blockpos2.south(), BlockVine.NORTH, notify);
                        }
                    }
                    BlockPos blockpos3 = blockpos.south();

                    if (this.isAirLeaves(selector, worldIn, blockpos3)) {
                        setBlockAndNotifyAdequately(selector, blockpos3, wood, notify);
                        if (i2 > 0) {
                            placeVine(selector, rand, blockpos3.west(), BlockVine.EAST, notify);
                            placeVine(selector, rand, blockpos3.south(), BlockVine.NORTH, notify);
                        }
                    }
                }
            }
            return true;
        }
    }


    private static void placeVine(ChunkSelector selector, Random random, BlockPos pos, PropertyBool bool, boolean notify) {
        if (random.nextInt(3) > 0 && selector.isAirBlock(pos)) {
            setBlockAndNotifyAdequately(selector, pos, Blocks.VINE.getDefaultState().withProperty(bool, Boolean.TRUE), notify);
        }
    }


    private void createCrown(ChunkSelector selector, BlockPos pos, int num) {
        for (int j = -2; j <= 0; ++j) {
            this.growLeavesLayerStrict(selector, pos.up(j), num + 1 - j);
        }
    }


    //Helper macro
    private boolean isAirLeaves(ChunkSelector selector, World world, BlockPos pos) {
        IBlockState state = selector.getState(pos);
        return state.getBlock().isAir(state, world, pos) || state.getBlock().isLeaves(state, world, pos);
    }
}