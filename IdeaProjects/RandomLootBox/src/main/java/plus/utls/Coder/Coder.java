package plus.utls.Coder;

import org.bukkit.inventory.ItemStack;

public interface Coder extends Writer{
    String codeItem(ItemStack item);

    ItemStack decodeItem(String code);
}
