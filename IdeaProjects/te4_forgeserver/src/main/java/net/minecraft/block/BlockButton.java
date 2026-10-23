package net.minecraft.block;

import net.minecraft.block.material.*;
import net.minecraft.block.properties.*;
import net.minecraft.creativetab.*;
import javax.annotation.*;
import net.minecraft.world.*;
import net.minecraft.entity.player.*;
import net.minecraft.entity.*;
import net.minecraft.entity.projectile.*;
import net.minecraft.util.math.*;
import java.util.*;
import net.minecraft.util.*;
import net.minecraft.block.state.*;
import plus.fserver.utls.collections.AABBList;


public abstract class BlockButton extends BlockDirectional {
    public static final PropertyBool POWERED;
    protected static final AxisAlignedBB AABB_DOWN_OFF;
    protected static final AxisAlignedBB AABB_UP_OFF;
    protected static final AxisAlignedBB AABB_NORTH_OFF;
    protected static final AxisAlignedBB AABB_SOUTH_OFF;
    protected static final AxisAlignedBB AABB_WEST_OFF;
    protected static final AxisAlignedBB AABB_EAST_OFF;
    protected static final AxisAlignedBB AABB_DOWN_ON;
    protected static final AxisAlignedBB AABB_UP_ON;
    protected static final AxisAlignedBB AABB_NORTH_ON;
    protected static final AxisAlignedBB AABB_SOUTH_ON;
    protected static final AxisAlignedBB AABB_WEST_ON;
    protected static final AxisAlignedBB AABB_EAST_ON;
    private final boolean wooden;

    protected BlockButton(final boolean wooden) {
        super(Material.CIRCUITS);
        this.setDefaultState(this.blockState.getBaseState().withProperty(BlockButton.FACING, EnumFacing.NORTH).withProperty(BlockButton.POWERED, false));
        this.setTickRandomly(true);
        this.setCreativeTab(CreativeTabs.REDSTONE);
        this.wooden = wooden;
    }


    @Nullable
    public AxisAlignedBB getCollisionBoundingBox(final IBlockState blockState, final IBlockAccess worldIn, final BlockPos pos) {
        return BlockButton.NULL_AABB;
    }


    @Override
    public void addCollisionBoxToListF(IBlockState state, IBlockAccess access, BlockPos pos, AxisAlignedBB entityBox, AABBList collidingBoxes) {}


    @Override
    public int tickRate(final World worldIn) {
        return this.wooden ? 30 : 20;
    }


    @Override
    public boolean isOpaqueCube(final IBlockState state) {
        return false;
    }


    @Override
    public boolean isFullCube(final IBlockState state) {
        return false;
    }


    @Override
    public boolean canPlaceBlockOnSide(final World worldIn, final BlockPos pos, final EnumFacing side) {
        return canPlaceBlock(worldIn, pos, side);
    }


    @Override
    public boolean canPlaceBlockAt(final World worldIn, final BlockPos pos) {
        for (final EnumFacing enumfacing : EnumFacing.VALUES) {
            if (canPlaceBlock(worldIn, pos, enumfacing)) {
                return true;
            }
        }
        return false;
    }


    protected static boolean canPlaceBlock(final World worldIn, final BlockPos pos, final EnumFacing direction) {
        final BlockPos blockpos = pos.offset(direction.getOpposite());
        final IBlockState iblockstate = worldIn.getBlockState(blockpos);
        final boolean flag = iblockstate.getBlockFaceShape(worldIn, blockpos, direction) == BlockFaceShape.SOLID;
        final Block block = iblockstate.getBlock();
        if (direction == EnumFacing.UP) {
            return iblockstate.isTopSolid() || (!isExceptionBlockForAttaching(block) && flag);
        }
        return !isExceptBlockForAttachWithPiston(block) && flag;
    }


    @Override
    public IBlockState getStateForPlacement(final World worldIn, final BlockPos pos, final EnumFacing facing, final float hitX, final float hitY, final float hitZ, final int meta, final EntityLivingBase placer) {
        return canPlaceBlock(worldIn, pos, facing) ? this.getDefaultState().withProperty(BlockButton.FACING, facing).withProperty(BlockButton.POWERED, false) : this.getDefaultState().withProperty(BlockButton.FACING, EnumFacing.DOWN).withProperty(BlockButton.POWERED, false);
    }


    @Override
    public void neighborChanged(final IBlockState state, final World worldIn, final BlockPos pos, final Block blockIn, final BlockPos fromPos) {
        if (this.checkForDrop(worldIn, pos, state) && !canPlaceBlock(worldIn, pos, state.getValue(BlockButton.FACING))) {
            this.dropBlockAsItem(worldIn, pos, state, 0);
            worldIn.setBlockToAir(pos);
        }
    }


    private boolean checkForDrop(final World worldIn, final BlockPos pos, final IBlockState state) {
        if (this.canPlaceBlockAt(worldIn, pos)) {
            return true;
        }
        //this.dropBlockAsItem(worldIn, pos, state, 0);
        //worldIn.setBlockToAir(pos);
        return false;
    }


    @Override
    public AxisAlignedBB getBoundingBox(final IBlockState state, final IBlockAccess source, final BlockPos pos) {
        final EnumFacing enumfacing = state.getValue(BlockButton.FACING);
        final boolean flag = state.getValue(BlockButton.POWERED);
        switch (enumfacing) {
            case EAST: {
                return flag ? BlockButton.AABB_EAST_ON : BlockButton.AABB_EAST_OFF;
            }
            case WEST: {
                return flag ? BlockButton.AABB_WEST_ON : BlockButton.AABB_WEST_OFF;
            }
            case SOUTH: {
                return flag ? BlockButton.AABB_SOUTH_ON : BlockButton.AABB_SOUTH_OFF;
            }
            default: {
                return flag ? BlockButton.AABB_NORTH_ON : BlockButton.AABB_NORTH_OFF;
            }
            case UP: {
                return flag ? BlockButton.AABB_UP_ON : BlockButton.AABB_UP_OFF;
            }
            case DOWN: {
                return flag ? BlockButton.AABB_DOWN_ON : BlockButton.AABB_DOWN_OFF;
            }
        }
    }


    @Override
    public boolean onBlockActivated(final World worldIn, final BlockPos pos, final IBlockState state, final EntityPlayer playerIn, final EnumHand hand, final EnumFacing facing, final float hitX, final float hitY, final float hitZ) {
        if (state.getValue(BlockButton.POWERED)) {
            return true;
        }
        worldIn.access().setBlockState(pos, state.withProperty(BlockButton.POWERED, true), 3);
        worldIn.markBlockRangeForRenderUpdate(pos, pos);
        this.playClickSound(playerIn, worldIn, pos);
        this.notifyNeighbors(worldIn, pos, state.getValue(BlockButton.FACING));
        worldIn.scheduleUpdate(pos, this, this.tickRate(worldIn));
        return true;
    }


    protected abstract void playClickSound(@Nullable final EntityPlayer p0, final World p1, final BlockPos p2);


    protected abstract void playReleaseSound(final World p0, final BlockPos p1);


    @Override
    public void breakBlock(final World worldIn, final BlockPos pos, final IBlockState state) {
        if (state.getValue(BlockButton.POWERED)) {
            this.notifyNeighbors(worldIn, pos, state.getValue(BlockButton.FACING));
        }
        super.breakBlock(worldIn, pos, state);
    }


    @Override
    public int getWeakPower(final IBlockState blockState, final IBlockAccess blockAccess, final BlockPos pos, final EnumFacing side) {
        return blockState.getValue(BlockButton.POWERED) ? 15 : 0;
    }


    @Override
    public int getStrongPower(final IBlockState blockState, final IBlockAccess blockAccess, final BlockPos pos, final EnumFacing side) {
        if (!blockState.getValue(BlockButton.POWERED)) {
            return 0;
        }
        return (blockState.getValue(BlockButton.FACING) == side) ? 15 : 0;
    }


    @Override
    public boolean canProvidePower(final IBlockState state) {
        return true;
    }


    @Override
    public void randomTick(final World worldIn, final BlockPos pos, final IBlockState state, final Random random) {}


    @Override
    public void updateTick(final World world, final BlockPos pos, final IBlockState state, final Random rand) {
        if (!world.isRemote && state.getValue(BlockButton.POWERED)) {
            if (this.wooden) {
                this.checkPressed(state, world, pos);
            }
            else {
                world.access().setBlockState(pos, state.withProperty(BlockButton.POWERED, false), 3);
                this.notifyNeighbors(world, pos, state.getValue(BlockButton.FACING));
                this.playReleaseSound(world, pos);
                world.markBlockRangeForRenderUpdate(pos, pos);
            }
        }
    }


    @Override
    public void onEntityCollision(final World worldIn, final BlockPos pos, final IBlockState state, final Entity entityIn) {
        if (!worldIn.isRemote && this.wooden && !state.getValue(BlockButton.POWERED)) {
            this.checkPressed(state, worldIn, pos);
        }
    }


    private void checkPressed(final IBlockState state, final World world, final BlockPos pos) {
        final List<? extends Entity> list = world.getEntitiesWithinAABB((Class<? extends Entity>)EntityArrow.class, state.getBoundingBox(world, pos).offset(pos));
        final boolean flag = !list.isEmpty();
        final boolean flag2 = state.getValue(BlockButton.POWERED);

        if (flag && !flag2) {
            world.access().setBlockState(pos, state.withProperty(BlockButton.POWERED, true), 3);
            this.notifyNeighbors(world, pos, state.getValue(BlockButton.FACING));
            world.markBlockRangeForRenderUpdate(pos, pos);
            this.playClickSound(null, world, pos);
        }
        if (!flag && flag2) {
            world.access().setBlockState(pos, state.withProperty(BlockButton.POWERED, false), 3);
            this.notifyNeighbors(world, pos, state.getValue(BlockButton.FACING));
            world.markBlockRangeForRenderUpdate(pos, pos);
            this.playReleaseSound(world, pos);
        }
        if (flag) {
            world.scheduleUpdate(new BlockPos(pos), this, this.tickRate(world));
        }
    }


    private void notifyNeighbors(final World world, final BlockPos pos, final EnumFacing facing) {
        world.notifyNeighborsOfStateChange(pos, this, false);
        world.notifyNeighborsOfStateChange(pos.offset(facing.getOpposite()), this, false);
    }


    @Override
    public IBlockState getStateFromMeta(final int meta) {
        EnumFacing enumfacing = null;
        switch (meta & 0x7) {
            case 0: {
                enumfacing = EnumFacing.DOWN;
                break;
            }
            case 1: {
                enumfacing = EnumFacing.EAST;
                break;
            }
            case 2: {
                enumfacing = EnumFacing.WEST;
                break;
            }
            case 3: {
                enumfacing = EnumFacing.SOUTH;
                break;
            }
            case 4: {
                enumfacing = EnumFacing.NORTH;
                break;
            }
            default: {
                enumfacing = EnumFacing.UP;
                break;
            }
        }
        return this.getDefaultState().withProperty(BlockButton.FACING, enumfacing).withProperty(BlockButton.POWERED, (meta & 0x8) > 0);
    }


    @Override
    public int getMetaFromState(final IBlockState state) {
        int i = 0;
        switch (state.getValue(BlockButton.FACING)) {
            case EAST: {
                i = 1;
                break;
            }
            case WEST: {
                i = 2;
                break;
            }
            case SOUTH: {
                i = 3;
                break;
            }
            case NORTH: {
                i = 4;
                break;
            }
            default: {
                i = 5;
                break;
            }
            case DOWN: {
                i = 0;
                break;
            }
        }
        if (state.getValue(BlockButton.POWERED)) {
            i |= 0x8;
        }
        return i;
    }


    @Override
    public IBlockState withRotation(final IBlockState state, final Rotation rot) {
        return state.withProperty(BlockButton.FACING, rot.rotate(state.getValue(BlockButton.FACING)));
    }


    @Override
    public IBlockState withMirror(final IBlockState state, final Mirror mirrorIn) {
        return state.withRotation(mirrorIn.toRotation(state.getValue(BlockButton.FACING)));
    }


    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, BlockButton.FACING, BlockButton.POWERED);
    }


    @Override
    public BlockFaceShape getBlockFaceShape(final IBlockAccess worldIn, final IBlockState state, final BlockPos pos, final EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }


    static {
        POWERED = PropertyBool.create("powered");
        AABB_DOWN_OFF = new AxisAlignedBB(0.3125, 0.875, 0.375, 0.6875, 1.0, 0.625);
        AABB_UP_OFF = new AxisAlignedBB(0.3125, 0.0, 0.375, 0.6875, 0.125, 0.625);
        AABB_NORTH_OFF = new AxisAlignedBB(0.3125, 0.375, 0.875, 0.6875, 0.625, 1.0);
        AABB_SOUTH_OFF = new AxisAlignedBB(0.3125, 0.375, 0.0, 0.6875, 0.625, 0.125);
        AABB_WEST_OFF = new AxisAlignedBB(0.875, 0.375, 0.3125, 1.0, 0.625, 0.6875);
        AABB_EAST_OFF = new AxisAlignedBB(0.0, 0.375, 0.3125, 0.125, 0.625, 0.6875);
        AABB_DOWN_ON = new AxisAlignedBB(0.3125, 0.9375, 0.375, 0.6875, 1.0, 0.625);
        AABB_UP_ON = new AxisAlignedBB(0.3125, 0.0, 0.375, 0.6875, 0.0625, 0.625);
        AABB_NORTH_ON = new AxisAlignedBB(0.3125, 0.375, 0.9375, 0.6875, 0.625, 1.0);
        AABB_SOUTH_ON = new AxisAlignedBB(0.3125, 0.375, 0.0, 0.6875, 0.625, 0.0625);
        AABB_WEST_ON = new AxisAlignedBB(0.9375, 0.375, 0.3125, 1.0, 0.625, 0.6875);
        AABB_EAST_ON = new AxisAlignedBB(0.0, 0.375, 0.3125, 0.0625, 0.625, 0.6875);
    }
}