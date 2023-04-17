package plus.common.mashine.lincker;

import net.minecraft.item.ItemStack;
import plus.common.mashine.item.TehBasicItem;
import plus.common.mashine.item.recept.IItemValidator;
import plus.common.mashine.item.recept.ReceptProcessor;
import plus.common.mashine.item.recept.ReseptKey;


public abstract class PowerReceptProcessor extends ReceptProcessor {
    public PowerReceptProcessor(IItemValidator... items) {
        super(items);
    }


    @Override
    public final ItemStack calcResult(ReseptKey key) {
        throw new UnsupportedOperationException();
    }


    public ItemStack calcResult(ReseptKey key, int shift, int power){
        byte count = 1;
        for(ItemStack item: key.getItems()){
            int proc = TehBasicItem.getTehProcess(item);
            if(proc>0){
                count++;
                power+=proc;
            }
        }
        if(count>1){
            power = power/count;
        }
        return onCalcResult(key, shift, power);
    }


    public abstract ItemStack onCalcResult(ReseptKey key, int shift, int power);
}