package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import plus.fserver.utls.ChunkSelector;


public class WorldGenLiquids extends WorldGenerator {
    private final IBlockState state;

    public WorldGenLiquids(Block blockIn) {
        this.state = blockIn.getDefaultState();
    }


    @Override
    public boolean generate(World worldIn, Random rand, BlockPos position) {
        Chunk chunk = worldIn.getChunk(position.getX()>>4, position.getZ()>>4);
        if (chunk.getBlockState(position.up()).getBlock() != Blocks.STONE) {
            return false;
        }
        else if (worldIn.getBlockState(position.down()).getBlock() != Blocks.STONE) {
            return false;
        } else {
            IBlockState iblockstate = chunk.getBlockState(position);

            if (!iblockstate.getBlock().isAir(iblockstate, worldIn, position)
                    && iblockstate.getBlock() != Blocks.STONE) {
                return false;
            } else {
                int i = 0;
                ChunkSelector selector = new ChunkSelector(chunk);

                if (selector.getState(position.west()).getBlock() == Blocks.STONE) {
                    ++i;
                }

                if (selector.getState(position.east()).getBlock() == Blocks.STONE) {
                    ++i;
                }

                if (selector.getState(position.north()).getBlock() == Blocks.STONE) {
                    ++i;
                }

                if (selector.getState(position.south()).getBlock() == Blocks.STONE) {
                    ++i;
                }

                int j = 0;

                if (selector.isAirBlock(position.west())) {
                    ++j;
                }

                if (selector.isAirBlock(position.east())) {
                    ++j;
                }

                if (selector.isAirBlock(position.north())) {
                    ++j;
                }

                if (selector.isAirBlock(position.south())) {
                    ++j;
                }

                if (i == 3 && j == 1) {
                    selector.setStateFast(position.getX(), position.getY(), position.getZ(), state);
                    selector.current().relight(position);
                    worldIn.scheduleBlockUpdate(position, state.getBlock(), 0, 0);
                    //worldIn.immediateBlockTick(position, state, rand);
                }
                return true;
            }
        }
    }
}