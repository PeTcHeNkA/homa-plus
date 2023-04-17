package plus.common.spship.item.craft;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import plus.common.mashine.item.recept.IItemValidator;
import plus.common.mashine.item.recept.ReseptKey;
import plus.common.mashine.lincker.PowerReceptProcessor;
import plus.common.spship.item.ItemCorpus;


public abstract class AbcCraftCorpusProcessor extends PowerReceptProcessor {
    protected final ItemCorpus generator;

    public AbcCraftCorpusProcessor(ItemCorpus generator, IItemValidator... validators) {
        super(validators);
        this.generator = generator;
    }


    abstract void onPostCraft(ReseptKey key, ItemCorpus.Provider item);


    @Override
    public ItemStack onCalcResult(ReseptKey key, int shift, int power) {
        ItemStack item = generator.create(shift, power);
        onPostCraft(key, generator.edit(item));
        return item;
    }


//    protected int getPrefColor(int val, int... colors){
//        int col;
//        for(int i=colors.length-1;i>0;i--){
//            col = colors[i];
//            if(col==val)return col;
//        }
//        return colors[0];
//    }


//    protected int getArmorColor(ItemStack item, int... colors){
//        return getPrefColor(getArmorColor(item), colors);
//    }


    protected int getArmorColor(ItemStack item){
        NBTTagCompound tag = item.getTagCompound();
        if(tag==null || !tag.hasKey("cmd"))return 7;
        return tag.getInteger("cmd");
    }
}