package plus.quest.script.system;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import plus.Main;
import plus.quest.script.quest.selector.Selector;
import plus.util.cfg.Sector;


public class ScriptClearMobs implements SystemScript {

    private final Selector<LivingEntity> selector;

    public ScriptClearMobs(Sector s, String path){
        selector = new Selector<LivingEntity>()
                .build(LivingEntity.class, s.getOrSetString(path+".selector",
                        "region:world:10;10;10;20;20;20; / range:world;10;10;10:10"));
    }


    @Override
    public void run() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for(LivingEntity e: selector.select()){
                    if(!(e instanceof Player))e.remove();
                }
            }
        }.runTaskLater(Main.instance, 0);
    }
}