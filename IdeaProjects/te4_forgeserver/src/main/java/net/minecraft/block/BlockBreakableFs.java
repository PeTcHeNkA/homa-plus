package net.minecraft.block;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import plus.fserver.utls.collections.AABBList;
import static plus.fserver.block.BlockFs.addCollisionBoxToListFs;


public class BlockBreakableFs extends BlockBreakable {
    protected BlockBreakableFs(Material materialIn, boolean ignoreSimilarityIn) {
        super(materialIn, ignoreSimilarityIn);
    }

    protected BlockBreakableFs(Material materialIn, boolean ignoreSimilarityIn, MapColor mapColorIn) {
        super(materialIn, ignoreSimilarityIn, mapColorIn);
    }


    @Override
    public void addCollisionBoxToListF(IBlockState state, IBlockAccess access, BlockPos pos, AxisAlignedBB entityBox, AABBList collidingBoxes) {
        addCollisionBoxToListFs(pos, entityBox, collidingBoxes, state.getCollisionBoundingBox(access, pos));
    }
}