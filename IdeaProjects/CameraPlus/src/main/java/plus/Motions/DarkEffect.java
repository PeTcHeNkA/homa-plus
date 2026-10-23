package plus.Motions;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class DarkEffect {

    private final int time;
    private final int power;

    public DarkEffect(int time, int power){
        this.time = time;
        this.power = power;
    }

    public void apply(Player player){
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, time, power, false));
    }
}