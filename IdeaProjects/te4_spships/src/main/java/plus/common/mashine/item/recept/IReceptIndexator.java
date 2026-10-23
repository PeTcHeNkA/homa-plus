package plus.common.mashine.item.recept;

import net.minecraft.item.ItemStack;


public interface IReceptIndexator {
    int calcHash(ItemStack[] items);
    int nextReceptId();
}