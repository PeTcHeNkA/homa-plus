package net.minecraft.block;

import net.minecraft.block.material.*;
import net.minecraft.block.properties.*;
import net.minecraft.creativetab.*;
import net.minecraft.util.math.*;
import javax.annotation.*;
import net.minecraft.world.*;
import net.minecraft.entity.*;
import java.util.*;
import net.minecraftforge.fml.relauncher.*;
import net.minecraft.util.*;
import net.minecraft.block.state.*;
import plus.fserver.utls.collections.ArrayListE6;


public class BlockTorch extends Block {
    public static final PropertyDirection FACING;
    protected static final AxisAlignedBB STANDING_AABB;
    protected static final AxisAlignedBB TORCH_NORTH_AABB;
    protected static final AxisAlignedBB TORCH_SOUTH_AABB;
    protected static final AxisAlignedBB TORCH_WEST_AABB;
    protected static final AxisAlignedBB TORCH_EAST_AABB;

    protected BlockTorch() {
        super(Material.CIRCUITS);
        this.setDefaultState(this.blockState.getBaseState().withProperty(BlockTorch.FACING, EnumFacing.UP));
        this.setTickRandomly(true);
        this.setCreativeTab(CreativeTabs.DECORATIONS);
    }


    @Override
    public AxisAlignedBB getBoundingBox(final IBlockState state, final IBlockAccess source, final BlockPos pos) {
        switch (state.getValue(BlockTorch.FACING)) {
            case EAST: {
                return BlockTorch.TORCH_EAST_AABB;
            }
            case WEST: {
                return BlockTorch.TORCH_WEST_AABB;
            }
            case SOUTH: {
                return BlockTorch.TORCH_SOUTH_AABB;
            }
            case NORTH: {
                return BlockTorch.TORCH_NORTH_AABB;
            }
            default: {
                return BlockTorch.STANDING_AABB;
            }
        }
    }


    @Nullable
    public AxisAlignedBB getCollisionBoundingBox(final IBlockState blockState, final IBlockAccess worldIn, final BlockPos pos) {
        return BlockTorch.NULL_AABB;
    }


    @Override
    public void addCollisionBoxToListF(IBlockState state, IBlockAccess access, BlockPos pos, AxisAlignedBB entityBox, ArrayListE6<AxisAlignedBB> collidingBoxes) {}


    @Override
    public boolean isOpaqueCube(final IBlockState state) {
        return false;
    }


    @Override
    public boolean isFullCube(final IBlockState state) {
        return false;
    }


    private boolean canPlaceOn(final World worldIn, final BlockPos pos) {
        final IBlockState state = worldIn.access().getBlockState(pos);
        return state.getBlock().canPlaceTorchOnTop(state, worldIn, pos);
    }


    @Override
    public boolean canPlaceBlockAt(final World worldIn, final BlockPos pos) {
        for (final EnumFacing enumfacing : BlockTorch.FACING.getAllowedValues()) {
            if (this.canPlaceAt(worldIn, pos, enumfacing)) {
                return true;
            }
        }
        return false;
    }


    private boolean canPlaceAt(final World shape, final BlockPos pos, final EnumFacing facing) {
        final BlockPos blockpos = pos.offset(facing.getOpposite());
        final IBlockState state = shape.access().getBlockState(blockpos);
        final Block block = state.getBlock();
        final BlockFaceShape blockfaceshape = state.getBlockFaceShape(shape, blockpos, facing);
        return (facing.equals(EnumFacing.UP) && this.canPlaceOn(shape, blockpos)) || (facing != EnumFacing.UP && facing != EnumFacing.DOWN && !isExceptBlockForAttachWithPiston(block) && blockfaceshape == BlockFaceShape.SOLID);
    }


    @Override
    public IBlockState getStateForPlacement(final World worldIn, final BlockPos pos, final EnumFacing facing, final float hitX, final float hitY, final float hitZ, final int meta, final EntityLivingBase placer) {
        if (this.canPlaceAt(worldIn, pos, facing)) {
            return this.getDefaultState().withProperty(BlockTorch.FACING, facing);
        }
        for (final EnumFacing enumfacing : EnumFacing.Plane.HORIZONTAL) {
            if (this.canPlaceAt(worldIn, pos, enumfacing)) {
                return this.getDefaultState().withProperty(BlockTorch.FACING, enumfacing);
            }
        }
        return this.getDefaultState();
    }


    @Override
    public void onBlockAdded(final World worldIn, final BlockPos pos, final IBlockState state) {
        this.checkForDrop(worldIn, pos, state);
    }


    @Override
    public void neighborChanged(final IBlockState state, final World worldIn, final BlockPos pos, final Block blockIn, final BlockPos fromPos) {
        this.onNeighborChangeInternal(worldIn, pos, state);
    }


    protected boolean onNeighborChangeInternal(final World world, final BlockPos pos, final IBlockState state) {
        if (!this.checkForDrop(world, pos, state)) {
            return true;
        }
        final EnumFacing enumfacing = state.getValue(BlockTorch.FACING);
        final EnumFacing.Axis axis = enumfacing.getAxis();
        final EnumFacing enumfacing2 = enumfacing.getOpposite();
        final BlockPos blockpos = pos.offset(enumfacing2);
        boolean flag = false;
        if (axis.isHorizontal() && world.access().getBlockState(blockpos).getBlockFaceShape(world, blockpos, enumfacing) != BlockFaceShape.SOLID) {
            flag = true;
        }
        else if (axis.isVertical() && !this.canPlaceOn(world, blockpos)) {
            flag = true;
        }
        if (flag) {
            this.dropBlockAsItem(world, pos, state, 0);
            world.access().setBlockState(pos, BlockIDIndexes.AIR_STATE, 3);
            return true;
        }
        return false;
    }


    protected boolean checkForDrop(final World world, final BlockPos pos, final IBlockState state) {
        if (state.getBlock() == this && this.canPlaceAt(world, pos, state.getValue(BlockTorch.FACING))) {
            return true;
        }
        if (world.access().getBlockState(pos).getBlock() == this) {
            this.dropBlockAsItem(world, pos, state, 0);
            world.access().setBlockState(pos, BlockIDIndexes.AIR_STATE, 3);
        }
        return false;
    }


    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(final IBlockState state, final World world, final BlockPos pos, final Random rand) {
        final EnumFacing enumfacing = state.getValue(BlockTorch.FACING);
        final double d0 = pos.getX() + 0.5;
        final double d2 = pos.getY() + 0.7;
        final double d3 = pos.getZ() + 0.5;

        if (enumfacing.getAxis().isHorizontal()) {
            final EnumFacing enumfacing2 = enumfacing.getOpposite();
            world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d0 + 0.27 * enumfacing2.getXOffset(), d2 + 0.22, d3 + 0.27 * enumfacing2.getZOffset(), 0.0, 0.0, 0.0, new int[0]);
            world.spawnParticle(EnumParticleTypes.FLAME, d0 + 0.27 * enumfacing2.getXOffset(), d2 + 0.22, d3 + 0.27 * enumfacing2.getZOffset(), 0.0, 0.0, 0.0, new int[0]);
        }
        else {
            world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d0, d2, d3, 0.0, 0.0, 0.0, new int[0]);
            world.spawnParticle(EnumParticleTypes.FLAME, d0, d2, d3, 0.0, 0.0, 0.0, new int[0]);
        }
    }


    @Override
    public IBlockState getStateFromMeta(final int meta) {
        IBlockState state = this.getDefaultState();
        switch (meta) {
            case 1: {
                state = state.withProperty(BlockTorch.FACING, EnumFacing.EAST);
                break;
            }
            case 2: {
                state = state.withProperty(BlockTorch.FACING, EnumFacing.WEST);
                break;
            }
            case 3: {
                state = state.withProperty(BlockTorch.FACING, EnumFacing.SOUTH);
                break;
            }
            case 4: {
                state = state.withProperty(BlockTorch.FACING, EnumFacing.NORTH);
                break;
            }
            default: {
                state = state.withProperty(BlockTorch.FACING, EnumFacing.UP);
                break;
            }
        }
        return state;
    }


    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }


    public int getMetaFromState(final IBlockState state) {
        int i = 0;
        switch (state.getValue(BlockTorch.FACING)) {
            case EAST: {
                i |= 0x1;
                break;
            }
            case WEST: {
                i |= 0x2;
                break;
            }
            case SOUTH: {
                i |= 0x3;
                break;
            }
            case NORTH: {
                i |= 0x4;
                break;
            }
            default: {
                i |= 0x5;
                break;
            }
        }
        return i;
    }


    @Override
    public IBlockState withRotation(final IBlockState state, final Rotation rot) {
        return state.withProperty(BlockTorch.FACING, rot.rotate(state.getValue(BlockTorch.FACING)));
    }


    @Override
    public IBlockState withMirror(final IBlockState state, final Mirror mirrorIn) {
        return state.withRotation(mirrorIn.toRotation(state.getValue(BlockTorch.FACING)));
    }


    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, BlockTorch.FACING);
    }


    @Override
    public BlockFaceShape getBlockFaceShape(final IBlockAccess worldIn, final IBlockState state, final BlockPos pos, final EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }


    static {
        FACING = PropertyDirection.create("facing", facing -> facing != EnumFacing.DOWN);
        STANDING_AABB = new AxisAlignedBB(0.4000000059604645, 0.0, 0.4000000059604645, 0.6000000238418579, 0.6000000238418579, 0.6000000238418579);
        TORCH_NORTH_AABB = new AxisAlignedBB(0.3499999940395355, 0.20000000298023224, 0.699999988079071, 0.6499999761581421, 0.800000011920929, 1.0);
        TORCH_SOUTH_AABB = new AxisAlignedBB(0.3499999940395355, 0.20000000298023224, 0.0, 0.6499999761581421, 0.800000011920929, 0.30000001192092896);
        TORCH_WEST_AABB = new AxisAlignedBB(0.699999988079071, 0.20000000298023224, 0.3499999940395355, 1.0, 0.800000011920929, 0.6499999761581421);
        TORCH_EAST_AABB = new AxisAlignedBB(0.0, 0.20000000298023224, 0.3499999940395355, 0.30000001192092896, 0.800000011920929, 0.6499999761581421);
    }
}