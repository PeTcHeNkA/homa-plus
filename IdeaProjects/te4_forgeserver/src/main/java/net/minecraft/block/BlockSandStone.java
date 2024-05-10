package net.minecraft.block;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import plus.fserver.block.BlockFx;


public class BlockSandStone extends BlockFx {
    public static final PropertyEnum<BlockSandStone.EnumType> TYPE = PropertyEnum.create("type", BlockSandStone.EnumType.class);

    public BlockSandStone() {
        super(Material.ROCK);
        this.setDefaultState(this.blockState.getBaseState().withProperty(TYPE, BlockSandStone.EnumType.DEFAULT));
        this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
    }


    @Override
    public int damageDropped(IBlockState state) {
        return state.getValue(TYPE).getMetadata();
    }


    @Override
    public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
        for (BlockSandStone.EnumType blocksandstone$enumtype : BlockSandStone.EnumType.values()) {
            items.add(new ItemStack(this, 1, blocksandstone$enumtype.getMetadata()));
        }
    }


    @Override
    public MapColor getMapColor(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        return MapColor.SAND;
    }


    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(TYPE, BlockSandStone.EnumType.byMetadata(meta));
    }


    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(TYPE).getMetadata();
    }


    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, TYPE);
    }


    public enum EnumType implements IStringSerializable {
        DEFAULT(0, "sandstone", "default"),
        CHISELED(1, "chiseled_sandstone", "chiseled"),
        SMOOTH(2, "smooth_sandstone", "smooth");

        private static final BlockSandStone.EnumType[] META_LOOKUP = new BlockSandStone.EnumType[values().length];
        private final int metadata;
        private final String name;
        private final String translationKey;

        EnumType(int meta, String name, String unlocalizedName) {
            this.metadata = meta;
            this.name = name;
            this.translationKey = unlocalizedName;
        }

        public int getMetadata() {
            return this.metadata;
        }


        @Override
        public String toString() {
            return this.name;
        }


        public static BlockSandStone.EnumType byMetadata(int meta) {
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
            for (BlockSandStone.EnumType blocksandstone$enumtype : values()) {
                META_LOOKUP[blocksandstone$enumtype.getMetadata()] = blocksandstone$enumtype;
            }
        }
    }
}