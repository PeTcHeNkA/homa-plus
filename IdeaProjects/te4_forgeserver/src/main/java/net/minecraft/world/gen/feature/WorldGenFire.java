package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import plus.fserver.utls.ChunkSelector;
import plus.fserver.utls.math.FastRandom;


public class WorldGenFire extends WorldGenerator {
    private static final IBlockState FIRE_STATE = Blocks.FIRE.getDefaultState();

    @Override
    public boolean generate(World worldIn, Random rand, BlockPos position) {
        ChunkSelector selector = new ChunkSelector(worldIn);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int i = 0; i < 64; ++i) {
            FastRandom.INST.setRandom8x8x4(pos, position);
            if (pos.getY()>0 && pos.getY() < 255 && worldIn.isAirBlock(pos)) {
                pos.setY(pos.getY()-1);
                if(selector.getState(pos).getBlock() == Blocks.NETHERRACK){
                    pos.setY(pos.getY()+1);
                    selector.setBlockState(pos, FIRE_STATE, 2);
                }
            }
        }
        return true;
    }
}