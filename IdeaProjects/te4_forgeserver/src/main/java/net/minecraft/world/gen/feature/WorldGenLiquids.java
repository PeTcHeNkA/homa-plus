package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import plus.fserver.api.Keep;
import plus.fserver.server.elca.ChunkSelector;


public class WorldGenLiquids extends CWorldGenerator {
    private final IBlockState state;

    @Keep
    public WorldGenLiquids(Block blockIn) {
        this.state = blockIn.getDefaultState();
    }


    @Override
    public boolean generate(World worldIn, ChunkSelector selector, Random rand, BlockPos pos) {
        Chunk chunk = worldIn.getChunk(pos.getX()>>4, pos.getZ()>>4);
        if (chunk.getBlockState(pos.getX(), pos.getY()+1, pos.getZ()).getBlock() != Blocks.STONE) {
            return false;
        }
        else if (worldIn.getBlockState(pos.down()).getBlock() != Blocks.STONE) {
            return false;
        } else {
            IBlockState iblockstate = chunk.getBlockState(pos);

            if (!iblockstate.getBlock().isAir(iblockstate, worldIn, pos)
                    && iblockstate.getBlock() != Blocks.STONE) {
                return false;
            } else {
                int i = 0;

                if (selector.getState(pos.west()).getBlock() == Blocks.STONE) {
                    ++i;
                }

                if (selector.getState(pos.east()).getBlock() == Blocks.STONE) {
                    ++i;
                }

                if (selector.getState(pos.north()).getBlock() == Blocks.STONE) {
                    ++i;
                }

                if (selector.getState(pos.south()).getBlock() == Blocks.STONE) {
                    ++i;
                }

                int j = 0;

                if (selector.isAirBlock(pos.west())) {
                    ++j;
                }

                if (selector.isAirBlock(pos.east())) {
                    ++j;
                }

                if (selector.isAirBlock(pos.north())) {
                    ++j;
                }

                if (selector.isAirBlock(pos.south())) {
                    ++j;
                }

                if (i == 3 && j == 1) {
                    selector.setStateFast(pos.getX(), pos.getY(), pos.getZ(), state);
                    selector.current().relight(pos);
                    //worldIn.scheduleBlockUpdate(pos, state.getBlock(), 0, 0);/
                    ((WorldServer)worldIn).addSyncTask(() -> worldIn.immediateBlockTick(pos, state, rand));
                }
                return true;
            }
        }
    }
}