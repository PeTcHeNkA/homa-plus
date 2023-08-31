package plus.common.mashine.lincker.craft;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import plus.common.Strings;
import plus.common.mashine.item.ReactComponent;
import plus.common.mashine.item.recept.IItemValidator;
import plus.common.mashine.item.recept.ReseptKey;
import plus.common.spship.SPM;
import plus.common.spship.item.ItemCorpus;


public abstract class AbcCraftCorpusProcessor extends PowerReceptProcessor {
    protected final ItemCorpus generator;

    public AbcCraftCorpusProcessor(ItemCorpus generator, IItemValidator... validators) {
        super(validators);
        this.generator = generator;
    }


    abstract ItemCorpus.Provider newProvider(ItemStack item);


    abstract void onPostCraft(ReseptKey key, ItemCorpus.Provider provider);


    @Override
    public ItemStack onCalcResult(ReseptKey key, int shift, int power) {
        ItemStack item = generator.create(shift, power);
        ReactComponent comp = (ReactComponent) key.getItems()[2].getItem();
        item.getTagCompound().setInteger(Strings.SP_REACT_LVL, comp.level);
        onPostCraft(key, newProvider(item));
        return onPostCraft(item);
    }


    @Override
    public ItemStack onPostCraft(ItemStack src) {
        SPM.write(src, SPM.createRandomSPM(Math.abs(ItemCorpus.getTehProcess(src))));
        return src;
    }

    protected int getArmorColor(ItemStack item){
        NBTTagCompound tag = item.getTagCompound();
        if(tag==null || !tag.hasKey(Strings.CMD))return 7;
        return tag.getInteger(Strings.CMD);
    }
}