package plus.common.spship.item.craft;

import net.minecraft.item.ItemStack;
import plus.common.mashine.item.recept.IItemValidator;
import plus.common.spship.item.ItemCorpus;


public class CraftCRavenProcessor extends CraftCHunterProcessor{
    public CraftCRavenProcessor(IItemValidator... validators) {
        super(validators);
    }


    @Override
    ItemCorpus.Provider newProvider(ItemStack item) {
        return new ItemCorpus.Provider(item, 0, 2, 2);
    }


    @Override
    protected int getPrefC(int v){
        switch (v){
            case 3:
            case 5:
                return 3;
            case 4:return 2;
            case 6:return 1;
            default:return 0;
        }
    }
}