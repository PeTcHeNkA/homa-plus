package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.BlockPumpkin;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing.Plane;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import plus.fserver.utls.ChunkSelector;
import plus.fserver.utls.math.FastRandom;


public class WorldGenPumpkin extends CWorldGenerator {
    private final IBlockState state;

    public WorldGenPumpkin() {
        state = Blocks.PUMPKIN.getDefaultState();
    }

    public WorldGenPumpkin(IBlockState state) {
        this.state = state;
    }


    @Override
    public boolean generate(World world, ChunkSelector selector, Random rand, BlockPos position) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for(int num = 0; num < 64; ++num) {
            FastRandom.INST.setRandom8x8x4(pos, position);
            if (pos.getY() < 255 && pos.getY() > 0 && selector.isAirBlock(pos) && selector.getState(pos.down()).getBlock() == Blocks.GRASS
                    && Blocks.PUMPKIN.canPlaceBlockAt(world, pos)) {

                selector.setBlockState(pos, state.withProperty(BlockPumpkin.FACING, Plane.HORIZONTAL.random(rand)));
            }
        }
        return true;
    }
}
