package plus.regions;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import plus.music.MCategory;


public interface RegionProvider {

    static RegionProvider getDefault(){
        return DefaultProvider.I;
    }


    static void setDefault(RegionProvider provider){
        DefaultProvider.I = provider;
    }


    MCategory getCategory(Player player);


    default MCategory getCategory(String player){
        return getCategory(Bukkit.getPlayer(player));
    }
}

class DefaultProvider{
    protected static RegionProvider I = (Player player) -> MCategory.valueOf(0);
}