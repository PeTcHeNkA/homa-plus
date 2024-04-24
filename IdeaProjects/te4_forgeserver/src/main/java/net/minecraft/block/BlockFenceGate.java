package net.minecraft.block;

import javax.annotation.Nullable;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.fserver.utls.collections.ArrayListE6;
import static net.minecraft.block.BlockFs.addCollisionBoxToListFs;


public class BlockFenceGate extends BlockHorizontal {
    public static final PropertyBool OPEN = PropertyBool.create("open");
    public static final PropertyBool POWERED = PropertyBool.create("powered");
    public static final PropertyBool IN_WALL = PropertyBool.create("in_wall");
    protected static final AxisAlignedBB AABB_HITBOX_ZAXIS = new AxisAlignedBB(0.0D, 0.0D, 0.375D, 1.0D, 1.0D, 0.625D);
    protected static final AxisAlignedBB AABB_HITBOX_XAXIS = new AxisAlignedBB(0.375D, 0.0D, 0.0D, 0.625D, 1.0D, 1.0D);
    protected static final AxisAlignedBB AABB_HITBOX_ZAXIS_INWALL = new AxisAlignedBB(0.0D, 0.0D, 0.375D, 1.0D, 0.8125D, 0.625D);
    protected static final AxisAlignedBB AABB_HITBOX_XAXIS_INWALL = new AxisAlignedBB(0.375D, 0.0D, 0.0D, 0.625D, 0.8125D, 1.0D);
    protected static final AxisAlignedBB AABB_COLLISION_BOX_ZAXIS = new AxisAlignedBB(0.0D, 0.0D, 0.375D, 1.0D, 1.5D, 0.625D);
    protected static final AxisAlignedBB AABB_COLLISION_BOX_XAXIS = new AxisAlignedBB(0.375D, 0.0D, 0.0D, 0.625D, 1.5D, 1.0D);

    public BlockFenceGate(BlockPlanks.EnumType p_i46394_1_) {
        super(Material.WOOD, p_i46394_1_.getMapColor());
        this.setDefaultState(this.blockState.getBaseState()
                .withProperty(OPEN, Boolean.FALSE)
                .withProperty(POWERED, Boolean.FALSE)
                .withProperty(IN_WALL, Boolean.FALSE));
        this.setCreativeTab(CreativeTabs.REDSTONE);
    }


    @Override
    public void addCollisionBoxToListF(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, ArrayListE6<AxisAlignedBB> collidingBoxes) {
        addCollisionBoxToListFs(pos, entityBox, collidingBoxes, state.getCollisionBoundingBox(worldIn, pos));
    }


    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        state = this.getActualState(state, source, pos);

        if (state.getValue(IN_WALL).booleanValue()) {
            return state.getValue(FACING).getAxis() == EnumFacing.Axis.X ? AABB_HITBOX_XAXIS_INWALL : AABB_HITBOX_ZAXIS_INWALL;
        } else {
            return state.getValue(FACING).getAxis() == EnumFacing.Axis.X ? AABB_HITBOX_XAXIS : AABB_HITBOX_ZAXIS;
        }
    }


    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        EnumFacing.Axis enumfacing$axis = state.getValue(FACING).getAxis();

        if (enumfacing$axis == EnumFacing.Axis.Z && (worldIn.getBlockState(pos.west()).getBlock() instanceof BlockWall
                || worldIn.getBlockState(pos.east()).getBlock() instanceof BlockWall)
                || enumfacing$axis == EnumFacing.Axis.X && (worldIn.getBlockState(pos.north()).getBlock() instanceof BlockWall
                || worldIn.getBlockState(pos.south()).getBlock() instanceof BlockWall)) {
            state = state.withProperty(IN_WALL, Boolean.valueOf(true));
        }
        return state;
    }


    @Override
    public IBlockState withRotation(IBlockState state, Rotation rot) {
        return state.withProperty(FACING, rot.rotate((EnumFacing)state.getValue(FACING)));
    }


    @Override
    public IBlockState withMirror(IBlockState state, Mirror mirrorIn) {
        return state.withRotation(mirrorIn.toRotation(state.getValue(FACING)));
    }


    @Override
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        return worldIn.getBlockState(pos.down()).getMaterial().isSolid() && super.canPlaceBlockAt(worldIn, pos);
    }


    @Override
    @Nullable
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        if (blockState.getValue(OPEN).booleanValue()) {
            return NULL_AABB;
        } else {
            return blockState.getValue(FACING).getAxis() == EnumFacing.Axis.Z ? AABB_COLLISION_BOX_ZAXIS : AABB_COLLISION_BOX_XAXIS;
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
    public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
        return worldIn.getBlockState(pos).getValue(OPEN).booleanValue();
    }


    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        boolean flag = worldIn.isBlockPowered(pos);
        return this.getDefaultState()
                .withProperty(FACING, placer.getHorizontalFacing())
                .withProperty(OPEN, flag)
                .withProperty(POWERED, flag)
                .withProperty(IN_WALL, Boolean.FALSE);
    }


    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (state.getValue(OPEN).booleanValue()) {
            state = state.withProperty(OPEN, Boolean.valueOf(false));
            worldIn.setBlockState(pos, state, 10);
        } else {
            EnumFacing enumfacing = EnumFacing.fromAngle((double)playerIn.rotationYaw);

            if (state.getValue(FACING) == enumfacing.getOpposite())
            {
                state = state.withProperty(FACING, enumfacing);
            }

            state = state.withProperty(OPEN, Boolean.valueOf(true));
            worldIn.setBlockState(pos, state, 10);
        }
        worldIn.playEvent(playerIn, state.getValue(OPEN).booleanValue() ? 1008 : 1014, pos, 0);
        return true;
    }


    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        if (!worldIn.isRemote) {
            boolean flag = worldIn.isBlockPowered(pos);

            if (state.getValue(POWERED) != flag) {
                worldIn.setBlockState(pos, state.withProperty(POWERED, flag).withProperty(OPEN, flag), 2);

                if (state.getValue(OPEN) != flag) {
                    worldIn.playEvent(null, flag ? 1008 : 1014, pos, 0);
                }
            }
        }
    }


    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        return true;
    }


    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState()
                .withProperty(FACING, EnumFacing.byHorizontalIndex(meta))
                .withProperty(OPEN, (meta & 4) != 0)
                .withProperty(POWERED, (meta & 8) != 0);
    }


    @Override
    public int getMetaFromState(IBlockState state) {
        int i = 0;
        i = i | state.getValue(FACING).getHorizontalIndex();
        if (state.getValue(POWERED)) {
            i |= 8;
        }
        if (state.getValue(OPEN)) {
            i |= 4;
        }
        return i;
    }


    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING, OPEN, POWERED, IN_WALL);
    }

    /* ======================================== FORGE START ======================================== */

    @Override
    public boolean canBeConnectedTo(IBlockAccess world, BlockPos pos, EnumFacing facing) {
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof BlockFenceGate &&
                state.getBlockFaceShape(world, pos, facing) == BlockFaceShape.MIDDLE_POLE) {
            Block connector = world.getBlockState(pos.offset(facing)).getBlock();
            return connector instanceof BlockFence || connector instanceof BlockWall;
        }
        return false;
    }

    /* ======================================== FORGE END ======================================== */

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        if (face != EnumFacing.UP && face != EnumFacing.DOWN) {
            return state.getValue(FACING).getAxis() == face.rotateY().getAxis() ? BlockFaceShape.MIDDLE_POLE : BlockFaceShape.UNDEFINED;
        } else {
            return BlockFaceShape.UNDEFINED;
        }
    }
}
