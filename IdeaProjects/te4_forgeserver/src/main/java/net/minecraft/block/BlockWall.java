package net.minecraft.block;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.fserver.api.Keep;
import plus.fserver.utls.collections.AABBList;
import static plus.fserver.block.BlockFs.addCollisionBoxToListFs;


public class BlockWall extends Block {
    @Keep
    public static final PropertyBool UP = PropertyBool.create("up");
    @Keep
    public static final PropertyBool NORTH = PropertyBool.create("north");
    @Keep
    public static final PropertyBool EAST = PropertyBool.create("east");
    @Keep
    public static final PropertyBool SOUTH = PropertyBool.create("south");
    @Keep
    public static final PropertyBool WEST = PropertyBool.create("west");
    @Keep
    public static final PropertyEnum<BlockWall.EnumType> VARIANT = PropertyEnum.create("variant", BlockWall.EnumType.class);
    @Keep
    protected static final AxisAlignedBB[] AABB_BY_INDEX = new AxisAlignedBB[] {new AxisAlignedBB(0.25D, 0.0D, 0.25D, 0.75D, 1.0D, 0.75D), new AxisAlignedBB(0.25D, 0.0D, 0.25D, 0.75D, 1.0D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.25D, 0.75D, 1.0D, 0.75D), new AxisAlignedBB(0.0D, 0.0D, 0.25D, 0.75D, 1.0D, 1.0D), new AxisAlignedBB(0.25D, 0.0D, 0.0D, 0.75D, 1.0D, 0.75D), new AxisAlignedBB(0.3125D, 0.0D, 0.0D, 0.6875D, 0.875D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.75D, 1.0D, 0.75D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.75D, 1.0D, 1.0D), new AxisAlignedBB(0.25D, 0.0D, 0.25D, 1.0D, 1.0D, 0.75D), new AxisAlignedBB(0.25D, 0.0D, 0.25D, 1.0D, 1.0D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.3125D, 1.0D, 0.875D, 0.6875D), new AxisAlignedBB(0.0D, 0.0D, 0.25D, 1.0D, 1.0D, 1.0D), new AxisAlignedBB(0.25D, 0.0D, 0.0D, 1.0D, 1.0D, 0.75D), new AxisAlignedBB(0.25D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 0.75D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D)};
    @Keep
    protected static final AxisAlignedBB[] CLIP_AABB_BY_INDEX = new AxisAlignedBB[] {AABB_BY_INDEX[0].setMaxY(1.5D), AABB_BY_INDEX[1].setMaxY(1.5D), AABB_BY_INDEX[2].setMaxY(1.5D), AABB_BY_INDEX[3].setMaxY(1.5D), AABB_BY_INDEX[4].setMaxY(1.5D), AABB_BY_INDEX[5].setMaxY(1.5D), AABB_BY_INDEX[6].setMaxY(1.5D), AABB_BY_INDEX[7].setMaxY(1.5D), AABB_BY_INDEX[8].setMaxY(1.5D), AABB_BY_INDEX[9].setMaxY(1.5D), AABB_BY_INDEX[10].setMaxY(1.5D), AABB_BY_INDEX[11].setMaxY(1.5D), AABB_BY_INDEX[12].setMaxY(1.5D), AABB_BY_INDEX[13].setMaxY(1.5D), AABB_BY_INDEX[14].setMaxY(1.5D), AABB_BY_INDEX[15].setMaxY(1.5D)};

    public BlockWall(Block modelBlock) {
        super(modelBlock.material);
        this.setDefaultState(this.blockState.getBaseState().withProperty(UP, Boolean.FALSE).withProperty(NORTH, Boolean.FALSE).withProperty(EAST, Boolean.FALSE).withProperty(SOUTH, Boolean.FALSE).withProperty(WEST, Boolean.FALSE).withProperty(VARIANT, BlockWall.EnumType.NORMAL));
        this.setHardness(modelBlock.blockHardness);
        this.setResistance(modelBlock.blockResistance / 3.0F);
        this.setSoundType(modelBlock.blockSoundType);
        this.setCreativeTab(CreativeTabs.DECORATIONS);
    }


    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        state = this.getActualState(state, source, pos);
        return AABB_BY_INDEX[getAABBIndex(state)];
    }


    @Override
    public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState) {
        if (!isActualState) {
            state = this.getActualState(state, worldIn, pos);
        }
        addCollisionBoxToList(pos, entityBox, collidingBoxes, CLIP_AABB_BY_INDEX[getAABBIndex(state)]);
    }


    @Override
    public void addCollisionBoxToListF(IBlockState state, IBlockAccess access, BlockPos pos, AxisAlignedBB entityBox, AABBList collidingBoxes) {
        addCollisionBoxToListFs(pos, entityBox, collidingBoxes, CLIP_AABB_BY_INDEX[getAABBIndex(getActualState(state, access, pos))]);
    }


    @Nullable
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        blockState = this.getActualState(blockState, worldIn, pos);
        return CLIP_AABB_BY_INDEX[getAABBIndex(blockState)];
    }


    private static int getAABBIndex(IBlockState state) {
        int i = 0;
        if (state.getValue(NORTH)) {
            i |= 1 << EnumFacing.NORTH.getHorizontalIndex();
        }
        if (state.getValue(EAST)) {
            i |= 1 << EnumFacing.EAST.getHorizontalIndex();
        }
        if (state.getValue(SOUTH)) {
            i |= 1 << EnumFacing.SOUTH.getHorizontalIndex();
        }
        if (state.getValue(WEST)) {
            i |= 1 << EnumFacing.WEST.getHorizontalIndex();
        }
        return i;
    }


    @Override
    public String getLocalizedName() {
        return I18n.translateToLocal(this.getTranslationKey() + "." + BlockWall.EnumType.NORMAL.getTranslationKey() + ".name");
    }


    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }


    @Override
    public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
        return false;
    }


    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }


    private boolean canConnectTo(IBlockAccess worldIn, BlockPos pos, EnumFacing facing) {
        IBlockState state = worldIn.getBlockState(pos);
        Block block = state.getBlock();
        BlockFaceShape shape = state.getBlockFaceShape(worldIn, pos, facing);
        boolean flag = shape == BlockFaceShape.MIDDLE_POLE_THICK || shape == BlockFaceShape.MIDDLE_POLE && block instanceof BlockFenceGate;
        return !isExcepBlockForAttachWithPiston(block) && shape == BlockFaceShape.SOLID || flag;
    }


    protected static boolean isExcepBlockForAttachWithPiston(Block block) {
        return Block.isExceptBlockForAttachWithPiston(block) || block == Blocks.BARRIER || block == Blocks.MELON_BLOCK || block == Blocks.PUMPKIN || block == Blocks.LIT_PUMPKIN;
    }


    @Override
    public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
        for (BlockWall.EnumType blockwall$enumtype : BlockWall.EnumType.values()) {
            items.add(new ItemStack(this, 1, blockwall$enumtype.getMetadata()));
        }
    }


    @Override
    public int damageDropped(IBlockState state) {
        return state.getValue(VARIANT).getMetadata();
    }


    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        return side == EnumFacing.DOWN ? super.shouldSideBeRendered(blockState, blockAccess, pos, side) : true;
    }


    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(VARIANT, BlockWall.EnumType.byMetadata(meta));
    }


    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(VARIANT).getMetadata();
    }


    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        boolean flag =  canWallConnectTo(worldIn, pos, EnumFacing.NORTH);
        boolean flag1 = canWallConnectTo(worldIn, pos, EnumFacing.EAST);
        boolean flag2 = canWallConnectTo(worldIn, pos, EnumFacing.SOUTH);
        boolean flag3 = canWallConnectTo(worldIn, pos, EnumFacing.WEST);
        boolean flag4 = flag && !flag1 && flag2 && !flag3 || !flag && flag1 && !flag2 && flag3;
        return state.withProperty(UP, !flag4 ||
                !worldIn.isAirBlock(pos.up()))
                .withProperty(NORTH, flag)
                .withProperty(EAST, flag1)
                .withProperty(SOUTH, flag2)
                .withProperty(WEST, flag3);
    }


    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, UP, NORTH, EAST, WEST, SOUTH, VARIANT);
    }


    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        return face != EnumFacing.UP && face != EnumFacing.DOWN ? BlockFaceShape.MIDDLE_POLE_THICK : BlockFaceShape.CENTER_BIG;
    }

    /* ======================================== FORGE START ======================================== */

    @Override
    public boolean canBeConnectedTo(IBlockAccess world, BlockPos pos, EnumFacing facing) {
        return canConnectTo(world, pos.offset(facing), facing.getOpposite());
    }


    private boolean canWallConnectTo(IBlockAccess world, BlockPos pos, EnumFacing facing) {
        BlockPos other = pos.offset(facing);
        Block block = world.getBlockState(other).getBlock();
        return block.canBeConnectedTo(world, other, facing.getOpposite()) || canConnectTo(world, other, facing.getOpposite());
    }


    /* ======================================== FORGE END ======================================== */
    @Keep
    public enum EnumType implements IStringSerializable {
        NORMAL(0, "cobblestone", "normal"),
        MOSSY(1, "mossy_cobblestone", "mossy");

        private static final BlockWall.EnumType[] META_LOOKUP = new BlockWall.EnumType[values().length];
        private final int meta;
        private final String name;
        private final String translationKey;

        EnumType(int meta, String name, String unlocalizedName) {
            this.meta = meta;
            this.name = name;
            this.translationKey = unlocalizedName;
        }

        public int getMetadata() {
            return this.meta;
        }


        @Override
        public String toString() {
            return this.name;
        }


        public static BlockWall.EnumType byMetadata(int meta) {
            if (meta < 0 || meta >= META_LOOKUP.length) {
                meta = 0;
            }
            return META_LOOKUP[meta];
        }


        @Override
        public String getName() {
            return this.name;
        }


        public String getTranslationKey() {
            return this.translationKey;
        }


        static {
            for (BlockWall.EnumType blockwall$enumtype : values()) {
                META_LOOKUP[blockwall$enumtype.getMetadata()] = blockwall$enumtype;
            }
        }
    }
}
