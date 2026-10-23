package plus.mythic.skill;

import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.ITargetedEntitySkill;
import io.lumine.xikage.mythicmobs.skills.SkillMechanic;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;
import org.bukkit.entity.Player;
import plus.chest.reward.RewardBox;
import plus.chest.reward.RewardRandomBox;
import plus.valcirias.Valkiria;
import plus.Main;


public class SkillDropReward extends SkillMechanic implements ITargetedEntitySkill {

    private final RewardBox b;

    public SkillDropReward(MythicLineConfig mc, String name){
        super("loot_box_rb", mc);
        b = new RewardRandomBox(Main.instance.getOrCreateYandeReConfig("rpg_drops.yml"),
                "rewards."+name, true
        );
    }


    @Override
    public boolean castAtEntity(SkillMetadata data, AbstractEntity target) {
        if(target.getBukkitEntity() instanceof Player player){
            Valkiria v = Main.floader.getValkiria(player);
            b.rewardS(v);
        }
        return true;
    }
}