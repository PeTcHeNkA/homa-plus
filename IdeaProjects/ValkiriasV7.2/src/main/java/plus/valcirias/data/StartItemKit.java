package plus.valcirias.data;

import org.bukkit.inventory.ItemStack;

public class StartItemKit {
    public final ItemStack WEAPON;
    public final ItemStack NORMAL_SKILL;
    public final ItemStack HEIGHT_SKILL;

    public StartItemKit(ItemStack weapon, ItemStack normal_skill, ItemStack height_skill) {
        WEAPON = weapon;
        NORMAL_SKILL = normal_skill;
        HEIGHT_SKILL = height_skill;
    }
}