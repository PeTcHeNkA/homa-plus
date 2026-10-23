package plus.versions;

import org.bukkit.inventory.ItemStack;

public interface AbstractItemGenerator {
    ItemStack writeTag(ItemStack item, String tag, String value);
    String getTag(ItemStack item, String tag);
    boolean hasTag(ItemStack item, String tag);
}