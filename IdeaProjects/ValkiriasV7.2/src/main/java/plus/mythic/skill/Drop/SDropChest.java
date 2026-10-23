package plus.mythic.skill.Drop;

import io.lumine.xikage.mythicmobs.skills.SkillCaster;
import org.bukkit.inventory.ItemStack;
import plus.chest.SimpleChest;
import plus.util.cfg.Config;

public class SDropChest extends SimpleChest implements Dropper{

    public SDropChest(Config c, String name) {
        super(c, name);
    }

    @Override
    public ItemStack[] drop(SkillCaster caster) {
        return super.dropContent();
    }
}