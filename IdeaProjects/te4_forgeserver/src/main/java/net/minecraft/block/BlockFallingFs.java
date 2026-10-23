package net.minecraft.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import plus.fserver.block.BlockFs;
import plus.fserver.utls.collections.AABBList;


public class BlockFallingFs extends BlockFalling {
    public BlockFallingFs(Material material) {
        super(material);
    }

    public BlockFallingFs() {}


    @Override
    public void addCollisionBoxToListF(IBlockState state, IBlockAccess access, BlockPos pos, AxisAlignedBB entityBox, AABBList list) {
        BlockFs.addCollisionBoxToListFs(pos, entityBox, list, getCollisionBoundingBox(state, access, pos));
    }
}