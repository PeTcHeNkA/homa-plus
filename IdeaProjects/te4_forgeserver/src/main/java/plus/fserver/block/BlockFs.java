package plus.fserver.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import plus.fserver.ElcaCV2;
import plus.fserver.utls.collections.ArrayListE6;
import javax.annotation.Nullable;


public class BlockFs extends Block {
    public BlockFs(Material blockMaterialIn, MapColor blockMapColorIn) {
        super(blockMaterialIn, blockMapColorIn);
    }

    public BlockFs(Material materialIn) {
        super(materialIn);
    }


    @Override
    public void addCollisionBoxToListF(IBlockState state, IBlockAccess access, BlockPos pos, AxisAlignedBB entityBox, ArrayListE6<AxisAlignedBB> list) {
        addCollisionBoxToListFs(pos, entityBox, list, state.getCollisionBoundingBox(access, pos));
    }


    public static void addCollisionBoxToListFs(BlockPos pos, AxisAlignedBB entityBox, ArrayListE6<AxisAlignedBB> list, @Nullable AxisAlignedBB blockBox) {
        if(ElcaCV2.LIBBUILD) {
            throw new RuntimeException("THIS CLASS ONLY FOR COMPILE. NOT LOAD THIS");
        }
        if (blockBox != NULL_AABB) {
            //Up to 30+ times faster
            AxisAlignedBB src = list.getSafe(list.sizeC());
            if(src != null) blockBox.offsetTo(pos, src);
            else src = blockBox.offset(pos);

            if (entityBox.intersects(src)) list.addF(src);
        }
    }


    public static void addCollisionBoxToListFx(BlockPos pos, AxisAlignedBB entityBox, ArrayListE6<AxisAlignedBB> list, @Nullable AxisAlignedBB blockBox) {
        if (ElcaCV2.LIBBUILD) {
            throw new RuntimeException("THIS CLASS ONLY FOR COMPILE. NOT LOAD THIS");
        }
        AxisAlignedBB src = list.getSafe(list.sizeC());
        if (src != null) blockBox.offsetTo(pos, src);
        else src = blockBox.offset(pos);
        if (entityBox.intersects(src)) list.addF(src);
    }


    static {
        if(ElcaCV2.LIBBUILD) {
            throw new RuntimeException("THIS CLASS ONLY FOR COMPILE. NOT LOAD THIS");
        }
    }
}