package net.minecraft.block;

import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.fserver.api.Keep;
import plus.fserver.utls.collections.ArrayListE6;
import plus.fserver.utls.collections.ArrayListEV;
import static plus.fserver.block.BlockFs.addCollisionBoxToListFx;


public class BlockStairs extends Block {
    @Keep
    public static final PropertyDirection FACING = BlockHorizontal.FACING;
    @Keep
    public static final PropertyEnum<BlockStairs.EnumHalf> HALF = PropertyEnum.create("half", BlockStairs.EnumHalf.class);
    @Keep
    public static final PropertyEnum<BlockStairs.EnumShape> SHAPE = PropertyEnum.create("shape", BlockStairs.EnumShape.class);
    @Keep
    protected static final AxisAlignedBB AABB_SLAB_TOP = new AxisAlignedBB(0.0D, 0.5D, 0.0D, 1.0D, 1.0D, 1.0D);
    @Keep
    protected static final AxisAlignedBB AABB_QTR_TOP_WEST = new AxisAlignedBB(0.0D, 0.5D, 0.0D, 0.5D, 1.0D, 1.0D);
    @Keep
    protected static final AxisAlignedBB AABB_QTR_TOP_EAST = new AxisAlignedBB(0.5D, 0.5D, 0.0D, 1.0D, 1.0D, 1.0D);
    @Keep
    protected static final AxisAlignedBB AABB_QTR_TOP_NORTH = new AxisAlignedBB(0.0D, 0.5D, 0.0D, 1.0D, 1.0D, 0.5D);
    @Keep
    protected static final AxisAlignedBB AABB_QTR_TOP_SOUTH = new AxisAlignedBB(0.0D, 0.5D, 0.5D, 1.0D, 1.0D, 1.0D);
    @Keep
    protected static final AxisAlignedBB AABB_OCT_TOP_NW = new AxisAlignedBB(0.0D, 0.5D, 0.0D, 0.5D, 1.0D, 0.5D);
    @Keep
    protected static final AxisAlignedBB AABB_OCT_TOP_NE = new AxisAlignedBB(0.5D, 0.5D, 0.0D, 1.0D, 1.0D, 0.5D);
    @Keep
    protected static final AxisAlignedBB AABB_OCT_TOP_SW = new AxisAlignedBB(0.0D, 0.5D, 0.5D, 0.5D, 1.0D, 1.0D);
    @Keep
    protected static final AxisAlignedBB AABB_OCT_TOP_SE = new AxisAlignedBB(0.5D, 0.5D, 0.5D, 1.0D, 1.0D, 1.0D);
    @Keep
    protected static final AxisAlignedBB AABB_SLAB_BOTTOM = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.5D, 1.0D);
    @Keep
    protected static final AxisAlignedBB AABB_QTR_BOT_WEST = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.5D, 0.5D, 1.0D);
    @Keep
    protected static final AxisAlignedBB AABB_QTR_BOT_EAST = new AxisAlignedBB(0.5D, 0.0D, 0.0D, 1.0D, 0.5D, 1.0D);
    @Keep
    protected static final AxisAlignedBB AABB_QTR_BOT_NORTH = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.5D, 0.5D);
    @Keep
    protected static final AxisAlignedBB AABB_QTR_BOT_SOUTH = new AxisAlignedBB(0.0D, 0.0D, 0.5D, 1.0D, 0.5D, 1.0D);
    @Keep
    protected static final AxisAlignedBB AABB_OCT_BOT_NW = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.5D, 0.5D, 0.5D);
    @Keep
    protected static final AxisAlignedBB AABB_OCT_BOT_NE = new AxisAlignedBB(0.5D, 0.0D, 0.0D, 1.0D, 0.5D, 0.5D);
    @Keep
    protected static final AxisAlignedBB AABB_OCT_BOT_SW = new AxisAlignedBB(0.0D, 0.0D, 0.5D, 0.5D, 0.5D, 1.0D);
    @Keep
    protected static final AxisAlignedBB AABB_OCT_BOT_SE = new AxisAlignedBB(0.5D, 0.0D, 0.5D, 1.0D, 0.5D, 1.0D);
    private final Block modelBlock;
    private final IBlockState modelState;

    protected BlockStairs(IBlockState modelState) {
        super(modelState.getBlock().material);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(HALF, BlockStairs.EnumHalf.BOTTOM).withProperty(SHAPE, BlockStairs.EnumShape.STRAIGHT));
        this.modelBlock = modelState.getBlock();
        this.modelState = modelState;
        this.setHardness(this.modelBlock.blockHardness);
        this.setResistance(this.modelBlock.blockResistance / 3.0F);
        this.setSoundType(this.modelBlock.blockSoundType);
        this.setLightOpacity(255);
        this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
    }


    @Override
    public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState) {
        if (!isActualState) {
            state = this.getActualState(state, worldIn, pos);
        }

        for (AxisAlignedBB aabb : getCollisionBoxList(state)) {
            addCollisionBoxToList(pos, entityBox, collidingBoxes, aabb);
        }
    }


    @Override
    public void addCollisionBoxToListF(IBlockState bstate, IBlockAccess access, BlockPos pos, AxisAlignedBB entityBox, ArrayListE6<AxisAlignedBB> collidingBoxes) {
        bstate = this.getActualState(bstate, access, pos);

        addCollisionBoxToListFx(pos, entityBox, collidingBoxes, bstate.getValue(HALF) == BlockStairs.EnumHalf.TOP ? AABB_SLAB_TOP : AABB_SLAB_BOTTOM);
        BlockStairs.EnumShape shape = bstate.getValue(SHAPE);

        if (shape == BlockStairs.EnumShape.STRAIGHT || shape == BlockStairs.EnumShape.INNER_LEFT || shape == BlockStairs.EnumShape.INNER_RIGHT)
            addCollisionBoxToListFx(pos, entityBox, collidingBoxes, getCollQuarterBlock(bstate));

        if (shape != BlockStairs.EnumShape.STRAIGHT)
            addCollisionBoxToListFx(pos, entityBox, collidingBoxes, getCollEighthBlock(bstate));
    }


    private static List<AxisAlignedBB> getCollisionBoxList(IBlockState bstate) {
        ArrayListEV<AxisAlignedBB> list = new ArrayListEV<>(3);

        list.addF(bstate.getValue(HALF) == BlockStairs.EnumHalf.TOP ? AABB_SLAB_TOP : AABB_SLAB_BOTTOM);
        BlockStairs.EnumShape shape = bstate.getValue(SHAPE);

        if (shape == BlockStairs.EnumShape.STRAIGHT || shape == BlockStairs.EnumShape.INNER_LEFT || shape == BlockStairs.EnumShape.INNER_RIGHT) {
            list.addF(getCollQuarterBlock(bstate));
        }

        if (shape != BlockStairs.EnumShape.STRAIGHT) {
            list.addF(getCollEighthBlock(bstate));
        }
        return list;
    }


    private static AxisAlignedBB getCollQuarterBlock(IBlockState bstate) {
        boolean flag = bstate.getValue(HALF) == BlockStairs.EnumHalf.TOP;

        switch (bstate.getValue(FACING)) {
            case NORTH:
            default:
                return flag ? AABB_QTR_BOT_NORTH : AABB_QTR_TOP_NORTH;
            case SOUTH:
                return flag ? AABB_QTR_BOT_SOUTH : AABB_QTR_TOP_SOUTH;
            case WEST:
                return flag ? AABB_QTR_BOT_WEST : AABB_QTR_TOP_WEST;
            case EAST:
                return flag ? AABB_QTR_BOT_EAST : AABB_QTR_TOP_EAST;
        }
    }


    private static AxisAlignedBB getCollEighthBlock(IBlockState bstate) {
        EnumFacing enumfacing = bstate.getValue(FACING);
        EnumFacing enumfacing1;

        switch (bstate.getValue(SHAPE)) {
            case OUTER_LEFT:
            default:
                enumfacing1 = enumfacing;
                break;
            case OUTER_RIGHT:
                enumfacing1 = enumfacing.rotateY();
                break;
            case INNER_RIGHT:
                enumfacing1 = enumfacing.getOpposite();
                break;
            case INNER_LEFT:
                enumfacing1 = enumfacing.rotateYCCW();
        }

        boolean flag = bstate.getValue(HALF) == BlockStairs.EnumHalf.TOP;

        switch (enumfacing1) {
            case NORTH:
            default:
                return flag ? AABB_OCT_BOT_NW : AABB_OCT_TOP_NW;
            case SOUTH:
                return flag ? AABB_OCT_BOT_SE : AABB_OCT_TOP_SE;
            case WEST:
                return flag ? AABB_OCT_BOT_SW : AABB_OCT_TOP_SW;
            case EAST:
                return flag ? AABB_OCT_BOT_NE : AABB_OCT_TOP_NE;
        }
    }


    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        state = this.getActualState(state, worldIn, pos);

        if (face.getAxis() == EnumFacing.Axis.Y) {
            return face == EnumFacing.UP == (state.getValue(HALF) == BlockStairs.EnumHalf.TOP) ? BlockFaceShape.SOLID : BlockFaceShape.UNDEFINED;
        } else {
            BlockStairs.EnumShape shape = state.getValue(SHAPE);

            if (shape != BlockStairs.EnumShape.OUTER_LEFT && shape != BlockStairs.EnumShape.OUTER_RIGHT) {
                EnumFacing facing = state.getValue(FACING);

                switch (shape) {
                    case INNER_RIGHT:
                        return facing != face && facing != face.rotateYCCW() ? BlockFaceShape.UNDEFINED : BlockFaceShape.SOLID;
                    case INNER_LEFT:
                        return facing != face && facing != face.rotateY() ? BlockFaceShape.UNDEFINED : BlockFaceShape.SOLID;
                    case STRAIGHT:
                        return facing == face ? BlockFaceShape.SOLID : BlockFaceShape.UNDEFINED;
                    default:
                        return BlockFaceShape.UNDEFINED;
                }
            } else {
                return BlockFaceShape.UNDEFINED;
            }
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


    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        this.modelBlock.randomDisplayTick(stateIn, worldIn, pos, rand);
    }


    @Override
    public void onBlockClicked(World worldIn, BlockPos pos, EntityPlayer playerIn) {
        this.modelBlock.onBlockClicked(worldIn, pos, playerIn);
    }


    @Override
    public void onPlayerDestroy(World worldIn, BlockPos pos, IBlockState state) {
        this.modelBlock.onPlayerDestroy(worldIn, pos, state);
    }


    @Override
    @SideOnly(Side.CLIENT)
    public int getPackedLightmapCoords(IBlockState state, IBlockAccess source, BlockPos pos) {
        return this.modelState.getPackedLightmapCoords(source, pos);
    }


    @Override
    public float getExplosionResistance(Entity exploder) {
        return this.modelBlock.getExplosionResistance(exploder);
    }


    @Override
    public int tickRate(World worldIn) {
        return this.modelBlock.tickRate(worldIn);
    }


    @Override
    public Vec3d modifyAcceleration(World worldIn, BlockPos pos, Entity entityIn, Vec3d motion) {
        return this.modelBlock.modifyAcceleration(worldIn, pos, entityIn, motion);
    }


    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        return this.modelBlock.getRenderLayer();
    }


    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World worldIn, BlockPos pos) {
        return this.modelState.getSelectedBoundingBox(worldIn, pos);
    }


    @Override
    public boolean isCollidable() {
        return this.modelBlock.isCollidable();
    }


    @Override
    public boolean canCollideCheck(IBlockState state, boolean hitIfLiquid) {
        return this.modelBlock.canCollideCheck(state, hitIfLiquid);
    }


    @Override
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        return this.modelBlock.canPlaceBlockAt(worldIn, pos);
    }


    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
        this.modelState.neighborChanged(worldIn, pos, Blocks.AIR, pos);
        this.modelBlock.onBlockAdded(worldIn, pos, this.modelState);
    }


    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        this.modelBlock.breakBlock(worldIn, pos, this.modelState);
    }


    @Override
    public void onEntityWalk(World worldIn, BlockPos pos, Entity entityIn) {
        this.modelBlock.onEntityWalk(worldIn, pos, entityIn);
    }


    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        this.modelBlock.updateTick(worldIn, pos, state, rand);
    }


    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        return this.modelBlock.onBlockActivated(worldIn, pos, this.modelState, playerIn, hand, EnumFacing.DOWN, 0.0F, 0.0F, 0.0F);
    }


    @Override
    public void onExplosionDestroy(World worldIn, BlockPos pos, Explosion explosionIn) {
        this.modelBlock.onExplosionDestroy(worldIn, pos, explosionIn);
    }


    @Override
    public boolean isTopSolid(IBlockState state) {
        return state.getValue(HALF) == BlockStairs.EnumHalf.TOP;
    }


    @Override
    public MapColor getMapColor(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        return this.modelBlock.getMapColor(this.modelState, worldIn, pos);
    }


    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        IBlockState iblockstate = super.getStateForPlacement(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer);
        iblockstate = iblockstate.withProperty(FACING, placer.getHorizontalFacing()).withProperty(SHAPE, BlockStairs.EnumShape.STRAIGHT);
        return facing != EnumFacing.DOWN && (facing == EnumFacing.UP || (double)hitY <= 0.5D) ? iblockstate.withProperty(HALF, BlockStairs.EnumHalf.BOTTOM) : iblockstate.withProperty(HALF, BlockStairs.EnumHalf.TOP);
    }


    //todo
    @Nullable
    public RayTraceResult collisionRayTrace(IBlockState blockState, World worldIn, BlockPos pos, Vec3d start, Vec3d end) {
        List<AxisAlignedBB> col = getCollisionBoxList(this.getActualState(blockState, worldIn, pos));
        ArrayListEV<RayTraceResult> list = new ArrayListEV<>(col.size());

        for (AxisAlignedBB axisalignedbb : col) {
            list.addF(this.rayTrace(pos, start, end, axisalignedbb));
        }

        RayTraceResult raytraceresult1 = null;
        double d1 = 0.0D;

        for (RayTraceResult raytraceresult : list) {
            if (raytraceresult != null) {
                double d0 = raytraceresult.hitVec.squareDistanceTo(end);
                if (d0 > d1) {
                    raytraceresult1 = raytraceresult;
                    d1 = d0;
                }
            }
        }
        return raytraceresult1;
    }


    @Override
    public IBlockState getStateFromMeta(int meta) {
        IBlockState iblockstate = this.getDefaultState().withProperty(HALF, (meta & 4) > 0 ? BlockStairs.EnumHalf.TOP : BlockStairs.EnumHalf.BOTTOM);
        iblockstate = iblockstate.withProperty(FACING, EnumFacing.byIndex(5 - (meta & 3)));
        return iblockstate;
    }


    @Override
    public int getMetaFromState(IBlockState state) {
        int i = 0;

        if (state.getValue(HALF) == BlockStairs.EnumHalf.TOP) {
            i |= 4;
        }
        i = i | 5 - state.getValue(FACING).getIndex();
        return i;
    }


    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        return state.withProperty(SHAPE, getStairsShape(state, worldIn, pos));
    }


    private static BlockStairs.EnumShape getStairsShape(IBlockState p_185706_0_, IBlockAccess access, BlockPos pos) {
        EnumFacing enumfacing = p_185706_0_.getValue(FACING);
        IBlockState iblockstate = access.getBlockState(pos.offset(enumfacing));

        if (isBlockStairs(iblockstate) && p_185706_0_.getValue(HALF) == iblockstate.getValue(HALF)) {
            EnumFacing facing = iblockstate.getValue(FACING);

            if (facing.getAxis() != p_185706_0_.getValue(FACING).getAxis() && isDifferentStairs(p_185706_0_, access, pos, facing.getOpposite())) {
                if (facing == enumfacing.rotateYCCW()) {
                    return BlockStairs.EnumShape.OUTER_LEFT;
                }
                return BlockStairs.EnumShape.OUTER_RIGHT;
            }
        }

        IBlockState state = access.getBlockState(pos.offset(enumfacing.getOpposite()));

        if (isBlockStairs(state) && p_185706_0_.getValue(HALF) == state.getValue(HALF)) {
            EnumFacing facing = state.getValue(FACING);

            if (facing.getAxis() != p_185706_0_.getValue(FACING).getAxis() && isDifferentStairs(p_185706_0_, access, pos, facing)) {
                if (facing == enumfacing.rotateYCCW()) {
                    return BlockStairs.EnumShape.INNER_LEFT;
                }

                return BlockStairs.EnumShape.INNER_RIGHT;
            }
        }
        return BlockStairs.EnumShape.STRAIGHT;
    }


    private static boolean isDifferentStairs(IBlockState state, IBlockAccess access, BlockPos pos, EnumFacing facing) {
        IBlockState iblockstate = access.getBlockState(pos.offset(facing));
        return !isBlockStairs(iblockstate) || iblockstate.getValue(FACING) != state.getValue(FACING) || iblockstate.getValue(HALF) != state.getValue(HALF);
    }


    public static boolean isBlockStairs(IBlockState state) {
        return state.getBlock() instanceof BlockStairs;
    }


    @Override
    public IBlockState withRotation(IBlockState state, Rotation rot) {
        return state.withProperty(FACING, rot.rotate(state.getValue(FACING)));
    }


    @Override
    @SuppressWarnings("incomplete-switch")
    public IBlockState withMirror(IBlockState state, Mirror mirrorIn) {
        EnumFacing enumfacing = state.getValue(FACING);
        BlockStairs.EnumShape blockstairs$enumshape = state.getValue(SHAPE);

        switch (mirrorIn) {
            case LEFT_RIGHT:

                if (enumfacing.getAxis() == EnumFacing.Axis.Z) {
                    switch (blockstairs$enumshape) {
                        case OUTER_LEFT:
                            return state.withRotation(Rotation.CLOCKWISE_180).withProperty(SHAPE, BlockStairs.EnumShape.OUTER_RIGHT);
                        case OUTER_RIGHT:
                            return state.withRotation(Rotation.CLOCKWISE_180).withProperty(SHAPE, BlockStairs.EnumShape.OUTER_LEFT);
                        case INNER_RIGHT:
                            return state.withRotation(Rotation.CLOCKWISE_180).withProperty(SHAPE, BlockStairs.EnumShape.INNER_LEFT);
                        case INNER_LEFT:
                            return state.withRotation(Rotation.CLOCKWISE_180).withProperty(SHAPE, BlockStairs.EnumShape.INNER_RIGHT);
                        default:
                            return state.withRotation(Rotation.CLOCKWISE_180);
                    }
                }

                break;
            case FRONT_BACK:

                if (enumfacing.getAxis() == EnumFacing.Axis.X) {
                    switch (blockstairs$enumshape) {
                        case OUTER_LEFT:
                            return state.withRotation(Rotation.CLOCKWISE_180).withProperty(SHAPE, BlockStairs.EnumShape.OUTER_RIGHT);
                        case OUTER_RIGHT:
                            return state.withRotation(Rotation.CLOCKWISE_180).withProperty(SHAPE, BlockStairs.EnumShape.OUTER_LEFT);
                        case INNER_RIGHT:
                            return state.withRotation(Rotation.CLOCKWISE_180).withProperty(SHAPE, BlockStairs.EnumShape.INNER_RIGHT);
                        case INNER_LEFT:
                            return state.withRotation(Rotation.CLOCKWISE_180).withProperty(SHAPE, BlockStairs.EnumShape.INNER_LEFT);
                        case STRAIGHT:
                            return state.withRotation(Rotation.CLOCKWISE_180);
                    }
                }
        }
        return super.withMirror(state, mirrorIn);
    }


    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING, HALF, SHAPE);
    }


    @Override
    public boolean doesSideBlockRendering(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing face) {
        if (net.minecraftforge.common.ForgeModContainer.disableStairSlabCulling)
            return super.doesSideBlockRendering(state, world, pos, face);

        if ( state.isOpaqueCube() ) return true;

        state = this.getActualState(state, world, pos);

        EnumHalf half = state.getValue(HALF);
        EnumFacing side = state.getValue(FACING);
        EnumShape shape = state.getValue(SHAPE);
        if (face == EnumFacing.UP) return half == EnumHalf.TOP;
        if (face == EnumFacing.DOWN) return half == EnumHalf.BOTTOM;
        if (shape == EnumShape.OUTER_LEFT || shape == EnumShape.OUTER_RIGHT) return false;
        if (face == side) return true;
        if (shape == EnumShape.INNER_LEFT && face.rotateY() == side) return true;
        return shape == EnumShape.INNER_RIGHT && face.rotateYCCW() == side;
    }


    @Keep
    public enum EnumHalf implements IStringSerializable {
        TOP("top"),
        BOTTOM("bottom");

        private final String name;

        EnumHalf(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return this.name;
        }


        @Override
        public String getName() {
            return this.name;
        }
    }


    @Keep
    public enum EnumShape implements IStringSerializable {
        STRAIGHT("straight"),
        INNER_LEFT("inner_left"),
        INNER_RIGHT("inner_right"),
        OUTER_LEFT("outer_left"),
        OUTER_RIGHT("outer_right");

        private final String name;

        EnumShape(String name) {
            this.name = name;
        }


        @Override
        public String toString() {
            return this.name;
        }


        @Override
        public String getName() {
            return this.name;
        }
    }
}
