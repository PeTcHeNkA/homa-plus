package plus.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import plus.SpMain;


public class SimpleBlock extends Block {
    public SimpleBlock(Material materialIn, String rName) {
        super(materialIn);
        setRegistryName(rName);
        setTranslationKey(rName);
        setCreativeTab(SpMain.SP_TAB);
        SpMain.proxy.addToRegObj(this);
    }
}