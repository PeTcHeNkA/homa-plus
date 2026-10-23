//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package net.elytrium.limboauth.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.event.connection.PreLoginEvent.PreLoginComponentResult;
import com.velocitypowered.api.util.UuidUtils;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import net.elytrium.limboapi.api.event.LoginLimboRegisterEvent;
import net.elytrium.limboapi.api.event.SafeGameProfileRequestEvent;
import net.elytrium.limboauth.LimboAuth;
import net.elytrium.limboauth.Settings;
import net.elytrium.limboauth.handler.AuthSessionHandler;
import net.elytrium.limboauth.model.RegisteredPlayer;
import net.elytrium.limboauth.thirdparty.com.j256.ormlite.dao.Dao;
import net.elytrium.limboauth.thirdparty.com.j256.ormlite.stmt.UpdateBuilder;


public class AuthListener {
    private final LimboAuth plugin;
    private final Dao<RegisteredPlayer, String> playerDao;

    public AuthListener(LimboAuth plugin, Dao<RegisteredPlayer, String> playerDao) {
        this.plugin = plugin;
        this.playerDao = playerDao;
    }

    @Subscribe
    public void onPreLoginEvent(PreLoginEvent event) {
        if (!event.getResult().isForceOfflineMode()) {
            if (!this.plugin.isPremium(event.getUsername())) {
                event.setResult(PreLoginComponentResult.forceOfflineMode());
            } else {
                event.setResult(PreLoginComponentResult.forceOnlineMode());
            }
        }

    }

    @Subscribe
    public void onPostLogin(PostLoginEvent event) {
        Map<UUID, Runnable> postLoginTasks = this.plugin.getPostLoginTasks();
        UUID uuid = event.getPlayer().getUniqueId();
        if (postLoginTasks.containsKey(uuid)) {
            this.plugin.getServer().getScheduler().buildTask(this.plugin, () -> {
                ((Runnable)postLoginTasks.get(uuid)).run();
            }).delay((long)Settings.IMP.MAIN.PREMIUM_AND_FLOODGATE_MESSAGES_DELAY, TimeUnit.MILLISECONDS).schedule();
        }

    }

    @Subscribe
    public void onLoginLimboRegister(LoginLimboRegisterEvent event) {
        event.addCallback(() -> {
            this.plugin.authPlayer(event.getPlayer());
        });
    }

    @Subscribe
    public void onGameProfileRequest(SafeGameProfileRequestEvent event) {
        if (Settings.IMP.MAIN.SAVE_UUID) {
            RegisteredPlayer registeredPlayer = AuthSessionHandler.fetchInfo(this.playerDao, event.getOriginalProfile().getId());
            if (registeredPlayer != null) {
                event.setGameProfile(event.getOriginalProfile().withId(UUID.fromString(registeredPlayer.getUuid())));
                return;
            }

            registeredPlayer = AuthSessionHandler.fetchInfo(this.playerDao, event.getUsername());
            if (registeredPlayer != null) {
                String currentUuid = registeredPlayer.getUuid();
                if (event.isOnlineMode() && registeredPlayer.getHash().isEmpty() && registeredPlayer.getPremiumUuid().isEmpty()) {
                    try {
                        registeredPlayer.setPremiumUuid(event.getOriginalProfile().getId().toString());
                        this.playerDao.update(registeredPlayer);
                    } catch (SQLException var7) {
                        var7.printStackTrace();
                    }
                }

                if (currentUuid.isEmpty()) {
                    try {
                        registeredPlayer.setUuid(event.getGameProfile().getId().toString());
                        this.playerDao.update(registeredPlayer);
                    } catch (SQLException var6) {
                        var6.printStackTrace();
                    }
                }

                event.setGameProfile(event.getOriginalProfile().withId(UUID.fromString(currentUuid)));
            }
        } else if (event.isOnlineMode()) {
            try {
                UpdateBuilder<RegisteredPlayer, String> updateBuilder = this.playerDao.updateBuilder();
                updateBuilder.where().eq("NICKNAME", event.getUsername());
                updateBuilder.updateColumnValue("HASH", "");
                updateBuilder.update();
            } catch (SQLException var5) {
                var5.printStackTrace();
            }
        }

        if (Settings.IMP.MAIN.FORCE_OFFLINE_UUID) {
            event.setGameProfile(event.getOriginalProfile().withId(UuidUtils.generateOfflinePlayerUuid(event.getUsername())));
        }

    }
}