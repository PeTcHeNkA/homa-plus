package plus.fserver.utls.item;

import net.minecraft.item.ItemStack;
import plus.fserver.utls.collections.FixedItemList;


public class ItemStackUtils {
    public static ItemStack getAndRemove(FixedItemList stacks, int index) {
        return index >= 0 && index < stacks.size() ? stacks.set(index, ItemStack.EMPTY) : ItemStack.EMPTY;
    }


    public static ItemStack getAndSplit(FixedItemList stacks, int index, int amount) {
        return index >= 0 && index < stacks.size() && !stacks.get(index).isEmpty() && amount > 0 ? stacks.get(index).splitStack(amount) : ItemStack.EMPTY;
    }
}