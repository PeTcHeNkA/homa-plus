package plus.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import plus.tson.TsonMap;
import plus.tson.TsonSerelizable;
import static plus.block.Block.*;


public class BlockPane extends net.minecraft.block.BlockPane implements TsonSerelizable {
    private final boolean isOpaqueCube;
    public BlockPane(String material, String id, String name, Boolean isOpaqueCube) {
        super(parseMaterial(material), true);
        setRegistryName(id+"_pane");
        setTranslationKey(name+"_pane");
        setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
        this.isOpaqueCube = isOpaqueCube;

        if(!isOpaqueCube) {
            setLightOpacity(0);
            setLightLevel(0);
        }
        setCreativeTab(TE4BLOCKS_TAB);
    }


    public BlockPane(TsonMap map){
        super(parseMaterial(getMaterialOfMap(map)), true);
        isOpaqueCube = Block.isOpaqueCube(map);
        onBlockInitFromMap(map, this);
        setCreativeTab(TE4BLOCKS_TAB);
    }


    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return isOpaqueCube;
    }


    @Override
    public TsonMap toTson() {
        return genDefaultMap(this);
    }
}