package plus.mythic.skill.Drop;

import io.lumine.xikage.mythicmobs.skills.SkillCaster;
import org.bukkit.inventory.ItemStack;
import plus.chest.LvlableSimpleChest;
import plus.util.cfg.Config;


public class LSDropChest extends LvlableSimpleChest implements Dropper{

    public LSDropChest(Config c, String name) {
        super(c, name);
    }

    @Override
    public ItemStack[] drop(SkillCaster caster) {
        return super.dropContent(caster.getLevel());
    }
}
