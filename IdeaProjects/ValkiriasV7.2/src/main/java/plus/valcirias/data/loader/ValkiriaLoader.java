package plus.valcirias.data.loader;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import plus.util.YandeRePlugin;

public class ValkiriaLoader implements Listener {

    private final AbstractValkiriaLoader l;
    public ValkiriaLoader(YandeRePlugin p, AbstractValkiriaLoader loader){
        this.l = loader;
        Bukkit.getPluginManager().registerEvents(this, p);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        System.out.printf("Start loading data for [%s]%n", e.getPlayer().getName());
        l.loadValkiria(e.getPlayer());
    }
}