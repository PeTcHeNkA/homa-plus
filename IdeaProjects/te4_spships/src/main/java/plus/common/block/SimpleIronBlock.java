package plus.common.block;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

public class SimpleIronBlock extends SimpleBlock {
    public SimpleIronBlock(String rName) {
        super(Material.IRON, rName);
        setSoundType(SoundType.METAL);
        setHardness(4f);
        setResistance(7f);
    }
}