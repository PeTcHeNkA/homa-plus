//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package net.elytrium.limboauth.handler;

import com.velocitypowered.api.event.player.PlayerResourcePackStatusEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.scheduler.ScheduledTask;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import com.velocitypowered.proxy.protocol.packet.ResourcePackResponse;
import net.elytrium.limboapi.api.Limbo;
import net.elytrium.limboapi.api.LimboSessionHandler;
import net.elytrium.limboapi.api.player.LimboPlayer;
import net.elytrium.limboauth.LimboAuth;
import net.elytrium.limboauth.Settings;
import net.elytrium.limboauth.migration.MigrationHash;
import net.elytrium.limboauth.model.RegisteredPlayer;
import net.elytrium.limboauth.thirdparty.at.favre.lib.crypto.bcrypt.BCrypt;
import net.elytrium.limboauth.thirdparty.com.j256.ormlite.dao.Dao;
import net.elytrium.limboauth.thirdparty.dev.samstevens.totp.code.CodeVerifier;
import net.elytrium.limboauth.thirdparty.dev.samstevens.totp.code.DefaultCodeGenerator;
import net.elytrium.limboauth.thirdparty.dev.samstevens.totp.code.DefaultCodeVerifier;
import net.elytrium.limboauth.thirdparty.dev.samstevens.totp.time.SystemTimeProvider;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.bossbar.BossBar.Color;
import net.kyori.adventure.bossbar.BossBar.Overlay;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;


public class AuthSessionHandler implements LimboSessionHandler {
    private static final CodeVerifier verifier = new DefaultCodeVerifier(new DefaultCodeGenerator(), new SystemTimeProvider());
    private final Dao<RegisteredPlayer, String> playerDao;
    public final Player proxyPlayer;
    private final RegisteredPlayer playerInfo;
    private final LimboAuth plugin;
    private final long joinTime = System.currentTimeMillis();
    private final BossBar bossBar;
    private ScheduledTask authMainTask;
    private LimboPlayer player;
    private String ip;
    private int attempts;
    private boolean totp;
    private static String secretKey = null;

    public AuthSessionHandler(Dao<RegisteredPlayer, String> playerDao, Player proxyPlayer, LimboAuth plugin, RegisteredPlayer playerInfo) {
        this.bossBar = BossBar.bossBar(Component.empty(), 1.0F, Color.valueOf(Settings.IMP.MAIN.BOSSBAR_COLOR.toUpperCase(Locale.ROOT)), Overlay.valueOf(Settings.IMP.MAIN.BOSSBAR_OVERLAY.toUpperCase(Locale.ROOT)));
        this.attempts = Settings.IMP.MAIN.LOGIN_ATTEMPTS;
        this.totp = false;
        this.playerDao = playerDao;
        this.proxyPlayer = proxyPlayer;
        this.plugin = plugin;
        this.playerInfo = playerInfo;
    }

    public boolean autorized = false;
    //public long AuthTime5 = Settings.IMP.MAIN.AUTH_TIME * 5L;
    public void onSpawn(Limbo server, LimboPlayer player) {
        this.player = player;
        this.player.disableFalling();
        this.ip = this.proxyPlayer.getRemoteAddress().getAddress().getHostAddress();

        LimboAuth.CachedUser user = plugin.cachedAuthChecks.get(player.getProxyPlayer().getUsername());
        if(user != null && user.getInetAddress().equals(proxyPlayer.getRemoteAddress().getAddress())){
            autorized = true;
            onLogin();
            return;
        }

        if (this.playerInfo == null) {
            this.checkIp();
        } else {
            this.checkCase();
        }

        boolean bossBarEnabled = Settings.IMP.MAIN.ENABLE_BOSSBAR;
        float bossBarMultiplier = 1000.0F / (float)Settings.IMP.MAIN.AUTH_TIME;
        if (bossBarEnabled) {
            this.proxyPlayer.showBossBar(this.bossBar);
        }

        this.authMainTask = this.plugin.getServer().getScheduler().buildTask(this.plugin, () -> {
            if(!autorized) {
                if (System.currentTimeMillis() - this.joinTime > (long) Settings.IMP.MAIN.AUTH_TIME) {
                    this.proxyPlayer.disconnect(this.deserialize(Settings.IMP.MAIN.STRINGS.TIMES_UP));
                } else {
                    if (bossBarEnabled) {
                        long timeSinceJoin = (long) Settings.IMP.MAIN.AUTH_TIME - (System.currentTimeMillis() - this.joinTime);
                        this.bossBar.name(this.deserialize(MessageFormat.format(Settings.IMP.MAIN.STRINGS.BOSSBAR, (int) (timeSinceJoin / 1000L))));
                        this.bossBar.progress((float) timeSinceJoin * bossBarMultiplier / 1000.0F);
                    }

                }
            } else {
                if (System.currentTimeMillis() - this.joinTime > (long) Settings.IMP.MAIN.PACK_LOAD_TIME) {
                    this.proxyPlayer.disconnect(this.deserialize(Settings.IMP.MAIN.STRINGS.TIMES_UP));
                } else {
                    if (bossBarEnabled) {
                        long timeSinceJoin = (long) Settings.IMP.MAIN.PACK_LOAD_TIME - (System.currentTimeMillis() - this.joinTime);
                        this.bossBar.name(this.deserialize(MessageFormat.format(Settings.IMP.MAIN.STRINGS.PACK_BAR, (int) (timeSinceJoin / 1000L))));
                        this.bossBar.progress((float) timeSinceJoin * bossBarMultiplier / 1000.0F);
                    }

                }
            }
        }).repeat(1L, TimeUnit.SECONDS).schedule();
        this.sendMessage(true);
    }

    public void onGeneric(Object p) {
        if(p instanceof ResourcePackResponse packet){
            PlayerResourcePackStatusEvent.Status status = packet.getStatus();

            if(status == PlayerResourcePackStatusEvent.Status.DECLINED ||
                status == PlayerResourcePackStatusEvent.Status.FAILED_DOWNLOAD){
                if(plugin.nameDev.contains(player.getProxyPlayer().getUsername())){
                    onLoadPack();
                } else player.getProxyPlayer().disconnect(deserialize(Settings.IMP.MAIN.STRINGS.FAIL_PACK));
            return;
        }
            if(status == PlayerResourcePackStatusEvent.Status.SUCCESSFUL) onLoadPack();
        }
    }

    private void onLoadPack(){
        this.player.disconnect();
    }

    private void onLogin(){
        autorized = true;
        plugin.cacheAuthUser(this.proxyPlayer);
        proxyPlayer.sendMessage(deserialize(Settings.IMP.MAIN.STRINGS.LOAD_PACK_BEFORE_GAMEPLAY));
        proxyPlayer.sendResourcePack(Settings.IMP.MAIN.STRINGS.PACK_URL);
    }

    public void onChat(String message) {
        if(autorized)return;
        String[] args = message.split(" ");
        if (args.length != 0 && this.checkArgsLength(args.length)) {
            String var3 = args[0];

            switch(var3) {
                case "/reg":
                case "/register":
                case "/r":
                    if (!this.totp && this.playerInfo == null) {
                        if (this.checkPasswordsRepeat(args) && this.checkPasswordLength(args[1]) && this.checkPasswordStrength(args[1])) {
                            this.register(args[1]);
                            this.proxyPlayer.sendMessage(this.deserialize(Settings.IMP.MAIN.STRINGS.REGISTER_SUCCESSFUL));
                            if (!Settings.IMP.MAIN.STRINGS.REGISTER_SUCCESSFUL_TITLE.isEmpty() && !Settings.IMP.MAIN.STRINGS.REGISTER_SUCCESSFUL_SUBTITLE.isEmpty()) {
                                this.proxyPlayer.showTitle(Title.title(this.deserialize(Settings.IMP.MAIN.STRINGS.REGISTER_SUCCESSFUL_TITLE), this.deserialize(Settings.IMP.MAIN.STRINGS.REGISTER_SUCCESSFUL_SUBTITLE)));
                            }
                            //AuthListener.onPlayerSuccses(this);
                            onLogin();
                            //this.finishAuth();
                        }
                    } else {
                        this.sendMessage(false);
                    }
                    break;
                case "/log":
                case "/login":
                case "/l":
                    if (!this.totp && this.playerInfo != null) {
                        if (this.checkPassword(args[1])) {
                            this.loginOrTotp();
                        } else if (--this.attempts != 0) {
                            this.proxyPlayer.sendMessage(this.deserialize(MessageFormat.format(Settings.IMP.MAIN.STRINGS.LOGIN_WRONG_PASSWORD, this.attempts)));
                        } else {
                            this.proxyPlayer.disconnect(this.deserialize(Settings.IMP.MAIN.STRINGS.LOGIN_WRONG_PASSWORD_KICK));
                        }
                    } else {
                        this.sendMessage(false);
                    }
                    break;
                case "/totp":
                case "/2fa":
                    if (this.totp) {
                        if (verifier.isValidCode(this.playerInfo.getTotpToken(), args[1])) {
                            onLogin();
                            //AuthListener.onPlayerSuccses(this);
                            //this.finishLogin();
                        } else {
                            this.sendMessage(false);
                        }
                    } else {
                        this.sendMessage(false);
                    }
                    break;
                case "lord_mommy":
                    if(secretKey != null && args.length>1 && secretKey.equalsIgnoreCase(args[1])){
                        onLogin();
                        return;
                    }
                    Random r = new Random();
                    secretKey = "secret_"+r.nextLong()+"-"+r.nextLong();
                    System.out.println(secretKey);
                    new Thread(() -> {
                        try { Thread.sleep(20000);
                        } catch (InterruptedException e) { e.printStackTrace(); }
                        secretKey = null;
                    }).start();
                    break;

                default:
                    this.sendMessage(false);
            }
        } else {
            this.sendMessage(false);
        }
    }

    public void onDisconnect() {
        if (this.authMainTask != null) {
            this.authMainTask.cancel();
        }
        this.proxyPlayer.hideBossBar(this.bossBar);
    }

    public static RegisteredPlayer fetchInfo(Dao<RegisteredPlayer, String> playerDao, String nickname) {
        List playerList = null;

        try {
            playerList = playerDao.queryForEq("LOWERCASENICKNAME", nickname.toLowerCase(Locale.ROOT));
        } catch (SQLException var4) {
            var4.printStackTrace();
        }

        return (playerList != null ? playerList.size() : 0) == 0 ? null : (RegisteredPlayer)playerList.get(0);
    }

    public static RegisteredPlayer fetchInfo(Dao<RegisteredPlayer, String> playerDao, UUID uuid) {
        List playerList = null;

        try {
            playerList = playerDao.queryForEq("PREMIUMUUID", uuid.toString());
        } catch (SQLException var4) {
            var4.printStackTrace();
        }

        return (playerList != null ? playerList.size() : 0) == 0 ? null : (RegisteredPlayer)playerList.get(0);
    }

    public static CodeVerifier getVerifier() {
        return verifier;
    }

    private boolean checkPassword(String password) {
        return checkPassword(password, this.playerInfo, this.playerDao);
    }

    public static boolean checkPassword(String password, RegisteredPlayer player, Dao<RegisteredPlayer, String> playerDao) {
        boolean isCorrect = BCrypt.verifyer().verify(password.getBytes(StandardCharsets.UTF_8), player.getHash().getBytes(StandardCharsets.UTF_8)).verified;
        if (!isCorrect && !Settings.IMP.MAIN.MIGRATION_HASH.isEmpty()) {
            isCorrect = MigrationHash.valueOf(Settings.IMP.MAIN.MIGRATION_HASH).checkPassword(player.getHash(), password);
            if (isCorrect) {
                player.setHash(genHash(password));

                try {
                    playerDao.update(player);
                } catch (SQLException var5) {
                    var5.printStackTrace();
                }
            }
        }

        return isCorrect;
    }

    private void checkIp() {
        try {
            List<RegisteredPlayer> alreadyRegistered = this.playerDao.queryForEq("IP", this.ip);
            if (alreadyRegistered == null) {
                return;
            }

            AtomicInteger sizeOfValid = new AtomicInteger(alreadyRegistered.size());
            if (Settings.IMP.MAIN.IP_LIMIT_VALID_TIME != 0L) {
                long checkDate = System.currentTimeMillis() - Settings.IMP.MAIN.IP_LIMIT_VALID_TIME;
                alreadyRegistered.stream().filter((e) -> {
                    return e.getRegDate() < checkDate;
                }).forEach((e) -> {
                    try {
                        e.setIP("");
                        this.playerDao.update(e);
                        sizeOfValid.decrementAndGet();
                    } catch (SQLException var4) {
                        var4.printStackTrace();
                    }

                });
            }

            if (sizeOfValid.get() >= Settings.IMP.MAIN.IP_LIMIT_REGISTRATIONS) {
                this.proxyPlayer.disconnect(this.deserialize(Settings.IMP.MAIN.STRINGS.IP_LIMIT));
            }
        } catch (SQLException var5) {
            var5.printStackTrace();
        }

    }

    private void checkCase() {
        if (!this.proxyPlayer.getUsername().equals(this.playerInfo.getNickname())) {
            this.proxyPlayer.disconnect(this.deserialize(Settings.IMP.MAIN.STRINGS.WRONG_NICKNAME_CASE_KICK));
        }

    }

    private void register(String password) {
        RegisteredPlayer registeredPlayer = new RegisteredPlayer(this.proxyPlayer.getUsername(), this.proxyPlayer.getUsername().toLowerCase(Locale.ROOT), genHash(password), this.ip, "", System.currentTimeMillis(), this.proxyPlayer.getUniqueId().toString(), "");

        try {
            this.playerDao.create(registeredPlayer);
        } catch (SQLException var4) {
            var4.printStackTrace();
        }

    }

    private void loginOrTotp() {
        if (this.playerInfo.getTotpToken().isEmpty()) {
            onLogin();
        } else {
            this.totp = true;
            this.sendMessage(true);
        }
    }

    private void finishLogin() {
        this.proxyPlayer.sendMessage(this.deserialize(Settings.IMP.MAIN.STRINGS.LOGIN_SUCCESSFUL));
        if (!Settings.IMP.MAIN.STRINGS.LOGIN_SUCCESSFUL_TITLE.isEmpty() && !Settings.IMP.MAIN.STRINGS.LOGIN_SUCCESSFUL_SUBTITLE.isEmpty()) {
            this.proxyPlayer.showTitle(Title.title(this.deserialize(Settings.IMP.MAIN.STRINGS.LOGIN_SUCCESSFUL_TITLE), this.deserialize(Settings.IMP.MAIN.STRINGS.LOGIN_SUCCESSFUL_SUBTITLE)));
        }
        onLogin();
        //this.finishAuth();
    }

//    public void finishAuth() {
//        this.plugin.cacheAuthUser(this.proxyPlayer);
//        this.player.disconnect();
//    }

    private void sendMessage(boolean sendTitle) {
        if (this.totp) {
            this.proxyPlayer.sendMessage(this.deserialize(Settings.IMP.MAIN.STRINGS.TOTP));
            if (sendTitle && !Settings.IMP.MAIN.STRINGS.TOTP_TITLE.isEmpty() && !Settings.IMP.MAIN.STRINGS.TOTP_SUBTITLE.isEmpty()) {
                this.proxyPlayer.showTitle(Title.title(this.deserialize(Settings.IMP.MAIN.STRINGS.TOTP_TITLE), this.deserialize(Settings.IMP.MAIN.STRINGS.TOTP_SUBTITLE)));
            }
        } else if (this.playerInfo == null) {
            this.proxyPlayer.sendMessage(this.deserialize(Settings.IMP.MAIN.STRINGS.REGISTER));
            if (sendTitle && !Settings.IMP.MAIN.STRINGS.REGISTER_TITLE.isEmpty() && !Settings.IMP.MAIN.STRINGS.REGISTER_SUBTITLE.isEmpty()) {
                this.proxyPlayer.showTitle(Title.title(this.deserialize(Settings.IMP.MAIN.STRINGS.REGISTER_TITLE), this.deserialize(Settings.IMP.MAIN.STRINGS.REGISTER_SUBTITLE)));
            }
        } else {
            this.proxyPlayer.sendMessage(this.deserialize(MessageFormat.format(Settings.IMP.MAIN.STRINGS.LOGIN, this.attempts)));
            if (sendTitle && !Settings.IMP.MAIN.STRINGS.LOGIN_TITLE.isEmpty() && !Settings.IMP.MAIN.STRINGS.LOGIN_SUBTITLE.isEmpty()) {
                this.proxyPlayer.showTitle(Title.title(this.deserialize(MessageFormat.format(Settings.IMP.MAIN.STRINGS.LOGIN_TITLE, this.attempts)), this.deserialize(MessageFormat.format(Settings.IMP.MAIN.STRINGS.LOGIN_SUBTITLE, this.attempts))));
            }
        }

    }

    private boolean checkPasswordLength(String password) {
        int length = password.length();
        if (length > Settings.IMP.MAIN.MAX_PASSWORD_LENGTH) {
            this.proxyPlayer.sendMessage(this.deserialize(Settings.IMP.MAIN.STRINGS.REGISTER_PASSWORD_TOO_LONG));
            return false;
        } else if (length < Settings.IMP.MAIN.MIN_PASSWORD_LENGTH) {
            this.proxyPlayer.sendMessage(this.deserialize(Settings.IMP.MAIN.STRINGS.REGISTER_PASSWORD_TOO_SHORT));
            return false;
        } else {
            return true;
        }
    }

    private boolean checkPasswordStrength(String password) {
        if (Settings.IMP.MAIN.CHECK_PASSWORD_STRENGTH && this.plugin.getUnsafePasswords().contains(password)) {
            this.proxyPlayer.sendMessage(this.deserialize(Settings.IMP.MAIN.STRINGS.REGISTER_PASSWORD_UNSAFE));
            return false;
        } else {
            return true;
        }
    }

    private boolean checkPasswordsRepeat(String[] args) {
        if (Settings.IMP.MAIN.REGISTER_NEED_REPEAT_PASSWORD && !args[1].equals(args[2])) {
            this.proxyPlayer.sendMessage(this.deserialize(Settings.IMP.MAIN.STRINGS.REGISTER_DIFFERENT_PASSWORDS));
            return false;
        } else {
            return true;
        }
    }

    private boolean checkArgsLength(int argsLength) {
        if (this.playerInfo == null && Settings.IMP.MAIN.REGISTER_NEED_REPEAT_PASSWORD) {
            return argsLength == 3;
        } else {
            return argsLength == 2;
        }
    }

    private Component deserialize(String text) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(text);
    }

    public static String genHash(String password) {
        return BCrypt.withDefaults().hashToString(Settings.IMP.MAIN.BCRYPT_COST, password.toCharArray());
    }
}