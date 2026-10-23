package plus.valcirias.data.coder;

import org.bukkit.inventory.ItemStack;


public interface AbstractItemCoder {
    ItemStack encode(String value);
    String code(ItemStack itemStack);
}
