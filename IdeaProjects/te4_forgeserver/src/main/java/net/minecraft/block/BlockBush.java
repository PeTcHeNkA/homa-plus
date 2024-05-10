package net.minecraft.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


public class BlockBush extends Block implements net.minecraftforge.common.IPlantable {
    protected static final AxisAlignedBB BUSH_AABB = new AxisAlignedBB(
                    0.30000001192092896D, 0.0D, 0.30000001192092896D,
                    0.699999988079071D, 0.6000000238418579D, 0.699999988079071D
            );

    protected BlockBush() {
        this(Material.PLANTS);
    }


    protected BlockBush(Material materialIn) {
        this(materialIn, materialIn.getMaterialMapColor());
    }


    protected BlockBush(Material materialIn, MapColor mapColorIn) {
        super(materialIn, mapColorIn);
        this.setTickRandomly(true);
        this.setCreativeTab(CreativeTabs.DECORATIONS);
    }

    public static boolean canGrowFlower(BlockBush block, IBlockState state){
        return block.canSustainBush(state);
    }


    @Override
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        IBlockState soil = worldIn.getBlockState(pos.down());
        return super.canPlaceBlockAt(worldIn, pos) && soil.getBlock().canSustainPlant(soil, worldIn, pos.down(), net.minecraft.util.EnumFacing.UP, this);
    }


    protected boolean canSustainBush(IBlockState state) {
        return state.getBlock() == Blocks.GRASS || state.getBlock() == Blocks.DIRT || state.getBlock() == Blocks.FARMLAND;
    }


    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
        this.checkAndDropBlock(worldIn, pos, state);
    }


    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        this.checkAndDropBlock(worldIn, pos, state);
    }


    protected void checkAndDropBlock(World worldIn, BlockPos pos, IBlockState state) {
        if (!this.canBlockStay(worldIn, pos, state)) {
            this.dropBlockAsItem(worldIn, pos, state, 0);
            worldIn.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
        }
    }


    public boolean canBlockStay(World worldIn, BlockPos pos, IBlockState state) {
        //BlockPos down = pos.down();
        if (state.getBlock() == this)
            //Forge: This function is called during world gen and placement,
            // before this block is set, so if we are not 'here' then assume it's the pre-check.
        {
            BlockPos down = worldIn.localTempPos3.setPos(pos.getX(),pos.getY()-1,pos.getZ());
            IBlockState soil = worldIn.access().getBlockState(down);
            return soil.getBlock().canSustainPlant(soil, worldIn, down, net.minecraft.util.EnumFacing.UP, this);
        }
        return this.canSustainBush(worldIn.getBlockState(pos.getX(), pos.getY()-1, pos.getZ()));
    }


    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return BUSH_AABB;
    }


    @Nullable
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return NULL_AABB;
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
    public EnumPlantType getPlantType(net.minecraft.world.IBlockAccess world, BlockPos pos) {
        if (this == Blocks.WHEAT)          return EnumPlantType.Crop;
        if (this == Blocks.CARROTS)        return EnumPlantType.Crop;
        if (this == Blocks.POTATOES)       return EnumPlantType.Crop;
        if (this == Blocks.BEETROOTS)      return EnumPlantType.Crop;
        if (this == Blocks.MELON_STEM)     return EnumPlantType.Crop;
        if (this == Blocks.PUMPKIN_STEM)   return EnumPlantType.Crop;
        if (this == Blocks.DEADBUSH)       return EnumPlantType.Desert;
        if (this == Blocks.WATERLILY)      return EnumPlantType.Water;
        if (this == Blocks.RED_MUSHROOM)   return EnumPlantType.Cave;
        if (this == Blocks.BROWN_MUSHROOM) return EnumPlantType.Cave;
        if (this == Blocks.NETHER_WART)    return EnumPlantType.Nether;
        if (this == Blocks.SAPLING)        return EnumPlantType.Plains;
        if (this == Blocks.TALLGRASS)      return EnumPlantType.Plains;
        if (this == Blocks.DOUBLE_PLANT)   return EnumPlantType.Plains;
        if (this == Blocks.RED_FLOWER)     return EnumPlantType.Plains;
        if (this == Blocks.YELLOW_FLOWER)  return EnumPlantType.Plains;
        return net.minecraftforge.common.EnumPlantType.Plains;
    }


    @Override
    public IBlockState getPlant(net.minecraft.world.IBlockAccess world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() != this) return getDefaultState();
        return state;
    }


    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }


    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }
}
