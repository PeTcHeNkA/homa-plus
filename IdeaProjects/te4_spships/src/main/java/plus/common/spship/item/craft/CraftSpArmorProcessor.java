package plus.common.spship.item.craft;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import plus.common.mashine.item.recept.IItemValidator;
import plus.common.mashine.item.recept.ReseptKey;
import plus.common.mashine.lincker.PowerReceptProcessor;
import plus.common.spship.item.ItemSpArmor;


public class CraftSpArmorProcessor extends PowerReceptProcessor {
    private static final int[] colorIndexes =
            {7, 1, 4, 1, 5, 2, 5, 3, 7, 2, 4, 6, 5, 2, 1, 3};


    public CraftSpArmorProcessor(IItemValidator... validators){
        super(validators);
    }


    @Override
    public ItemStack onCalcResult(ReseptKey key, int shift, int power) {
        ItemStack item = key.getItems()[0];
        return ItemSpArmor.SP_ARMOR.create(shift, power, getColor(item));
    }


    private int getColor(ItemStack item){
        if(item.getItem() != Items.DYE)return 0;
        return colorIndexes[item.getItemDamage()];
    }
}
