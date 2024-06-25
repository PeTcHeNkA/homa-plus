package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import plus.fserver.server.elca.ChunkSelector;


public class WorldGenIceSpike extends WorldGenerator {
    public WorldGenIceSpike() {}


    @Override
    public boolean generate(World world, Random rand, BlockPos pos) {
        ChunkSelector selector = new ChunkSelector(world);
        while(selector.isAirBlock(pos) && pos.getY() > 2) {
            pos = pos.down();
        }
        if (selector.getBlockState(pos).getBlock() != Blocks.SNOW) {
            return false;
        } else {
            pos = pos.up(rand.nextInt(4));

            int i = rand.nextInt(4) + 7;
            int j = i / 4 + rand.nextInt(2);
            if (j > 1 && rand.nextInt(60) == 0) {
                pos = pos.up(10 + rand.nextInt(30));
            }

            int sX = pos.getX(), sY = pos.getY(), sZ = pos.getZ();
            BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();

            int y;
            int lim;
            for(y = 0; y < i; ++y) {
                float yFactor = (1.0F - (float)y / (float)i) * (float)j;
                lim = MathHelper.ceil(yFactor);

                for(int x = -lim; x <= lim; ++x) {
                    float f1 = (float)MathHelper.abs(x) - 0.25F;

                    for(int z = -lim; z <= lim; ++z) {
                        float f2 = (float)MathHelper.abs(z) - 0.25F;
                        if ((x == 0 && z == 0 || f1 * f1 + f2 * f2 <= yFactor * yFactor) && (x != -lim && x != lim && z != -lim && z != lim || rand.nextFloat() <= 0.75F)) {

                            IBlockState state = selector.getBlockState(mut.setPos(sX+z, sY+y, sZ+z));
                            Block block = state.getBlock();

                            if (state.getBlock().isAir(state, selector, mut) || block == Blocks.DIRT || block == Blocks.SNOW || block == Blocks.ICE) {
                                this.setBlockAndNotifyAdequately(selector, mut, Blocks.PACKED_ICE.getDefaultState());
                            }

                            if (y != 0 && lim > 1) {
                                mut.setY(sY-y);

                                state = selector.getBlockState(mut);
                                block = state.getBlock();
                                if (state.getBlock().isAir(state, selector, mut) || block == Blocks.DIRT || block == Blocks.SNOW || block == Blocks.ICE) {
                                    this.setBlockAndNotifyAdequately(selector, mut, Blocks.PACKED_ICE.getDefaultState());
                                }
                            }
                        }
                    }
                }
            }

            y = j - 1;
            if (y < 0) {
                y = 0;
            } else if (y > 1) {
                y = 1;
            }

            for(int l1 = -y; l1 <= y; ++l1) {
                for(lim = -y; lim <= y; ++lim) {
                    BlockPos blockpos = pos.add(l1, -1, lim);
                    int j2 = 50;
                    if (Math.abs(l1) == 1 && Math.abs(lim) == 1) {
                        j2 = rand.nextInt(5);
                    }

                    while(blockpos.getY() > 50) {
                        IBlockState iblockstate1 = selector.getBlockState(blockpos);
                        Block block1 = iblockstate1.getBlock();
                        if (!iblockstate1.getBlock().isAir(iblockstate1, selector, blockpos) && block1 != Blocks.DIRT && block1 != Blocks.SNOW && block1 != Blocks.ICE && block1 != Blocks.PACKED_ICE) {
                            break;
                        }

                        this.setBlockAndNotifyAdequately(selector, blockpos, Blocks.PACKED_ICE.getDefaultState());
                        blockpos = blockpos.down();
                        --j2;
                        if (j2 <= 0) {
                            blockpos = blockpos.down(rand.nextInt(5) + 1);
                            j2 = rand.nextInt(5);
                        }
                    }
                }
            }

            return true;
        }
    }
}