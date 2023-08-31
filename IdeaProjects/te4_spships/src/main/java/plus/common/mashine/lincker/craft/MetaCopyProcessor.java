package plus.common.mashine.lincker.craft;

import net.minecraft.item.ItemStack;
import plus.common.mashine.item.recept.IItemValidator;
import plus.common.mashine.item.recept.ISimplePowerCraftable;
import plus.common.mashine.item.recept.ReseptKey;


public class MetaCopyProcessor extends SimplePowerReceptProcessor{
    private final int copy;

    public MetaCopyProcessor(ISimplePowerCraftable result, int copy, IItemValidator... items) {
        super(result, items);
        this.copy = copy;
    }


    @Override
    public ItemStack onCalcResult(ReseptKey key, int shift, int power) {
        int typeQuant = key.getItems()[copy].getMetadata();
        ItemStack result = super.onCalcResult(key, shift, power);
        result.setItemDamage(typeQuant);
        return result;
    }
}