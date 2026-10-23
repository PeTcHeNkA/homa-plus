package plus.mythic.skill;

import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.ITargetedEntitySkill;
import io.lumine.xikage.mythicmobs.skills.SkillMechanic;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import plus.mythic.skill.Drop.Dropper;
import plus.Main;


public class SkillDropWorld extends SkillMechanic implements ITargetedEntitySkill {

    private final Dropper chest;

    public SkillDropWorld(MythicLineConfig mc, String name) {
        super("loot_box_w", mc);
        setAsyncSafe(false);

        this.chest = Dropper.createDropper(Main.instance.getOrCreateYandeReConfig("rpg_drops.yml"), name);
    }


    @Override
    public boolean castAtEntity(SkillMetadata data, AbstractEntity target) {
        Location loc = data.getCaster().getEntity().getBukkitEntity().getLocation();
        for(ItemStack item:chest.drop(data.getCaster())) {
            loc.getWorld().dropItem(loc, item);
        }
        return true;
    }
}