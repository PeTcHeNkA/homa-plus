//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package plus.fserver.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import plus.fserver.utls.collections.ArrayListE6;

public class BlockFx extends Block {
    public BlockFx(Material blockMaterialIn, MapColor blockMapColorIn) {
        super(blockMaterialIn, blockMapColorIn);
    }

    public BlockFx(Material materialIn) {
        super(materialIn);
    }

    public void addCollisionBoxToListF(IBlockState state, IBlockAccess access, BlockPos pos, AxisAlignedBB entityBox, ArrayListE6<AxisAlignedBB> list) {
        throw new RuntimeException("THIS CLASS ONLY FOR COMPILE. NOT LOAD THIS");
    }


    public static void addCollisionBoxToListFx(IBlockState state, IBlockAccess access, BlockPos pos, AxisAlignedBB entityBox, ArrayListE6<AxisAlignedBB> list) {
        throw new RuntimeException("THIS CLASS ONLY FOR COMPILE. NOT LOAD THIS");
    }
}