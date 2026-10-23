package plus.mythic.skill.Drop;

import io.lumine.xikage.mythicmobs.skills.SkillCaster;
import org.bukkit.inventory.ItemStack;
import plus.util.cfg.Config;

public interface Dropper {

    ItemStack[] drop(SkillCaster caster);

    static Dropper createDropper(Config c, String name){
        if(c.contains("loot_tables." + name + ".lvltable")){
            System.out.printf("Loaded lvlable for %s %n", name);
            return new LSDropChest(c, name);
        } else {
            System.out.printf("Loaded standart for %s %n", name);
            return new SDropChest(c, name);
        }
    }
}