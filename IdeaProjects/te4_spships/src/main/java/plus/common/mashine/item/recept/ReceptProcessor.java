package plus.common.mashine.item.recept;

import net.minecraft.item.ItemStack;


public abstract class ReceptProcessor implements IReseptValidator{
    private final IItemValidator[] checkers;

    public ReceptProcessor(IItemValidator[] items) {
        this.checkers = items;
    }

    public abstract ItemStack calcResult(ReseptKey key);


    @Override
    public boolean valid(ReseptKey key) {
        if(key.getSize() != checkers.length)return false;
        ItemStack[] items = key.getItems();
        for(int i=0;i<checkers.length;i++){
            ItemStack temp = items[i];
            IItemValidator validator = checkers[i];
            if(validator.getCount()>temp.getCount() || !validator.isEqual(temp))return false;
        }
        return true;
    }


    public void spendItems(ReseptKey key, ICraftMatrix matrix){
        for(int i=0;i<checkers.length;i++){
            matrix.takeItem(i, checkers[i].getCount());
        }
    }
}