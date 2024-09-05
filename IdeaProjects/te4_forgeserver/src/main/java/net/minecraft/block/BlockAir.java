//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package net.minecraft.block;

import javax.annotation.Nullable;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import plus.fserver.utls.collections.AABBList;
import java.util.List;


public class BlockAir extends Block {
    protected BlockAir() {
        super(Material.AIR);
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.INVISIBLE;
    }


    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess access, BlockPos pos) {
        return NULL_AABB;
    }


    @Override
    public void addCollisionBoxToListF(IBlockState state, IBlockAccess access, BlockPos pos, AxisAlignedBB entityBox, AABBList collidingBoxes) {}


    @Override
    public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @org.jetbrains.annotations.Nullable Entity entityIn, boolean isActualState) {}


    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }


    @Override
    public boolean canCollideCheck(IBlockState state, boolean hitIfLiquid) {
        return false;
    }


    @Override
    public void dropBlockAsItemWithChance(World world, BlockPos pos, IBlockState state, float chance, int fortune) {}


    @Override
    public boolean isReplaceable(IBlockAccess access, BlockPos pos) {
        return true;
    }


    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }


    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess access, IBlockState state, BlockPos pos, EnumFacing facing) {
        return BlockFaceShape.UNDEFINED;
    }
}