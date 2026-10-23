package plus;

import com.google.inject.Inject;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;


@Plugin(id = "no_cmd_2", name = "NoCmd2", version = "1.0") public class Disabler {
    @Inject public Disabler(ProxyServer s){s.getCommandManager().unregister("server");}
}