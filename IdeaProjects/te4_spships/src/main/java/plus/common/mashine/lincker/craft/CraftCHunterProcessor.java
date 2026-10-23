package plus.common.mashine.lincker.craft;

import net.minecraft.item.ItemStack;
import plus.common.mashine.item.recept.IItemValidator;
import plus.common.mashine.item.recept.ReseptKey;
import plus.common.spship.item.ItemCorpus;


public class CraftCHunterProcessor extends AbcCraftCorpusProcessor{
    public CraftCHunterProcessor(IItemValidator... validators) {
        super(ItemCorpus.SP_MODEL, validators);
    }


    @Override
    ItemCorpus.Provider newProvider(ItemStack item) {
        return new ItemCorpus.Provider(item, 16, 2, 2);
    }


    @Override
    void onPostCraft(ReseptKey key, ItemCorpus.Provider item) {
        ItemStack[] items = key.getItems();
        int wingColor   = getArmorColor(items[1]);
        int corpusColor = getArmorColor(items[3]);

        item.setParam(0, getPrefC(wingColor)).setParam(1, getPrefC(corpusColor));
    }


    protected int getPrefC(int v){
        switch (v){
            case 4: return 0;
            case 1:
            case 6:
                return 1;
            case 3:
            case 7:
                return 2;

            default: return 3;
        }
    }
}