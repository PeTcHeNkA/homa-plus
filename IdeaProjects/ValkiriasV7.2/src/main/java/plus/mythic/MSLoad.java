package plus.mythic;

import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMechanicLoadEvent;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.SkillMechanic;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import plus.mythic.skill.SkillDrop;
import plus.mythic.skill.SkillDropReward;
import plus.mythic.skill.SkillDropRewardChest;
import plus.mythic.skill.SkillDropWorld;


public class MSLoad implements Listener {

    @EventHandler
    public void onMythicMechanicLoad(MythicMechanicLoadEvent event)	{
        if(!event.getMechanicName().equalsIgnoreCase("vw_drop"))	return;
        MythicLineConfig mc = event.getConfig();
        String name = mc.getString("l");

        if(name != null){
            event.register(ofType(mc, name));
        }
    }


    private static SkillMechanic ofType(MythicLineConfig mc, String name){
        String type = mc.getString("t");
        if(type==null)return new SkillDrop(mc, name);
        return switch (type) {
            case "world" -> new SkillDropWorld(mc, name);
            case "reward" -> new SkillDropReward(mc, name);
            case "reward_c" -> new SkillDropRewardChest(mc, name);
            default -> new SkillDrop(mc, name);
        };
    }
}















