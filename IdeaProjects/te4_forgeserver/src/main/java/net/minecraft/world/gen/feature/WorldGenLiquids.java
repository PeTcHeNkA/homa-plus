package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
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
    public boolean generate(World world, ChunkSelector selector, Random rand, BlockPos pos) {
        Chunk chunk = world.getChunk(pos.getX()>>4, pos.getZ()>>4);
        if (chunk.getBlockState(pos.getX(), pos.getY()+1, pos.getZ()).getBlock() != Blocks.STONE) {
            return false;
        }
        else if (world.getBlockState(pos.getX(), pos.getY()-1, pos.getZ()).getBlock() != Blocks.STONE) {
            return false;
        } else {
            IBlockState iblockstate = chunk.getBlockState(pos);

            if (!iblockstate.getBlock().isAir(iblockstate, world, pos)
                    && iblockstate.getBlock() != Blocks.STONE) {
                return false;
            } else {
                int stone = 0, air = 0;

                IBlockState state1 = selector.getState(pos.getX()-1, pos.getY(), pos.getZ());
                IBlockState state2 = selector.getState(pos.getX()+1, pos.getY(), pos.getZ());
                IBlockState state3 = selector.getState(pos.getX(), pos.getY(), pos.getZ()+1);
                IBlockState state4 = selector.getState(pos.getX(), pos.getY(), pos.getZ()-1);

                if(state1 == Blocks.STONE){
                    ++stone;
                } else if(state1.getMaterial() == Material.AIR){
                    ++air;
                }

                if(state2 == Blocks.STONE){
                    ++stone;
                } else if(state2.getMaterial() == Material.AIR){
                    ++air;
                }

                if(state3 == Blocks.STONE){
                    ++stone;
                } else if(state3.getMaterial() == Material.AIR){
                    ++air;
                }

                if(state4 == Blocks.STONE){
                    ++stone;
                } else if(state4.getMaterial() == Material.AIR){
                    ++air;
                }

                if (stone == 3 && air == 1) {
                    selector.setStateFast(pos.getX(), pos.getY(), pos.getZ(), state);
                    selector.current().relight(pos);
                    //worldIn.scheduleBlockUpdate(pos, state.getBlock(), 0, 0);/
                    ((WorldServer) world).addSyncTask(() -> world.immediateBlockTick(pos, state, rand));
                }
                return true;
            }
        }
    }
}