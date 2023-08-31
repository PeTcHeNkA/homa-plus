package plus.common.mashine.lincker.craft;

import net.minecraft.item.ItemStack;
import plus.common.mashine.item.recept.IItemValidator;
import plus.common.mashine.item.recept.ISimplePowerCraftable;
import plus.common.mashine.item.recept.ReseptKey;


public class SimplePowerReceptProcessor extends PowerReceptProcessor{
    private final ISimplePowerCraftable result;

    public SimplePowerReceptProcessor(ISimplePowerCraftable result, IItemValidator... items) {
        super(items);
        this.result = result;
    }


    @Override
    public ItemStack onCalcResult(ReseptKey key, int shift, int power) {
        return result.create(shift, power);
    }
}