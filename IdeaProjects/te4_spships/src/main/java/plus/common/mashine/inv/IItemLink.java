package plus.common.mashine.inv;

import net.minecraft.item.ItemStack;


public interface IItemLink {
    ItemStack getItem();
    void setItem(ItemStack item);
}