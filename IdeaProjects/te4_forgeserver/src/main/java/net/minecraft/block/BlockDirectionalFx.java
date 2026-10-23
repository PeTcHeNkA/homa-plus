package net.minecraft.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import plus.fserver.ElcaCV2;
import plus.fserver.utls.collections.AABBList;


public class BlockDirectionalFx extends BlockDirectional{
    protected BlockDirectionalFx(Material material) {
        super(material);
    }


    @Override
    public void addCollisionBoxToListF(IBlockState state, IBlockAccess access, BlockPos pos, AxisAlignedBB entityBox, AABBList list) {
        if(ElcaCV2.LIBBUILD) {
            throw new RuntimeException("THIS CLASS ONLY FOR COMPILE. NOT LOAD THIS");
        }//Up to 40+ times faster
        list.addIfIntersects(entityBox, getCollisionBoundingBox(state, access, pos), pos);
    }
}
