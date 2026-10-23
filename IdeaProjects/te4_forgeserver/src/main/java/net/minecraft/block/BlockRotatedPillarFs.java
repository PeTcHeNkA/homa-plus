package net.minecraft.block;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import plus.fserver.utls.collections.AABBList;
import static plus.fserver.block.BlockFs.addCollisionBoxToListFs;


public class BlockRotatedPillarFs extends BlockRotatedPillar{
    protected BlockRotatedPillarFs(Material materialIn) {
        super(materialIn);
    }


    protected BlockRotatedPillarFs(Material materialIn, MapColor color) {
        super(materialIn, color);
    }


    @Override
    public void addCollisionBoxToListF(IBlockState state, IBlockAccess worldIn, BlockPos pos, AxisAlignedBB entityBox, AABBList collidingBoxes) {
        addCollisionBoxToListFs(pos, entityBox, collidingBoxes, state.getCollisionBoundingBox(worldIn, pos));
    }
}
