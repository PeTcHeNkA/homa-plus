package plus.quest.script.scene;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import plus.Main;
import plus.quest.motion.BasicPos;
import java.util.Collection;


public class ScriptDarkEffect implements Script{

    private final int time;
    private final int power;

    public ScriptDarkEffect(int time, int power){
        this.time = time;
        this.power = power;
    }


    @Override
    public void run(Player player, int uid, BasicPos pos) {
        new BukkitRunnable() {
            @Override
            public void run() {
                player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, time, power, false));
            }
        }.runTaskLater(Main.instance, 0);
    }


    @Override
    public void run(Collection<Player> players, int uid, BasicPos pos) {
        PotionEffect effect = new PotionEffect(PotionEffectType.BLINDNESS, time, power, false);
        for(Player player:players){
            player.addPotionEffect(effect);
        }
    }
}