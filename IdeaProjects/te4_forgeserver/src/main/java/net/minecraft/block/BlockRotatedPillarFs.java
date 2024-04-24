package net.minecraft.block;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import plus.fserver.utls.collections.ArrayListE6;
import static net.minecraft.block.BlockFs.addCollisionBoxToListFs;

public class BlockRotatedPillarFs extends BlockRotatedPillar{
    protected BlockRotatedPillarFs(Material materialIn) {
        super(materialIn);
    }


    protected BlockRotatedPillarFs(Material materialIn, MapColor color) {
        super(materialIn, color);
    }


    @Override
    public void addCollisionBoxToListF(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, ArrayListE6<AxisAlignedBB> collidingBoxes) {
        addCollisionBoxToListFs(pos, entityBox, collidingBoxes, state.getCollisionBoundingBox(worldIn, pos));
    }
}
