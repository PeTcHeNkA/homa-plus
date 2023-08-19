package plus.common.spship.item;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import plus.common.Strings;
import plus.common.mashine.item.TehBasicItem;
import plus.utls.bin.BinNum;


public class ItemCorpus extends TehBasicItem {// 1, 16
    public static ItemCorpus SP_MODEL = new ItemCorpus("spship_model", 16, 2, 2);


    private final Provider provider;
    protected ItemCorpus(String name, int shift, int... patern) {
        super(name);
        this.provider = new Provider(shift, patern);
    }


    public ItemStack createSimple(int modelId){
        ItemStack item = new ItemStack(this);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger(Strings.CMD, modelId);
        item.setTagCompound(tag);
        return item;
    }


    //не поотокобезопасно
    public Provider edit(ItemStack item){
        provider.setItem(item);
        return provider;
    }


    public static int getColor(ItemStack item){
        NBTTagCompound tag = item.getTagCompound();
        if(tag==null || !tag.hasKey(Strings.CMD))return 0;
        return tag.getInteger(Strings.CMD);
    }


    public static final class Provider extends BinNum.Provider{
        private ItemStack item = ItemStack.EMPTY;
        private NBTTagCompound tag = null;

        private final int[] patern;
        private final int shift;

        public Provider(int shift, int... patern){
            this.patern = BinNum.genPatern(patern);
            this.shift = shift;
        }


        public ItemCorpus.Provider setParam(int i, int val){
            setVal(i, val);
            return this;
        }


        public void setItem(ItemStack item){
            this.item = item;
            tag = item.getTagCompound();
            if(tag==null){
                tag = new NBTTagCompound();
                item.setTagCompound(tag);
            }
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