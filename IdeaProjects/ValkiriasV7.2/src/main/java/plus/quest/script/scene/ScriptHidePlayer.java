package plus.quest.script.scene;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import plus.Main;
import plus.util.cfg.Sector;
import plus.quest.motion.BasicPos;
import plus.quest.QuestConfig;

import java.util.List;


public class ScriptHidePlayer implements UniversalScript{

    private final int time;

    public ScriptHidePlayer(Sector s, String path){
        if(s instanceof QuestConfig c){
            this.time = c.getSingleInt(path, ".time", 500);
        } else this.time = s.getOrSetInt(path+".time", 500);
    }


    @Override
    public void run(Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, time, 1, false));
            }
        }.runTaskLater(Main.instance, 0);
    }


    @Override
    public void run(List<Player> players, int uid, BasicPos pos) {
        PotionEffect effect = new PotionEffect(PotionEffectType.INVISIBILITY, time, 1, false);
        for(Player player:players){
            player.addPotionEffect(effect);
        }
    }
}
