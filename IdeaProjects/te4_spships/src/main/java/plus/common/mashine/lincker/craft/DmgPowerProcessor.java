package plus.common.mashine.lincker.craft;

import net.minecraft.item.ItemStack;
import plus.common.mashine.item.TehBasicItem;
import plus.common.mashine.item.recept.IItemValidator;
import plus.common.mashine.item.recept.ISimplePowerCraftable;
import plus.common.mashine.item.recept.ReseptKey;


public class DmgPowerProcessor extends PowerReceptProcessor{
    protected final ISimplePowerCraftable result;
    public DmgPowerProcessor(ISimplePowerCraftable item, IItemValidator... items) {
        super(items);
        this.result = item;
    }


    public ItemStack calcResult(ReseptKey key, int shift, int tehProc){
        int count = 1;
        float sumDmg = 0;
        float power = 0;

        for(ItemStack item: key.getItems()){
            int proc = TehBasicItem.getTehProcess(item);
            if(proc>0){
                count++;
                tehProc+=proc;
                sumDmg += TehBasicItem.calcDamageCoef(item);
                power += TehBasicItem.getPower(item);
            }
        }
        if(count>1){
            tehProc = tehProc/count;
            sumDmg = sumDmg/count;
        }
        return onCalcResult(key, shift, tehProc, power, sumDmg);
    }


    @Override
    protected final ItemStack onCalcResult(ReseptKey key, int shift, int power) {
        throw new UnsupportedOperationException();
    }


    protected ItemStack onCalcResult(ReseptKey key, int shift, int tehProc, float power, float damage) {
        return result.create(shift, power, damage, tehProc);
    }
}