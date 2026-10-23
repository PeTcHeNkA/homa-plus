package plus.provider;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;


public abstract class SoundProvider {
    public abstract void playBgSound(Player player, String sound, String category, float pith);
    public abstract void playNormalSound(Player player, Location loc, String sound, String category, float volume,float pith);
    public abstract void StopSound(Player p, String sound, String category);

    public static final SoundProvider G = Define();
    private static SoundProvider Define(){
        if(Bukkit.getVersion().contains("17")){
            return new S1_17_1();
        }
        System.out.println("Better support not enabled. Bukkit version ="+Bukkit.getVersion());
        return new DefaultProvider();
    }
}


class DefaultProvider extends SoundProvider{

    @Override
    public void playBgSound(Player player, String sound, String category, float pith) {
        Location l = player.getLocation();
        l.setY(1000000000000000f);
        player.playSound(
                l, sound,
                SoundCategory.valueOf(category),
                10000000000000000000f, pith);
    }
    
    @Override
    public void playNormalSound(Player player, Location loc, String sound, String category, float volume,float pith) {
        player.playSound(loc,sound, SoundCategory.valueOf(category), volume, pith);
    }


    @Override
    public void StopSound(Player p, String sound, String category) {
        p.stopSound(sound);
    }
}