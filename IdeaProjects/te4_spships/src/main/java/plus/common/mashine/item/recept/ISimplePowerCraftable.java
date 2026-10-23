package plus.common.mashine.item.recept;

import net.minecraft.item.ItemStack;


public interface ISimplePowerCraftable {
    ItemStack create(int shift, int tehProcess);
    ItemStack create(int shift, float power, float damage, int tehProcess);
}