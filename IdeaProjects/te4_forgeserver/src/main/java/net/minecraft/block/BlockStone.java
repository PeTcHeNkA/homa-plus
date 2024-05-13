package net.minecraft.block;

import java.util.Random;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.IBlockAccess;
import plus.fserver.api.Keep;
import plus.fserver.block.BlockFx;


public class BlockStone extends BlockFx {
    public static final PropertyEnum<BlockStone.EnumType> VARIANT = PropertyEnum.create("variant", BlockStone.EnumType.class);

    public BlockStone() {
        super(Material.ROCK);
        this.setDefaultState(this.blockState.getBaseState().withProperty(VARIANT, BlockStone.EnumType.STONE));
        this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
    }


    @Override
    public String getLocalizedName() {
        return I18n.translateToLocal(this.getTranslationKey() + "." + BlockStone.EnumType.STONE.getTranslationKey() + ".name");
    }


    @Override
    public MapColor getMapColor(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        return state.getValue(VARIANT).getMapColor();
    }


    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return state.getValue(VARIANT) == BlockStone.EnumType.STONE ? Item.getItemFromBlock(Blocks.COBBLESTONE) : Item.getItemFromBlock(Blocks.STONE);
    }


    @Override
    public int damageDropped(IBlockState state) {
        return state.getValue(VARIANT).getMetadata();
    }


    @Override
    public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
        for (BlockStone.EnumType blockstone$enumtype : BlockStone.EnumType.values()) {
            items.add(new ItemStack(this, 1, blockstone$enumtype.getMetadata()));
        }
    }


    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(VARIANT, BlockStone.EnumType.byMetadata(meta));
    }


    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(VARIANT).getMetadata();
    }


    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, VARIANT);
    }


    @Keep
    public enum EnumType implements IStringSerializable {
        STONE(0, MapColor.STONE, "stone", true),
        GRANITE(1, MapColor.DIRT, "granite", true),
        GRANITE_SMOOTH(2, MapColor.DIRT, "smooth_granite", "graniteSmooth", false),
        DIORITE(3, MapColor.QUARTZ, "diorite", true),
        DIORITE_SMOOTH(4, MapColor.QUARTZ, "smooth_diorite", "dioriteSmooth", false),
        ANDESITE(5, MapColor.STONE, "andesite", true),
        ANDESITE_SMOOTH(6, MapColor.STONE, "smooth_andesite", "andesiteSmooth", false);

        private static final BlockStone.EnumType[] META_LOOKUP = new BlockStone.EnumType[values().length];
        private final int meta;
        private final String name;
        private final String translationKey;
        private final MapColor mapColor;
        private final boolean isNatural;

        EnumType(int meta, MapColor color, String name, boolean natural) {
            this(meta, color, name, name, natural);
        }

        EnumType(int meta, MapColor color, String name, String translate, boolean natural) {
            this.meta = meta;
            this.name = name;
            this.translationKey = translate;
            this.mapColor = color;
            this.isNatural = natural;
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


        public static BlockStone.EnumType byMetadata(int meta) {
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


        public boolean isNatural() {
            return this.isNatural;
        }


        static {
            for (BlockStone.EnumType type : values()) {
                META_LOOKUP[type.getMetadata()] = type;
            }
        }
    }
}