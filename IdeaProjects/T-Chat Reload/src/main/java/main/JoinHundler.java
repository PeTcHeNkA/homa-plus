package main;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinHundler implements Listener {
    @EventHandler
    public void onPlayerConnect(PlayerJoinEvent event){
        //event.getPlayer();
        if(!Floader.hasUser(event.getPlayer())) {
            Floader.addUser(event.getPlayer().getName());
        }

    }

}
