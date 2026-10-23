package plus.quest.script.system;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.mobs.MobManager;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;
import plus.Main;
import plus.quest.script.quest.trigger.BlockTrigger;
import plus.util.cfg.Sector;


public class ScriptMMSpawn implements SystemScript {

    private final Location[] locations;
    private final String mobType;
    private final int lvl;

    public ScriptMMSpawn(Sector s, String path){
        locations = BlockTrigger.parseLocations(s, path);
        mobType = s.getOrSetString(path+".mob_type", "example");
        lvl = s.getOrSetInt(path+".mob_lvl", 1);
    }


    @Override
    public void run() {
        MobManager manager = MythicMobs.inst().getMobManager();
        new BukkitRunnable() {
            @Override
            public void run() {
                for(Location loc:locations) {
                    manager.spawnMob(mobType, loc, lvl);
                }
            }
        }.runTaskLater(Main.instance, 0);
    }
}