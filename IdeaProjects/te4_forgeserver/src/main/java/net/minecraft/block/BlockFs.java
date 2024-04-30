package net.minecraft.block;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import plus.fserver.utls.collections.ArrayListE6;
import javax.annotation.Nullable;


public class BlockFs extends Block{
    public BlockFs(Material blockMaterialIn, MapColor blockMapColorIn) {
        super(blockMaterialIn, blockMapColorIn);
    }

    public BlockFs(Material materialIn) {
        super(materialIn);
    }


    @Override
    public void addCollisionBoxToListF(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, ArrayListE6<AxisAlignedBB> collidingBoxes) {
        addCollisionBoxToListFs(pos, entityBox, collidingBoxes, state.getCollisionBoundingBox(worldIn, pos));
    }


    public static void addCollisionBoxToListFs(BlockPos pos, AxisAlignedBB entityBox, ArrayListE6<AxisAlignedBB> list, @Nullable AxisAlignedBB blockBox) {
        if (blockBox != NULL_AABB) {
            //Up to 30+ times faster
            AxisAlignedBB src = list.getSafe(list.sizeC());
            if(src != null) blockBox.offsetTo(pos, src);
            else src = blockBox.offset(pos);

            if (entityBox.intersects(src)) list.addF(src);
        }
    }
}
