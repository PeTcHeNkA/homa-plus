package net.minecraft.world.gen.feature;

import java.util.Random;

import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import plus.fserver.server.elca.ChunkSelector;
import plus.fserver.utls.math.FastRandom;

import static net.minecraft.world.gen.feature.WorldGenGlowStone1.GLOW_STONE_DEF_STATE;


public class WorldGenGlowStone2 extends WorldGenerator {
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
            //IBlockState state;
            BlockPos pos0;
            BlockPos.MutableBlockPos blockpos = new BlockPos.MutableBlockPos();

            for (int i = 0; i < 1500; ++i) {
                FastRandom.INST.setRandom8x8x4(blockpos, position);
//                blockpos = position.add(
//                        rand.nextInt(8) - rand.nextInt(8),
//                        rand.nextInt(3) - rand.nextInt(3),
//                        rand.nextInt(8) - rand.nextInt(8)
//                );
                //state = selector.getState(blockpos);
                if (blockpos.getY()>0 && blockpos.getY() < 255 && selector.isAirBlock(blockpos)) {
                    int j = 0;

                    for (EnumFacing enumfacing : EnumFacing.VALUES) {
                        pos0 = blockpos.offset(enumfacing);
                        if (selector.getState(pos0).getBlock() == Blocks.GLOWSTONE) {
                            ++j;
                        }
                        if (j > 1) break;
                    }
                    if (j == 1) {
                        selector.setBlockState(blockpos, GLOW_STONE_DEF_STATE);
                    }
                }
            }
            return true;
        }
    }
}