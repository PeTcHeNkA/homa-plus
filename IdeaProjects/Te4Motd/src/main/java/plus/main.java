package plus;

import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;
import plus.motd.Pinger;


@Plugin(
        id = "te4hnomotd",
        name = "Te4hnomotd",
        version = BuildConstants.VERSION
)

public class main {

    public static ProxyServer server;
    private final Logger l;


    @Inject
    public main(ProxyServer server, Logger logger) {
        main.server = server;
        this.l = logger;
    }


    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent e) {

        server.getEventManager().register(this, new Pinger());
    }
}
