package net.minecraft.block;

import java.util.Random;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


public class BlockEndRod extends BlockDirectionalFx {
    protected static final AxisAlignedBB END_ROD_VERTICAL_AABB = new AxisAlignedBB(0.375, 0.0, 0.375, 0.625, 1.0, 0.625);
    protected static final AxisAlignedBB END_ROD_NS_AABB = new AxisAlignedBB(0.375, 0.375, 0.0, 0.625, 0.625, 1.0);
    protected static final AxisAlignedBB END_ROD_EW_AABB = new AxisAlignedBB(0.0, 0.375, 0.375, 1.0, 0.625, 0.625);

    protected BlockEndRod() {
        super(Material.CIRCUITS);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.UP));
        this.setCreativeTab(CreativeTabs.DECORATIONS);
    }


    @Override
    public IBlockState withRotation(IBlockState state, Rotation rot) {
        return state.withProperty(FACING, rot.rotate(state.getValue(FACING)));
    }


    @Override
    public IBlockState withMirror(IBlockState state, Mirror mirror) {
        return state.withProperty(FACING, mirror.mirror(state.getValue(FACING)));
    }


    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess access, BlockPos pos) {
        switch (state.getValue(FACING).getAxis()) {
            case X:
            default:
                return END_ROD_EW_AABB;
            case Z:
                return END_ROD_NS_AABB;
            case Y:
                return END_ROD_VERTICAL_AABB;
        }
    }


    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }


    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }


    @Override
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        return true;
    }


    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float p_180642_4_, float p_180642_5_, float p_180642_6_, int p_180642_7_, EntityLivingBase p_180642_8_) {
        IBlockState state = world.access().getBlockState(pos.offset(facing.getOpposite()));
        if (state.getBlock() == Blocks.END_ROD) {
            EnumFacing enumFacing = state.getValue(FACING);
            if (enumFacing == facing) {
                return this.getDefaultState().withProperty(FACING, facing.getOpposite());
            }
        }
        return this.getDefaultState().withProperty(FACING, facing);
    }


    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState stateIn, World world, BlockPos pos, Random rand) {
        EnumFacing facing = stateIn.getValue(FACING);
        double dx = (double)pos.getX() + 0.55 - (double)(rand.nextFloat() * 0.1F);
        double dy = (double)pos.getY() + 0.55 - (double)(rand.nextFloat() * 0.1F);
        double dz = (double)pos.getZ() + 0.55 - (double)(rand.nextFloat() * 0.1F);
        double rNum = 0.4F - (rand.nextFloat() + rand.nextFloat()) * 0.4F;
        if (rand.nextInt(5) == 0) {
            world.spawnParticle(EnumParticleTypes.END_ROD, dx + (double)facing.getXOffset() * rNum, dy + (double)facing.getYOffset() * rNum, dz + (double)facing.getZOffset() * rNum, rand.nextGaussian() * 0.005, rand.nextGaussian() * 0.005, rand.nextGaussian() * 0.005, new int[0]);
        }
    }


    @Override
    public IBlockState getStateFromMeta(int meta) {
        IBlockState state = this.getDefaultState();
        state = state.withProperty(FACING, EnumFacing.byIndex(meta));
        return state;
    }


    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }


    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getIndex();
    }


    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }


    @Override
    public EnumPushReaction getPushReaction(IBlockState state) {
        return EnumPushReaction.NORMAL;
    }


    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess access, IBlockState state, BlockPos pos, EnumFacing facing) {
        return BlockFaceShape.UNDEFINED;
    }
}