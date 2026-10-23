package plus.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
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


    @Override
    public SimpleBlock setSoundType(SoundType sound) {
        super.setSoundType(sound);
        return this;
    }


    @Override
    public SimpleBlock setHardness(float hardness) {
        super.setHardness(hardness);
        return this;
    }


    @Override
    public SimpleBlock setResistance(float resistance) {
        super.setResistance(resistance);
        return this;
    }


    public SimpleBlock setHarvestLvl(String toolClass, int level){
        super.setHarvestLevel(toolClass, level);
        return this;
    }
}