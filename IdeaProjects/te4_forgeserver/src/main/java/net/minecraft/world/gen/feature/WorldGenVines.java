package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.BlockVine;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import plus.fserver.server.elca.ChunkSelector;


public class WorldGenVines extends WorldGenerator {
    public WorldGenVines() {}


    public boolean generate(World world, Random random, BlockPos pos) {
        for(ChunkSelector selector = new ChunkSelector(world);pos.getY() < 128; pos = pos.up()) {
            if (selector.isAirBlock(pos)) {
                for (EnumFacing facing : Chunk.HORIZONTAL) {
                    if (Blocks.VINE.canPlaceBlockOnSide(world, pos, facing)) {
                        IBlockState state = Blocks.VINE.getDefaultState().withProperty(BlockVine.NORTH, facing == EnumFacing.NORTH).withProperty(BlockVine.EAST, facing == EnumFacing.EAST).withProperty(BlockVine.SOUTH, facing == EnumFacing.SOUTH).withProperty(BlockVine.WEST, facing == EnumFacing.WEST);
                        selector.setCurState(pos, state);
                        break;
                    }
                }
            } else {
                pos = pos.add(random.nextInt(4) - random.nextInt(4), 0, random.nextInt(4) - random.nextInt(4));
            }
        }
        return true;
    }
}