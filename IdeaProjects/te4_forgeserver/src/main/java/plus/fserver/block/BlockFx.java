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


public class BlockFx extends Block {
    public BlockFx(Material blockMaterialIn, MapColor blockMapColorIn) {
        super(blockMaterialIn, blockMapColorIn);
    }


    public BlockFx(Material materialIn) {
        super(materialIn);
    }


    @Override
    public void addCollisionBoxToListF(IBlockState state, IBlockAccess access, BlockPos pos, AxisAlignedBB entityBox, ArrayListE6<AxisAlignedBB> list) {
        if(ElcaCV2.LIBBUILD) {
            throw new RuntimeException("THIS CLASS ONLY FOR COMPILE. NOT LOAD THIS");
        }//Up to 40+ times faster
        AxisAlignedBB src;
        AxisAlignedBB blockBox = getCollisionBoundingBox(state, access, pos);
        if((src = list.getSafe(list.sizeC())) != null) blockBox.offsetTo(pos, src);
        else src = blockBox.offset(pos);
        if (entityBox.intersects(src)) list.addF(src);
    }


    public static void addCollisionBoxToListFx(IBlockState state, IBlockAccess access, BlockPos pos, AxisAlignedBB entityBox, ArrayListE6<AxisAlignedBB> list) {
        if(ElcaCV2.LIBBUILD) {
            throw new RuntimeException("THIS CLASS ONLY FOR COMPILE. NOT LOAD THIS");
        }
        //Up to 37+ times faster
        AxisAlignedBB src;
        AxisAlignedBB blockBox = state.getCollisionBoundingBox(access, pos);
        if((src = list.getSafe(list.sizeC())) != null) blockBox.offsetTo(pos, src);
        else src = blockBox.offset(pos);
        if (entityBox.intersects(src)) list.addF(src);
    }


    static {
        if(ElcaCV2.LIBBUILD) {
            throw new RuntimeException("THIS CLASS ONLY FOR COMPILE. NOT LOAD THIS");
        }
    }
}