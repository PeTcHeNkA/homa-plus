package net.minecraft.block;

import net.minecraft.block.material.*;
import net.minecraft.block.properties.*;
import net.minecraft.creativetab.*;
import net.minecraft.block.state.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import java.util.*;
import net.minecraft.world.*;
import net.minecraft.entity.*;


public class BlockObserver extends BlockDirectionalFx {
    public static final PropertyBool POWERED;

    public BlockObserver() {
        super(Material.ROCK);
        this.setDefaultState(this.blockState.getBaseState().withProperty(BlockObserver.FACING, EnumFacing.SOUTH).withProperty(BlockObserver.POWERED, false));
        this.setCreativeTab(CreativeTabs.REDSTONE);
    }


    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, BlockObserver.FACING, BlockObserver.POWERED);
    }


    @Override
    public IBlockState withRotation(final IBlockState state, final Rotation rot) {
        return state.withProperty(BlockObserver.FACING, rot.rotate(state.getValue(BlockObserver.FACING)));
    }


    @Override
    public IBlockState withMirror(final IBlockState state, final Mirror mirrorIn) {
        return state.withRotation(mirrorIn.toRotation(state.getValue(BlockObserver.FACING)));
    }


    @Override
    public void updateTick(final World world, final BlockPos pos, final IBlockState state, final Random rand) {
        if (state.getValue(BlockObserver.POWERED)) {
            world.access().setBlockState(pos, state.withProperty(BlockObserver.POWERED, false), 2);
        } else {
            world.access().setBlockState(pos, state.withProperty(BlockObserver.POWERED, true), 2);
            world.scheduleUpdate(pos, this, 2);
        }
        this.updateNeighborsInFront(world, pos, state);
    }


    @Override
    public void neighborChanged(final IBlockState state, final World worldIn, final BlockPos pos, final Block blockIn, final BlockPos fromPos) {}


    public void observedNeighborChanged(final IBlockState state, final World worldIn, final BlockPos pos, final Block blockIn, final BlockPos fromPos) {
        if (!worldIn.isRemote && pos.offset(state.getValue(BlockObserver.FACING)).equals(fromPos)) {
            this.startSignal(state, worldIn, pos);
        }
    }


    private void startSignal(final IBlockState state, final World world, final BlockPos pos) {
        if (!state.getValue(BlockObserver.POWERED) && !world.isUpdateScheduled(pos, this)) {
            world.scheduleUpdate(pos, this, 2);
        }
    }


    protected void updateNeighborsInFront(final World world, final BlockPos pos, final IBlockState state) {
        final EnumFacing facing = state.getValue(BlockObserver.FACING);
        final BlockPos blockpos = pos.offset(facing.getOpposite());
        world.neighborChanged(blockpos, this, pos);
        world.notifyNeighborsOfStateExcept(blockpos, this, facing);
    }


    @Override
    public boolean canProvidePower(final IBlockState state) {
        return true;
    }


    @Override
    public int getStrongPower(final IBlockState state, final IBlockAccess access, final BlockPos pos, final EnumFacing side) {
        return state.getWeakPower(access, pos, side);
    }


    @Override
    public int getWeakPower(final IBlockState state, final IBlockAccess access, final BlockPos pos, final EnumFacing side) {
        return (state.getValue(BlockObserver.POWERED) && state.getValue(BlockObserver.FACING) == side) ? 15 : 0;
    }


    @Override
    public void onBlockAdded(final World worldIn, final BlockPos pos, final IBlockState state) {
        if (!worldIn.isRemote) {
            if (state.getValue(BlockObserver.POWERED)) {
                this.updateTick(worldIn, pos, state, worldIn.rand);
            }
            this.startSignal(state, worldIn, pos);
        }
    }


    @Override
    public void breakBlock(final World world, final BlockPos pos, final IBlockState state) {
        if (state.getValue(BlockObserver.POWERED) && world.isUpdateScheduled(pos, this)) {
            this.updateNeighborsInFront(world, pos, state.withProperty(BlockObserver.POWERED, false));
        }
    }


    @Override
    public IBlockState getStateForPlacement(final World worldIn, final BlockPos pos, final EnumFacing facing, final float hitX, final float hitY, final float hitZ, final int meta, final EntityLivingBase placer) {
        return this.getDefaultState().withProperty(BlockObserver.FACING, EnumFacing.getDirectionFromEntityLiving(pos, placer).getOpposite());
    }


    @Override
    public int getMetaFromState(final IBlockState state) {
        int i = 0;
        i |= state.getValue(BlockObserver.FACING).getIndex();
        if (state.getValue(BlockObserver.POWERED)) {
            i |= 0x8;
        }
        return i;
    }


    @Override
    public IBlockState getStateFromMeta(final int meta) {
        return this.getDefaultState().withProperty(BlockObserver.FACING, EnumFacing.byIndex(meta & 0x7));
    }


    @Override
    public void observedNeighborChange(final IBlockState state, final World world, final BlockPos pos, final Block block, final BlockPos pos2) {
        this.observedNeighborChanged(state, world, pos, block, pos2);
    }


    static {
        POWERED = PropertyBool.create("powered");
    }
}