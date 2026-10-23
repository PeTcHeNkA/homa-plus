package plus.block;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import plus.tson.TsonMap;
import plus.tson.TsonSerelizable;
import static plus.block.Block.*;


public class BlockSlab extends net.minecraft.block.BlockSlab implements TsonSerelizable {
    private final boolean isOpaqueCube;

    public BlockSlab(String material, String id, String name, Boolean isOpaqueCube) {
        super(parseMaterial(material));
        this.isOpaqueCube = isOpaqueCube;
        id = id+"_slab";
        setRegistryName(id);
        setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
        setTranslationKey(name);

        if(!isOpaqueCube) {
            setLightOpacity(0);
            setLightLevel(0);
        }
        setCreativeTab(TE4BLOCKS_TAB);
    }


    public BlockSlab(TsonMap map){
        super(parseMaterial(getMaterialOfMap(map)));
        isOpaqueCube = Block.isOpaqueCube(map);
        onBlockInitFromMap(map, this);
        setCreativeTab(TE4BLOCKS_TAB);
    }


    @Override
    public TsonMap toTson() {
        return genDefaultMap(this);
    }


    @Override
    public String getTranslationKey(int i) {
        return super.getTranslationKey();
    }


    @Override
    public boolean isDouble() {
        return false;
    }


    @Override
    public IProperty<?> getVariantProperty() {
        return HALF;
    }


    @Override
    public Comparable<?> getTypeForItem(ItemStack itemStack) {
        return true;
    }


    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, HALF);
    }


    @Override
    public final IBlockState getStateFromMeta(int meta) {
        IBlockState blockState = this.blockState.getBaseState().withProperty(HALF, EnumBlockHalf.BOTTOM);
        if(!this.isDouble()) {
            blockState = blockState.withProperty(HALF, ((meta&8)!=0)?EnumBlockHalf.TOP:EnumBlockHalf.BOTTOM);
        }
        return blockState;
    }


    @Override
    public final int getMetaFromState(IBlockState state) {
        int meta = 0;
        if(!this.isDouble()&& state.getValue(HALF)==EnumBlockHalf.TOP) {
            meta |= 8;
        }
        return meta;
    }


    public boolean isFullCube(IBlockState state) {
        return false;
    }


    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return isOpaqueCube;
    }
}