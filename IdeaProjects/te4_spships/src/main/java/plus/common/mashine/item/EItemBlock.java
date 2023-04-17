package plus.common.mashine.item;

import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.SpMain;

import java.util.List;


public class EItemBlock extends ItemBlock{
    @SideOnly(Side.CLIENT)
    private int countLore = 3;

    public EItemBlock(Block block) {
        super(block);
        ResourceLocation rn = block.getRegistryName();
        setRegistryName(rn);
        setTranslationKey(rn.getPath());
        setCreativeTab(SpMain.SP_TAB);

//        if(!SpMain.proxy.preReg(this)){
//            throw new RuntimeException();
//        }
    }


    public EItemBlock(Block block, int countLore){
        this(block);
        this.countLore = countLore+1;
    }


    @SideOnly(Side.CLIENT)
    protected static void addLore(List<String> lore, String s, Object... args){
        lore.add(I18n.translateToLocalFormatted(s, args));
    }


    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack item, World world, List<String> lore,
                               ITooltipFlag flagIn) {
        String rn = getTranslationKey()+".lore_";
        for(int i=1;i<countLore;i++){
            addLore(lore, rn+i);
        }
    }
}
