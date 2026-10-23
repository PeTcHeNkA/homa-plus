package plus;

import com.google.inject.Inject;
import com.velocitypowered.api.event.player.PlayerModInfoEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;
import plus.utl.Config;

@Plugin(
        id = "redir_player",
        name = "Redirect_player",
        version = BuildConstants.VERSION,
        dependencies = {@Dependency(id = "limboapi")}
)
public class Main {

    private static ProxyServer server;
    private final Logger l;
    private QuerryServer querryServer;

    @Inject
    public Main(ProxyServer server, Logger logger) {
        Main.server = server;
        this.l = logger;
    }

    public static ProxyServer getServer(){
        return server;
    }


    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        Config c = new Config("redirect_settings.st");
        querryServer = new QuerryServer(Integer.parseInt(c.getString("querry_port")));
        l.info("Starded.!");
    }

    @Subscribe //isOnlineMode()
    public void onPlayerModInfoEvent(PlayerModInfoEvent e){
        l.info(String.format("Player [%s] connectet with [%s] mods.", e.getPlayer(), e.getModInfo().getMods().size()));
    }
}