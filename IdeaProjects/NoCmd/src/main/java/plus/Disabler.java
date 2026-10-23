package plus;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;


@Plugin(id = "cmd_disabler", name = "cmd_disabler", version = "1.0")
public class Disabler {

    private ProxyServer server;
    @Inject public Disabler(ProxyServer server){this.server = server;}

    @Subscribe
    public void disableFuckingCommand(ProxyInitializeEvent e){
        server.getCommandManager().unregister("server"); server = null;
    }
}