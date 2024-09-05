package plus.fserver.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import plus.fserver.ElcaCV2;
import plus.fserver.utls.collections.AABBList;
import javax.annotation.Nullable;


public class BlockFs extends Block {
    public BlockFs(Material blockMaterialIn, MapColor blockMapColorIn) {
        super(blockMaterialIn, blockMapColorIn);
    }

    public BlockFs(Material materialIn) {
        super(materialIn);
    }


    @Override
    public void addCollisionBoxToListF(IBlockState state, IBlockAccess access, BlockPos pos, AxisAlignedBB entityBox, AABBList list) {
        addCollisionBoxToListFs(pos, entityBox, list, state.getCollisionBoundingBox(access, pos));
    }


    public static void addCollisionBoxToListFs(BlockPos pos, AxisAlignedBB entityBox, AABBList list, @Nullable AxisAlignedBB blockBox) {
        if(ElcaCV2.LIBBUILD) {
            throw new RuntimeException("THIS CLASS ONLY FOR COMPILE. NOT LOAD THIS");
        }
        if (blockBox != NULL_AABB) {
            //Up to 30+ times faster
            list.addIfIntersects(entityBox, blockBox, pos);
        }
    }


    public static void addCollisionBoxToListFx(BlockPos pos, AxisAlignedBB entityBox, AABBList list, @Nullable AxisAlignedBB blockBox) {
        if (ElcaCV2.LIBBUILD) {
            throw new RuntimeException("THIS CLASS ONLY FOR COMPILE. NOT LOAD THIS");
        }
        list.addIfIntersects(entityBox, blockBox, pos);
    }


    static {
        if(ElcaCV2.LIBBUILD) {
            throw new RuntimeException("THIS CLASS ONLY FOR COMPILE. NOT LOAD THIS");
        }
    }
}