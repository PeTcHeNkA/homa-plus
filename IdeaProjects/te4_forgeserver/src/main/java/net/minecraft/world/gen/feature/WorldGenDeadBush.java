package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import plus.fserver.server.elca.ChunkSelector;
import plus.fserver.utls.math.FastRandom;


public class WorldGenDeadBush extends CWorldGenerator {
    @Override
    public boolean generate(World world, ChunkSelector selector, Random rand, BlockPos position0) {
        BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos(position0);
        for (IBlockState iblockstate = selector.getState(mut);
             (iblockstate.getBlock().isAir(iblockstate, world, mut) ||
                     iblockstate.getBlock().isLeaves(iblockstate, world, mut))
                     && mut.getY() > 0; iblockstate = world.getBlockState(mut))
        {
            mut.setY(mut.getY()-1);
        }
        position0 = mut.toImmutable();

        for (int i = 0; i < 4; ++i) {
            FastRandom.setRandom8x8x4(rand, mut, position0);

            if (mut.getY() > 0 && mut.getY() < 255 && selector.isAirBlock(mut)
                    && Blocks.DEADBUSH.canBlockStay(world, mut, Blocks.DEADBUSH.getDefaultState())) {
                selector.setCurStateFast(mut.getX(), mut.getY(), mut.getZ(),
                        Blocks.DEADBUSH.getDefaultState());
            }
        }
        return true;
    }
}