package plus.tjo;

import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import java.util.HashMap;


public class EventFilter extends HashMap<String,String> {
    private final int minCanUse;

    public EventFilter(int minCanUse) {
        this.minCanUse = minCanUse;
    }


    public boolean canUse(Player player, String cause){
        if(player.getStatistic(Statistic.PLAY_ONE_TICK)>minCanUse)return true;
        String reqPerm;
        if((reqPerm = super.get(cause)) == null)return false;
        return player.hasPermission(reqPerm);
    }
}