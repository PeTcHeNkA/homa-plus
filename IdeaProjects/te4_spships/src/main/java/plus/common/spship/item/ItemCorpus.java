package plus.common.spship.item;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.common.Strings;
import plus.common.mashine.item.TehBasicItem;
import plus.utls.bin.BinNum;
import java.util.List;
import static plus.common.spship.item.ItemModule.addSPM;


public class ItemCorpus extends TehBasicItem {// 1, 16
    public static ItemCorpus SP_MODEL = new ItemCorpus("spship_model");

    protected ItemCorpus(String name) {
        super(name);
    }


    public ItemStack createSimple(int modelId){
        ItemStack item = new ItemStack(this);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger(Strings.CMD, modelId);
        item.setTagCompound(tag);
        return item;
    }


    public static int getColor(ItemStack item){
        NBTTagCompound tag = item.getTagCompound();
        if(tag==null || !tag.hasKey(Strings.CMD))return 0;
        return tag.getInteger(Strings.CMD);
    }


    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack item, World worldIn, List<String> lore,
                               ITooltipFlag flagIn) {
        NBTTagCompound tags = item.getTagCompound();

        if(tags!=null) {
            int baseLvl = tags.getInteger(Strings.SP_REACT_LVL);
            if (isMoreInfo()) {
                addSPM(item, tags, lore);
                if(baseLvl>0) addLore(lore, "teh_lvl", baseLvl);
            } else {
                if(baseLvl>0) addLore(lore, "teh_lvl", baseLvl);
                int type = scanType(item);
                addLore(lore, "spship_model_a" + type);
                addLore(lore, "spship_model_b" + type);
                addLore(lore, "more_inf");
            }
        }
    }


    @SideOnly(Side.CLIENT)
    private int scanType(ItemStack item){
        return getColor(item)>>4;
    }


    public static final class Provider extends BinNum.Provider{
        private final NBTTagCompound tag;
        private final ItemStack item;
        private final int[] patern;
        private final int shift;

        public Provider(ItemStack item, int shift, int... patern){
            this.patern = BinNum.genPatern(patern);
            this.shift = shift;
            this.item = item;
            NBTTagCompound tag0 = item.getTagCompound();
            if(tag0==null){
                tag0 = new NBTTagCompound();
                item.setTagCompound(tag0);
            }
            tag = tag0;
        }


        public ItemCorpus.Provider setParam(int i, int val){
            setVal(i, val);
            return this;
        }




        public ItemStack getItem(){
            return item;
        }


        @Override
        protected int getV() {
            return tag.getInteger(Strings.CMD)-shift;
        }


        @Override
        protected void setV(int v) {
            tag.setInteger(Strings.CMD, v+shift);
        }


        @Override
        protected int[] patern() {
            return patern;
        }
    }
}