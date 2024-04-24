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


public class BlockPlanks extends BlockFs {
    public static final PropertyEnum<BlockPlanks.EnumType> VARIANT = PropertyEnum.create("variant", BlockPlanks.EnumType.class);

    public BlockPlanks() {
        super(Material.WOOD);
        this.setDefaultState(this.blockState.getBaseState().withProperty(VARIANT, BlockPlanks.EnumType.OAK));
        this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
    }


    @Override
    public int damageDropped(IBlockState state) {
        return state.getValue(VARIANT).getMetadata();
    }


    @Override
    public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
        for (BlockPlanks.EnumType type : BlockPlanks.EnumType.values()) {
            items.add(new ItemStack(this, 1, type.getMetadata()));
        }
    }


    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(VARIANT, BlockPlanks.EnumType.byMetadata(meta));
    }


    @Override
    public MapColor getMapColor(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        return state.getValue(VARIANT).getMapColor();
    }


    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(VARIANT).getMetadata();
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, VARIANT);
    }


    public enum EnumType implements IStringSerializable {
        OAK(0, "oak", MapColor.WOOD),
        SPRUCE(1, "spruce", MapColor.OBSIDIAN),
        BIRCH(2, "birch", MapColor.SAND),
        JUNGLE(3, "jungle", MapColor.DIRT),
        ACACIA(4, "acacia", MapColor.ADOBE),
        DARK_OAK(5, "dark_oak", "big_oak", MapColor.BROWN);

        private static final BlockPlanks.EnumType[] META_LOOKUP = new BlockPlanks.EnumType[values().length];
        private final int meta;
        private final String name;
        private final String translationKey;
        private final MapColor mapColor;

        EnumType(int metaIn, String nameIn, MapColor mapColorIn) {
            this(metaIn, nameIn, nameIn, mapColorIn);
        }


        EnumType(int metaIn, String nameIn, String unlocalizedNameIn, MapColor mapColorIn) {
            this.meta = metaIn;
            this.name = nameIn;
            this.translationKey = unlocalizedNameIn;
            this.mapColor = mapColorIn;
        }


        public int getMetadata() {
            return this.meta;
        }


        public MapColor getMapColor() {
            return this.mapColor;
        }


        @Override
        public String toString() {
            return this.name;
        }


        public static BlockPlanks.EnumType byMetadata(int meta) {
            if (meta < 0 || meta >= META_LOOKUP.length) {
                meta = 0;
            }
            return META_LOOKUP[meta];
        }


        public String getName() {
            return this.name;
        }


        public String getTranslationKey() {
            return this.translationKey;
        }

        static {
            for (BlockPlanks.EnumType type : values()) {
                META_LOOKUP[type.getMetadata()] = type;
            }
        }
    }
}