package plus.common.mashine.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;


/*
* Mark item as Capacitor - compatible
* */
public interface IItemCapacitor {
    static boolean isCapacitor(ItemStack itemStack) {
        return isCapacitor(itemStack.getItem());
    }


    static boolean isCapacitor(Item item) {
        return item instanceof IItemCapacitor;
    }
}