package plus.craft;

import org.bukkit.inventory.ItemStack;


public interface CraftThing {

    ItemStack toItem();

    boolean isSuccessful(ItemStack item);

    ItemStack view();
}