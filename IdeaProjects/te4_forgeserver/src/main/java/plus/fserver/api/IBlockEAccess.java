package plus.fserver.api;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;


public interface IBlockEAccess extends IBlockAccess {
    IBlockState getBlockState(int x, int y, int z);
    boolean setBlockState(BlockPos pos, IBlockState state);
    boolean setBlockState(BlockPos pos, IBlockState state, int flags);
}