package plus.valcirias.data;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import plus.Main;
import plus.quest.DungeonQuestSession;
import plus.util.cfg.Config;
import plus.valcirias.BaseValkiria;
import plus.valcirias.Valkiria;


public class DungeonListeners implements Listener {

    public final String server;

    public DungeonListeners(){
        Config c = Main.instance.getOrCreateYandeReConfig("floader_settings.yml");
        server = c.getOrSetString("main.server_on_death", "main");
    }


    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e){
        Main.floader.bd.Redirect(e.getPlayer().getName(), server);
    }


    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e){
        Valkiria v = Main.floader.getValkiria(e.getPlayer());
        DungeonQuestSession s = (DungeonQuestSession) v.getQuestSession();
        if(s.getSelector()!=null) {
            s.getSelector().remove(v.toPlayerName());
        }
        if(v.getState()!=BaseValkiria.VState.REDIRECTING) Main.floader.clearValkiriaCacheOf(e.getPlayer());
    }
}