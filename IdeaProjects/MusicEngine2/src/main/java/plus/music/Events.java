package plus.music;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import plus.Main;


public class Events implements Listener {

    private final int preStartDelay;

    public Events() {
        this.preStartDelay = Main.c.getOrSetInt("PRE_START_DELAY", 0);
    }


    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        if(preStartDelay>0) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    MusicSession.startNew(e.getPlayer());
                }
            }.runTaskLaterAsynchronously(Main.I, preStartDelay);
        } else {
            MusicSession.startNew(e.getPlayer());
        }
    }


    @EventHandler
    public void onLeave(PlayerQuitEvent e){
        MusicSession.stopFor(e.getPlayer());
    }
}