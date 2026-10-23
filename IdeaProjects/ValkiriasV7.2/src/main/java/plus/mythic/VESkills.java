package plus.mythic;

import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMechanicLoadEvent;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.ITargetedEntitySkill;
import io.lumine.xikage.mythicmobs.skills.SkillMechanic;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import plus.util.Reloadable;
import plus.quest.Scene;


public class VESkills implements Listener {

    @EventHandler
    public void onMythicMechanicLoad(MythicMechanicLoadEvent event) {
        if (!event.getMechanicName().equalsIgnoreCase("ve_scene")) return;
        MythicLineConfig mc = event.getConfig();
        String name = mc.getString("s");
        if(name==null){
            System.out.println("Name of scene is null! Skipped.");
            return;
        }
        Scene scene = Scene.get(name);
        if(scene==null){
            System.out.println("Scene not founded! Skipped.");
            return;
        }
        event.register(new SceneSkill(mc, scene));
    }
}


class SceneSkill extends SkillMechanic implements ITargetedEntitySkill, Reloadable {

    private Scene scene;

    public SceneSkill(MythicLineConfig mlc, Scene scene) {
        super("ve_scene", mlc);
        this.scene = scene;
        Reloadable.add(this);
    }


    @Override
    public boolean castAtEntity(SkillMetadata skillMetadata, AbstractEntity abstractEntity) {
        return false;
    }


    @Override
    public void reload() {
        Scene s2 = Scene.get(scene.name);
        if(s2 == null){
            System.out.println(String.format("Scene %s not found in config! Used cached data.", scene.name));
            return;
        }
        scene = s2;
    }
}