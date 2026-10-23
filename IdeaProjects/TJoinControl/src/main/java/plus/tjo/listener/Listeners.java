package plus.tjo.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerEvent;
import plus.tjo.EventFilter;
import plus.tjo.TJoMain;


public class Listeners {
    private final EventFilter filter;

    public Listeners(TJoMain main){
        filter = new EventFilter(main.getConfig().getInt("min-time")*20*60);
    }


    private void onCommand(PlayerCommandPreprocessEvent event){
        if(event.isCancelled())return;
        Player player;
        if(!filter.canUse(player = event.getPlayer(), event.getMessage())) {
            event.setCancelled(true);
            player.send
        }
    }



}
