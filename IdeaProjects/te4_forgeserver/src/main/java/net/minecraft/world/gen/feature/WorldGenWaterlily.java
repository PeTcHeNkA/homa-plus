package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import plus.fserver.api.Keep;
import plus.fserver.server.elca.ChunkSelector;
import plus.fserver.utls.math.FastRandom;


public class WorldGenWaterlily extends CWorldGenerator {
    private static final IBlockState WATERLILY = Blocks.WATERLILY.getDefaultState();

    @Keep
    public WorldGenWaterlily() {}


    @Override
    public boolean generate(World world, ChunkSelector selector, Random rand, BlockPos pos) {
        BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();

        for(int count = 0; count < 10; ++count) {
            FastRandom.INST.setRandom8x8x4(blockPos, pos);
            if (blockPos.getY()>0 && blockPos.getY() < 255 & selector.isAirBlock(blockPos) && Blocks.WATERLILY.canPlaceBlockAt(world, blockPos)) {
                selector.setCurState(blockPos, WATERLILY);
            }
        }
        return true;
    }
}