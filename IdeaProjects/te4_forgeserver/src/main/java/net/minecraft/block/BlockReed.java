package net.minecraft.block;

import net.minecraft.block.material.*;
import net.minecraft.block.properties.*;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import java.util.*;
import javax.annotation.*;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraftforge.fml.relauncher.*;
import net.minecraftforge.common.*;
import net.minecraft.block.state.*;
import plus.fserver.api.IBlockEAccess;
import plus.fserver.server.ElcaThread;
import plus.fserver.utls.collections.AABBList;


public class BlockReed extends Block implements IPlantable {
    public static final PropertyInteger AGE;
    protected static final AxisAlignedBB REED_AABB;

    protected BlockReed() {
        super(Material.PLANTS);
        this.setDefaultState(this.blockState.getBaseState().withProperty(BlockReed.AGE, 0));
        this.setTickRandomly(true);
    }


    @Override
    public void addCollisionBoxToListF(IBlockState state, IBlockAccess access, BlockPos pos, AxisAlignedBB entityBox, AABBList collidingBoxes) {}


    @Override
    public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @org.jetbrains.annotations.Nullable Entity entityIn, boolean isActualState) {}


    @Override
    public AxisAlignedBB getBoundingBox(final IBlockState state, final IBlockAccess source, final BlockPos pos) {
        return BlockReed.REED_AABB;
    }


    @Override
    public void updateTick(final World world, final BlockPos pos, final IBlockState state, final Random rand) {
        final IBlockEAccess access = world.access();
        if ((access.getBlockState(pos.getX(), pos.getY()-1, pos.getZ()).getBlock() == Blocks.REEDS || this.checkForDrop(world, pos, state)) && world.isAirBlock(pos.up())) {
            int i;
            for (i = 1; access.getBlockState(pos.getX(), pos.getY()-i, pos.getZ()).getBlock() == this; ++i) {}
            if (i < 3) {
                final int j = state.getValue(BlockReed.AGE);
                if (ForgeHooks.onCropsGrowPre(world, pos, state, true)) {
                    if (j == 15) {
                        BlockPos up = world.localTempPos2.setPos(pos.getX(), pos.getY()+1, pos.getZ());
                        access.setBlockState(up, this.getDefaultState(), 3);
                        access.setBlockState(pos, state.withProperty(BlockReed.AGE, 0), 4);
                    } else {
                        access.setBlockState(pos, state.withProperty(BlockReed.AGE, j + 1), 4);
                    }
                    ForgeHooks.onCropsGrowPost(world, pos, state, access.getBlockState(pos));
                }
            }
        }
    }


    @Override
    public boolean canPlaceBlockAt(final World world, final BlockPos pos) {
        return canPlaceBlockAtAsy(world, pos);
    }


    @Override
    public boolean canPlaceBlockAtAsy(final IBlockEAccess access, final BlockPos pos) {
        BlockPos temp = new BlockPos(pos.getX(), pos.getY()-1, pos.getZ());
        final IBlockState state = access.getBlockState(temp);
        final Block block = state.getBlock();
        if (block.canSustainPlant(state, access, temp, EnumFacing.UP, this)) {
            return true;
        }
        if (block == this) {
            return true;
        }
        if (block != Blocks.GRASS && block != Blocks.DIRT && block != Blocks.SAND) {
            return false;
        }
        for (final EnumFacing facing : EnumFacing.Plane.HORIZONTAL) {
            final IBlockState iblockstate = access.getBlockState(
                    temp.getX() + facing.getXOffset(),
                    temp.getY() + facing.getYOffset(),
                    temp.getZ() + facing.getZOffset()
            );
            if (iblockstate.getMaterial() == Material.WATER || iblockstate.getBlock() == Blocks.FROSTED_ICE) {
                return true;
            }
        }
        return false;
    }


    @Override
    public void neighborChanged(final IBlockState state, final World worldIn, final BlockPos pos, final Block blockIn, final BlockPos fromPos) {
        this.checkForDrop(worldIn, pos, state);
    }


    protected final boolean checkForDrop(final World worldIn, final BlockPos pos, final IBlockState state) {
        if (this.canBlockStay(worldIn, pos)) {
            return true;
        }
        this.dropBlockAsItem(worldIn, pos, state, 0);
        worldIn.setBlockToAir(pos);
        return false;
    }


    public boolean canBlockStay(final World worldIn, final BlockPos pos) {
        return this.canPlaceBlockAt(worldIn, pos);
    }


    @Nullable
    public AxisAlignedBB getCollisionBoundingBox(final IBlockState blockState, final IBlockAccess worldIn, final BlockPos pos) {
        return BlockReed.NULL_AABB;
    }


    @Override
    public Item getItemDropped(final IBlockState state, final Random rand, final int fortune) {
        return Items.REEDS;
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
    public ItemStack getItem(final World worldIn, final BlockPos pos, final IBlockState state) {
        return new ItemStack(Items.REEDS);
    }


    @Override
    public IBlockState getStateFromMeta(final int meta) {
        return this.getDefaultState().withProperty(BlockReed.AGE, meta);
    }


    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }


    @Override
    public int getMetaFromState(final IBlockState state) {
        return state.getValue(BlockReed.AGE);
    }


    @Override
    public EnumPlantType getPlantType(final IBlockAccess p_getPlantType_1_, final BlockPos p_getPlantType_2_) {
        return EnumPlantType.Beach;
    }


    @Override
    public IBlockState getPlant(final IBlockAccess p_getPlant_1_, final BlockPos p_getPlant_2_) {
        return this.getDefaultState();
    }


    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, BlockReed.AGE);
    }


    @Override
    public BlockFaceShape getBlockFaceShape(final IBlockAccess worldIn, final IBlockState state, final BlockPos pos, final EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }


    static {
        AGE = PropertyInteger.create("age", 0, 15);
        REED_AABB = new AxisAlignedBB(0.125, 0.0, 0.125, 0.875, 1.0, 0.875);
    }
}