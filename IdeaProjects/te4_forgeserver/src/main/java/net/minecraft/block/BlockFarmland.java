package net.minecraft.block;

import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.fserver.ElcaCV;
import plus.fserver.block.BlockFx;


public class BlockFarmland extends BlockFx {
    public static final PropertyInteger MOISTURE = PropertyInteger.create("moisture", 0, 7);
    protected static final AxisAlignedBB FARMLAND_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.9375D, 1.0D);
    protected static final AxisAlignedBB field_194405_c = new AxisAlignedBB(0.0D, 0.9375D, 0.0D, 1.0D, 1.0D, 1.0D);

    protected BlockFarmland() {
        super(Material.GROUND);
        this.setDefaultState(this.blockState.getBaseState().withProperty(MOISTURE, Integer.valueOf(0)));
        this.setTickRandomly(true);
        this.setLightOpacity(255);
    }


    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return FARMLAND_AABB;
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
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        int i = state.getValue(MOISTURE);
        BlockPos.MutableBlockPos temp = worldIn.localTempPos;
        if (!this.hasWater(worldIn, temp)) {
            if (!worldIn.isRainingAt(temp.setPos(pos.getX(), pos.getY() + 1, pos.getZ()))) {
                if (i > 0) {
                    worldIn.setBlockState(pos, state.withProperty(MOISTURE, i - 1), 2);
                } else if (!this.hasCrops(worldIn, pos)) {
                    turnToDirt(worldIn, pos);
                }
            }
            return;
        }

        if (i < 7) {
            worldIn.setBlockState(pos, state.withProperty(MOISTURE, 7), 2);
        }
    }


    @Override
    public void onFallenUpon(World worldIn, BlockPos pos, Entity entityIn, float fallDistance) {
        if (net.minecraftforge.common.ForgeHooks.onFarmlandTrample(worldIn, pos, Blocks.DIRT.getDefaultState(), fallDistance, entityIn))
            // Forge: Move logic to Entity#canTrample
        {
            turnToDirt(worldIn, pos);
        }
        super.onFallenUpon(worldIn, pos, entityIn, fallDistance);
    }


    protected static void turnToDirt(World world, BlockPos pos) {
        world.setBlockState(pos, Blocks.DIRT.getDefaultState());
        AxisAlignedBB axisalignedbb;
        field_194405_c.offsetTo(pos, axisalignedbb = world.commonWorldEntityAABB);

        for (Entity entity : world.getEntitiesWithinAABBExcludingEntity(null, axisalignedbb)) {
            double d0 = Math.min(axisalignedbb.maxY - axisalignedbb.minY, axisalignedbb.maxY - entity.getEntityBoundingBox().minY);
            entity.setPositionAndUpdate(entity.posX, entity.posY + d0 + 0.001D, entity.posZ);
        }
    }


    private boolean hasCrops(World worldIn, BlockPos pos) {
        Block block = worldIn.getBlockState(pos.up()).getBlock();
        return block instanceof IPlantable && canSustainPlant(worldIn.getBlockState(pos), worldIn, pos, EnumFacing.UP, (IPlantable)block);
    }


    private boolean hasWater(World worldIn, BlockPos.MutableBlockPos pos) {
        for (BlockPos.MutableBlockPos pos1 : BlockPos.getAllInBoxMutable(pos,
                pos.getX()-4, pos.getY(), pos.getZ()-4,
                pos.getX()+4, pos.getY(), pos.getZ()+4
        )) {
            if (worldIn.getBlockState(pos1).getMaterial() == Material.WATER) {
                return true;
            }
        }
        if(ElcaCV.DISABLE_FORGE_FARMLAND)return false;
        return net.minecraftforge.common.FarmlandWaterManager.hasBlockWaterTicket(worldIn, pos);
    }


    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos);

        if (worldIn.getBlockState(pos.getX(), pos.getY()+1, pos.getZ()).getMaterial().isSolid()) {
            turnToDirt(worldIn, pos);
        }
    }


    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
        super.onBlockAdded(worldIn, pos, state);

        if (worldIn.getBlockState(pos.getX(), pos.getY()+1, pos.getZ()).getMaterial().isSolid()) {
            turnToDirt(worldIn, pos);
        }
    }


    @Override
    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        switch (side) {
            case UP:
                return true;
            case NORTH:
            case SOUTH:
            case WEST:
            case EAST:
                IBlockState iblockstate = blockAccess.getBlockState(pos.offset(side));
                Block block = iblockstate.getBlock();
                return !iblockstate.isOpaqueCube() && block != Blocks.FARMLAND && block != Blocks.GRASS_PATH;
            default:
                return super.shouldSideBeRendered(blockState, blockAccess, pos, side);
        }
    }


    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Blocks.DIRT.getItemDropped(Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.DIRT), rand, fortune);
    }


    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(MOISTURE, Integer.valueOf(meta & 7));
    }


    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(MOISTURE).intValue();
    }


    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, MOISTURE);
    }


    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        return face == EnumFacing.DOWN ? BlockFaceShape.SOLID : BlockFaceShape.UNDEFINED;
    }
}
