package net.minecraft.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import plus.fserver.utls.collections.ArrayListE6;
import static plus.fserver.block.BlockFs.addCollisionBoxToListFs;


public class BlockEmptyDropsFs extends BlockEmptyDrops {
    public BlockEmptyDropsFs(Material materialIn) {
        super(materialIn);
    }


    @Override
    public void addCollisionBoxToListF(IBlockState state, IBlockAccess access, BlockPos pos, AxisAlignedBB entityBox, ArrayListE6<AxisAlignedBB> collidingBoxes) {
        addCollisionBoxToListFs(pos, entityBox, collidingBoxes, state.getCollisionBoundingBox(access, pos));
    }
}