package net.minecraft.block;

import java.util.Random;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.fserver.block.BlockFx;


public abstract class BlockSlab extends BlockFx {
    public static final PropertyEnum<BlockSlab.EnumBlockHalf> HALF = PropertyEnum.create("half", BlockSlab.EnumBlockHalf.class);
    protected static final AxisAlignedBB AABB_BOTTOM_HALF = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.5D, 1.0D);
    protected static final AxisAlignedBB AABB_TOP_HALF    = new AxisAlignedBB(0.0D, 0.5D, 0.0D, 1.0D, 1.0D, 1.0D);

    public BlockSlab(Material materialIn) {
        this(materialIn, materialIn.getMaterialMapColor());
    }


    public BlockSlab(Material material, MapColor color) {
        super(material, color);
        this.fullBlock = this.isDouble();
        this.setLightOpacity(255);
    }


    @Override
    protected boolean canSilkHarvest() {
        return false;
    }


    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        if (this.isDouble()) {
            return FULL_BLOCK_AABB;
        } else {
            return state.getValue(HALF) == BlockSlab.EnumBlockHalf.TOP ? AABB_TOP_HALF : AABB_BOTTOM_HALF;
        }
    }


    @Override
    public boolean isTopSolid(IBlockState state) {
        return ((BlockSlab)state.getBlock()).isDouble() || state.getValue(HALF) == BlockSlab.EnumBlockHalf.TOP;
    }


    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        if (((BlockSlab)state.getBlock()).isDouble()) {
            return BlockFaceShape.SOLID;
        } else if (face == EnumFacing.UP && state.getValue(HALF) == BlockSlab.EnumBlockHalf.TOP) {
            return BlockFaceShape.SOLID;
        } else {
            return face == EnumFacing.DOWN && state.getValue(HALF) == BlockSlab.EnumBlockHalf.BOTTOM ? BlockFaceShape.SOLID : BlockFaceShape.UNDEFINED;
        }
    }


    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return this.isDouble();
    }


    @Override
    public boolean doesSideBlockRendering(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing face) {
        if (net.minecraftforge.common.ForgeModContainer.disableStairSlabCulling)
            return super.doesSideBlockRendering(state, world, pos, face);

        if ( state.isOpaqueCube() )
            return true;

        EnumBlockHalf side = state.getValue(HALF);
        return (side == EnumBlockHalf.TOP && face == EnumFacing.UP) || (side == EnumBlockHalf.BOTTOM && face == EnumFacing.DOWN);
    }


    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        IBlockState iblockstate = super.getStateForPlacement(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer).withProperty(HALF, BlockSlab.EnumBlockHalf.BOTTOM);

        if (this.isDouble()) {
            return iblockstate;
        } else {
            return facing != EnumFacing.DOWN && (facing == EnumFacing.UP || (double)hitY <= 0.5D) ? iblockstate : iblockstate.withProperty(HALF, BlockSlab.EnumBlockHalf.TOP);
        }
    }


    @Override
    public int quantityDropped(Random random) {
        return this.isDouble() ? 2 : 1;
    }


    @Override
    public boolean isFullCube(IBlockState state) {
        return this.isDouble();
    }


    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        if (this.isDouble()) {
            return super.shouldSideBeRendered(blockState, blockAccess, pos, side);
        } else if (side != EnumFacing.UP && side != EnumFacing.DOWN && !super.shouldSideBeRendered(blockState, blockAccess, pos, side)) {
            return false;
        }
        return super.shouldSideBeRendered(blockState, blockAccess, pos, side);
    }


    @SideOnly(Side.CLIENT)
    protected static boolean isHalfSlab(IBlockState state) {
        Block block = state.getBlock();
        return block == Blocks.STONE_SLAB || block == Blocks.WOODEN_SLAB || block == Blocks.STONE_SLAB2 || block == Blocks.PURPUR_SLAB;
    }


    public abstract String getTranslationKey(int meta);


    public abstract boolean isDouble();


    public abstract IProperty<?> getVariantProperty();


    public abstract Comparable<?> getTypeForItem(ItemStack stack);


    public enum EnumBlockHalf implements IStringSerializable {
        TOP("top"),
        BOTTOM("bottom");

        private final String name;


        EnumBlockHalf(String name) {
            this.name = name;
        }


        public String toString()
        {
            return this.name;
        }


        public String getName()
        {
            return this.name;
        }
    }
}
