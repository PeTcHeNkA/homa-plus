package plus.fserver.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import plus.fserver.ElcaCV2;
import plus.fserver.utls.collections.AABBList;


public class BlockFx extends Block {
    public BlockFx(Material blockMaterialIn, MapColor blockMapColorIn) {
        super(blockMaterialIn, blockMapColorIn);
    }


    public BlockFx(Material materialIn) {
        super(materialIn);
    }


    @Override
    public void addCollisionBoxToListF(IBlockState state, IBlockAccess access, BlockPos pos, AxisAlignedBB entityBox, AABBList list) {
        if(ElcaCV2.LIBBUILD) {
            throw new RuntimeException("THIS CLASS ONLY FOR COMPILE. NOT LOAD THIS");
        }//Up to 40+ times faster
        list.addIfIntersects(entityBox, getCollisionBoundingBox(state, access, pos), pos);
    }


    @Deprecated
    public boolean hasCollisionRayTrace(IBlockState blockState, IBlockAccess access, BlockPos pos, Vec3d start, Vec3d end) {
        return blockState.getBoundingBox(access, pos).checkInterceptRT(
                start.subtract(pos.getX(), pos.getY(), pos.getZ()),
                end  .subtract(pos.getX(), pos.getY(), pos.getZ())
        );
    }


    public static void addCollisionBoxToListFx(IBlockState state, IBlockAccess access, BlockPos pos, AxisAlignedBB entityBox, AABBList list) {
        if(ElcaCV2.LIBBUILD) {
            throw new RuntimeException("THIS CLASS ONLY FOR COMPILE. NOT LOAD THIS");
        }
        //Up to 37+ times faster
        list.addIfIntersects(entityBox, state.getCollisionBoundingBox(access, pos), pos);
    }
}