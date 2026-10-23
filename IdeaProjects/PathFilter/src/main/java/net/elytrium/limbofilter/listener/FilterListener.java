//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package net.elytrium.limbofilter.listener;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.event.connection.PreLoginEvent.PreLoginComponentResult;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.event.query.ProxyQueryEvent;
import com.velocitypowered.api.proxy.Player;
import net.elytrium.limboapi.api.event.LoginLimboRegisterEvent;
import net.elytrium.limbofilter.LimboFilter;
import net.elytrium.limbofilter.Settings;

public class FilterListener {
    private final LimboFilter plugin;

    public FilterListener(LimboFilter plugin) {
        this.plugin = plugin;
    }

    @Subscribe(
            order = PostOrder.FIRST
    )
    public void onProxyConnect(PreLoginEvent event) {
        this.plugin.getStatistics().addConnection();
        if (this.plugin.checkCpsLimit(Settings.IMP.MAIN.FILTER_AUTO_TOGGLE.ONLINE_MODE_VERIFY) && this.plugin.shouldCheck(event.getUsername(), event.getConnection().getRemoteAddress().getAddress())) {
            event.setResult(PreLoginComponentResult.forceOfflineMode());
        }

    }

    @Subscribe(
            order = PostOrder.FIRST
    )
    public void onLogin(LoginLimboRegisterEvent event) {
        Player player = event.getPlayer();
        if (this.plugin.shouldCheck(player)) {
            event.addCallback(() -> {
                this.plugin.sendToFilterServer(player);
            });
        }

    }

    @Subscribe(
            order = PostOrder.LAST
    )
    public void onPing(ProxyPingEvent event) {
        if (this.plugin.checkPpsLimit(Settings.IMP.MAIN.FILTER_AUTO_TOGGLE.DISABLE_MOTD_PICTURE)) {
            event.setPing(event.getPing().asBuilder().clearFavicon().build());
        }

        this.plugin.getStatistics().addPing();
    }

    @Subscribe
    public void onQuery(ProxyQueryEvent event) {
        this.plugin.getStatistics().addConnection();
    }
}