package net.minecraft.block;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import plus.fserver.api.IBlockEAccess;


public class BlockConcretePowder extends BlockFallingFs {
    public static final PropertyEnum<EnumDyeColor> COLOR = PropertyEnum.<EnumDyeColor>create("color", EnumDyeColor.class);

    public BlockConcretePowder() {
        super(Material.SAND);
        this.setDefaultState(this.blockState.getBaseState().withProperty(COLOR, EnumDyeColor.WHITE));
        this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
    }


    @Override
    public void onEndFalling(World worldIn, BlockPos pos, IBlockState fallingState, IBlockState hitState) {
        if (hitState.getMaterial().isLiquid()) {
            worldIn.setBlockState(pos, Blocks.CONCRETE.getDefaultState().withProperty(BlockColored.COLOR, fallingState.getValue(COLOR)), 3);
        }
    }


    protected boolean tryTouchWater(World worldIn, BlockPos pos, IBlockState state) {
        IBlockEAccess access = worldIn.isRemote?worldIn:((WorldServer)worldIn).localSelector;

        for (EnumFacing enumfacing : EnumFacing.VALUES) {
            if (enumfacing != EnumFacing.DOWN) {
                if (access.getBlockState(
                        pos.getX()+enumfacing.getXOffset(),
                        pos.getY()+enumfacing.getYOffset(),
                        pos.getZ()+enumfacing.getZOffset()
                ).getMaterial() == Material.WATER) {
                    access = null;
                    break;
                }
            }
        }
        if (access != null) {
            access.setBlockState(pos, Blocks.CONCRETE.getDefaultState().withProperty(BlockColored.COLOR, state.getValue(COLOR)), 3);
            return true;
        }
        return false;
    }


    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        if (!this.tryTouchWater(worldIn, pos, state)) {
            super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
        }
    }


    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
        if (!this.tryTouchWater(worldIn, pos, state)) {
            super.onBlockAdded(worldIn, pos, state);
        }
    }


    @Override
    public int damageDropped(IBlockState state) {
        return state.getValue(COLOR).getMetadata();
    }


    @Override
    public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
        for (EnumDyeColor enumdyecolor : EnumDyeColor.values()) {
            items.add(new ItemStack(this, 1, enumdyecolor.getMetadata()));
        }
    }


    @Override
    public MapColor getMapColor(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        return MapColor.getBlockColor(state.getValue(COLOR));
    }


    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(COLOR, EnumDyeColor.byMetadata(meta));
    }


    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(COLOR).getMetadata();
    }


    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, COLOR);
    }
}