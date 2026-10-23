package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import plus.fserver.server.elca.ChunkSelector;


public class WorldGenGlowStone1 extends WorldGenerator {
    static final IBlockState GLOW_STONE_DEF_STATE = Blocks.GLOWSTONE.getDefaultState();

    @Override
    public boolean generate(World worldIn, Random rand, BlockPos position) {
        if (!worldIn.isAirBlock(position)) {
            return false;
        }
        Chunk chunk = worldIn.getChunk(position);
        if (chunk.getBlockState(position.up()).getBlock() != Blocks.NETHERRACK) {
            return false;
        }
        else {
            ChunkSelector selector = new ChunkSelector(chunk);
            chunk.setBlockState(position, GLOW_STONE_DEF_STATE);

            BlockPos pos0, blockpos;

            for (int i = 0; i < 1500; ++i) {
                blockpos = position.add(rand.nextInt(8) - rand.nextInt(8), -rand.nextInt(12), rand.nextInt(8) - rand.nextInt(8));

                if (blockpos.getY() > 0 && blockpos.getY() < 255 && selector.isAirBlock(position)) {
                    int j = 0;

                    for (EnumFacing enumfacing : EnumFacing.values()) {
                        pos0 = blockpos.offset(enumfacing);
                        if (selector.getState(pos0).getBlock() == Blocks.GLOWSTONE) {
                            ++j;
                        }
                        if (j > 1) {
                            break;
                        }
                    }
                    if (j == 1) {
                        selector.setBlockState(blockpos, Blocks.GLOWSTONE.getDefaultState());
                    }
                }
            }

            return true;
        }
    }
}