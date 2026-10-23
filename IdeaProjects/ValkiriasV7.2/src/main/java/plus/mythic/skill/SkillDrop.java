package plus.mythic.skill;

import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.ITargetedEntitySkill;
import io.lumine.xikage.mythicmobs.skills.SkillMechanic;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import plus.mythic.skill.Drop.Dropper;
import plus.Main;


public class SkillDrop extends SkillMechanic implements ITargetedEntitySkill {


    protected final Dropper chest;


    public SkillDrop(MythicLineConfig mythicConfig, String name) {
        super("loot_box", mythicConfig);
        setAsyncSafe(false);

        this.chest = Dropper.createDropper(Main.instance.getOrCreateYandeReConfig("rpg_drops.yml"), name);
    }


    @Override
    public boolean castAtEntity(SkillMetadata data, AbstractEntity target) {
        if(target.getBukkitEntity() instanceof Player player) {
            //ItemStack[] a = chest.drop(data.getCaster());
            //System.out.println(a.length);

            //for (ItemStack item : a) {
            for (ItemStack item : chest.drop(data.getCaster())) {
                int slot = player.getInventory().firstEmpty();
                if (slot >= 0) {
                    player.getInventory().setItem(slot, item);
                } else {
                    player.getWorld().dropItem(player.getLocation(), item);
                }
            }
        }
        return true;
    }
}