//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package net.elytrium.limboauth;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.elytrium.limboapi.api.Limbo;
import net.elytrium.limboapi.api.LimboFactory;
import net.elytrium.limboapi.api.chunk.Dimension;
import net.elytrium.limboapi.api.chunk.VirtualWorld;
import net.elytrium.limboapi.api.file.SchematicFile;
import net.elytrium.limboapi.thirdparty.org.bstats.charts.SimplePie;
import net.elytrium.limboapi.thirdparty.org.bstats.charts.SingleLineChart;
import net.elytrium.limboapi.thirdparty.org.bstats.velocity.Metrics;
import net.elytrium.limboapi.thirdparty.org.bstats.velocity.Metrics.Factory;
import net.elytrium.limboauth.Settings.DATABASE;
import net.elytrium.limboauth.Settings.MAIN.AUTH_COORDS;
import net.elytrium.limboauth.Settings.MAIN.WORLD_COORDS;
import net.elytrium.limboauth.command.ChangePasswordCommand;
import net.elytrium.limboauth.command.DestroySessionCommand;
import net.elytrium.limboauth.command.ForceChangePasswordCommand;
import net.elytrium.limboauth.command.ForceUnregisterCommand;
import net.elytrium.limboauth.command.LimboAuthCommand;
import net.elytrium.limboauth.command.PremiumCommand;
import net.elytrium.limboauth.command.TotpCommand;
import net.elytrium.limboauth.command.UnregisterCommand;
import net.elytrium.limboauth.floodgate.FloodgateApiHolder;
import net.elytrium.limboauth.handler.AuthSessionHandler;
import net.elytrium.limboauth.listener.AuthListener;
import net.elytrium.limboauth.model.RegisteredPlayer;
import net.elytrium.limboauth.thirdparty.com.j256.ormlite.dao.Dao;
import net.elytrium.limboauth.thirdparty.com.j256.ormlite.dao.DaoManager;
import net.elytrium.limboauth.thirdparty.com.j256.ormlite.field.FieldType;
import net.elytrium.limboauth.thirdparty.com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import net.elytrium.limboauth.thirdparty.com.j256.ormlite.stmt.QueryBuilder;
import net.elytrium.limboauth.thirdparty.com.j256.ormlite.table.TableUtils;
import net.elytrium.limboauth.thirdparty.com.mysql.cj.conf.url.SingleConnectionUrl;
import net.elytrium.limboauth.thirdparty.org.h2.Driver;
import net.elytrium.limboauth.thirdparty.org.h2.engine.Engine;
import net.elytrium.limboauth.utils.UpdatesChecker;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.slf4j.Logger;


@Plugin(
        id = "limboauth",
        name = "LimboAuth",
        version = "1.0.3",
        url = "https://elytrium.net/",
        authors = {"hevav", "mdxd44"},
        dependencies = {@Dependency(
                id = "limboapi"
        ), @Dependency(
                id = "floodgate",
                optional = true
        )}
)
public class LimboAuth {
    public final Map<String, LimboAuth.CachedUser> cachedAuthChecks = new ConcurrentHashMap();
    private final Map<UUID, Runnable> postLoginTasks = new ConcurrentHashMap();
    private final Set<String> unsafePasswords = new HashSet();
    private final HttpClient client = HttpClient.newHttpClient();
    private final ProxyServer server;
    private final Logger logger;
    private final Factory metricsFactory;
    private final Path dataDirectory;
    private final LimboFactory factory;
    private final FloodgateApiHolder floodgateApi;
    private Dao<RegisteredPlayer, String> playerDao;
    private Pattern nicknameValidationPattern;
    private Limbo authServer;
    public final HashSet<String> nameDev = new HashSet<>();

    @Inject
    public LimboAuth(ProxyServer server, Logger logger, Factory metricsFactory, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.metricsFactory = metricsFactory;
        this.dataDirectory = dataDirectory;
        this.factory = (LimboFactory)this.server.getPluginManager().getPlugin("limboapi").flatMap(PluginContainer::getInstance).orElseThrow();
        if (this.server.getPluginManager().getPlugin("floodgate").isPresent()) {
            this.floodgateApi = new FloodgateApiHolder();
        } else {
            this.floodgateApi = null;
        }
        logger.info("Te4hnoAuth Injected!");
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) throws Exception {
        Metrics metrics = this.metricsFactory.make(this, 13700);
        System.setProperty("com.j256.simplelogging.level", "ERROR");
        this.reload();
        metrics.addCustomChart(new SimplePie("floodgate_auth", () -> String.valueOf(Settings.IMP.MAIN.FLOODGATE_NEED_AUTH)));
        metrics.addCustomChart(new SimplePie("premium_auth", () -> String.valueOf(Settings.IMP.MAIN.ONLINE_MODE_NEED_AUTH)));
        metrics.addCustomChart(new SimplePie("db_type", () -> Settings.IMP.DATABASE.STORAGE_TYPE));
        metrics.addCustomChart(new SimplePie("load_world", () -> String.valueOf(Settings.IMP.MAIN.LOAD_WORLD)));
        metrics.addCustomChart(new SimplePie("totp_enabled", () -> String.valueOf(Settings.IMP.MAIN.ENABLE_TOTP)));
        metrics.addCustomChart(new SimplePie("dimension", () -> Settings.IMP.MAIN.DIMENSION));
        metrics.addCustomChart(new SimplePie("save_uuid", () -> String.valueOf(Settings.IMP.MAIN.SAVE_UUID)));
        metrics.addCustomChart(new SingleLineChart("registered_players", () -> Math.toIntExact(this.playerDao.countOf())));
        UpdatesChecker.checkForUpdates(this.getLogger());

        for(String nick:Settings.IMP.MAIN.STRINGS.DEVELOPERS.split(",")){
            nameDev.add(nick.strip());
        }
    }

    public void reload() throws Exception {
        Settings.IMP.reload(new File(this.dataDirectory.toFile().getAbsoluteFile(), "config.yml"));
        if (this.floodgateApi == null && !Settings.IMP.MAIN.FLOODGATE_NEED_AUTH) {
            throw new IllegalStateException("If you don't need to auth floodgate players please install floodgate plugin.");
        } else {
            if (Settings.IMP.MAIN.CHECK_PASSWORD_STRENGTH) {
                this.unsafePasswords.clear();
                Path unsafePasswordsPath = Paths.get(this.dataDirectory.toFile().getAbsolutePath(), Settings.IMP.MAIN.UNSAFE_PASSWORDS_FILE);
                if (!unsafePasswordsPath.toFile().exists()) {
                    Files.copy(Objects.requireNonNull(this.getClass().getResourceAsStream("/unsafe_passwords.txt")), unsafePasswordsPath, new CopyOption[0]);
                }

                this.unsafePasswords.addAll(Files.lines(unsafePasswordsPath).collect(Collectors.toSet()));
            }

            this.cachedAuthChecks.clear();
            DATABASE dbConfig = Settings.IMP.DATABASE;
            String var3 = dbConfig.STORAGE_TYPE.toLowerCase(Locale.ROOT);
            byte var4 = -1;
            switch(var3.hashCode()) {
                case -2105481388:
                    if (var3.equals("postgresql")) {
                        var4 = 2;
                    }
                    break;
                case 3274:
                    if (var3.equals("h2")) {
                        var4 = 0;
                    }
                    break;
                case 104382626:
                    if (var3.equals("mysql")) {
                        var4 = 1;
                    }
            }

            JdbcPooledConnectionSource connectionSource;
            switch(var4) {
                case 0:
                    Objects.requireNonNull(Driver.class);
                    Objects.requireNonNull(Engine.class);
                    connectionSource = new JdbcPooledConnectionSource("jdbc:h2:" + this.dataDirectory.toFile().getAbsoluteFile() + "/limboauth");
                    break;
                case 1:
                    Objects.requireNonNull(net.elytrium.limboauth.thirdparty.com.mysql.cj.jdbc.Driver.class);
                    Objects.requireNonNull(SingleConnectionUrl.class);
                    connectionSource = new JdbcPooledConnectionSource("jdbc:mysql://" + dbConfig.HOSTNAME + "/" + dbConfig.DATABASE + dbConfig.CONNECTION_PARAMETERS, dbConfig.USER, dbConfig.PASSWORD);
                    break;
                case 2:
                    Objects.requireNonNull(net.elytrium.limboauth.thirdparty.org.postgresql.Driver.class);
                    connectionSource = new JdbcPooledConnectionSource("jdbc:postgresql://" + dbConfig.HOSTNAME + "/" + dbConfig.DATABASE + dbConfig.CONNECTION_PARAMETERS, dbConfig.USER, dbConfig.PASSWORD);
                    break;
                default:
                    this.getLogger().error("WRONG DATABASE TYPE.");
                    this.server.shutdown();
                    return;
            }

            TableUtils.createTableIfNotExists(connectionSource, RegisteredPlayer.class);
            this.playerDao = DaoManager.createDao(connectionSource, RegisteredPlayer.class);
            this.nicknameValidationPattern = Pattern.compile(Settings.IMP.MAIN.ALLOWED_NICKNAME_REGEX);
            this.migrateDb(this.playerDao);
            CommandManager manager = this.server.getCommandManager();
            manager.unregister("unregister");
            manager.unregister("premium");
            manager.unregister("forceunregister");
            manager.unregister("changepassword");
            manager.unregister("forcechangepassword");
            manager.unregister("destroysession");
            manager.unregister("2fa");
            manager.unregister("limboauth");
            manager.register("unregister", new UnregisterCommand(this, this.playerDao), new String[]{"unreg"});
            manager.register("premium", new PremiumCommand(this, this.playerDao), new String[0]);
            manager.register("forceunregister", new ForceUnregisterCommand(this, this.server, this.playerDao), new String[]{"forceunreg"});
            manager.register("changepassword", new ChangePasswordCommand(this.playerDao), new String[]{"changepass"});
            manager.register("forcechangepassword", new ForceChangePasswordCommand(this.server, this.playerDao), new String[]{"forcechangepass"});
            manager.register("destroysession", new DestroySessionCommand(this), new String[0]);
            if (Settings.IMP.MAIN.ENABLE_TOTP) {
                manager.register("2fa", new TotpCommand(this.playerDao), new String[]{"totp"});
            }

            manager.register("limboauth", new LimboAuthCommand(this), new String[]{"la", "auth", "lauth"});
            AUTH_COORDS authCoords = Settings.IMP.MAIN.AUTH_COORDS;
            VirtualWorld authWorld = this.factory.createVirtualWorld(Dimension.valueOf(Settings.IMP.MAIN.DIMENSION), authCoords.X, authCoords.Y, authCoords.Z, (float)authCoords.YAW, (float)authCoords.PITCH);
            if (Settings.IMP.MAIN.LOAD_WORLD) {
                try {
                    Path path = this.dataDirectory.resolve(Settings.IMP.MAIN.WORLD_FILE_PATH);
                    String var8 = Settings.IMP.MAIN.WORLD_FILE_TYPE;
                    byte var9 = -1;
                    switch(var8.hashCode()) {
                        case -152986803:
                            if (var8.equals("schematic")) {
                                var9 = 0;
                            }
                        default:
                            switch(var9) {
                                case 0:
                                    SchematicFile file = new SchematicFile(path);
                                    WORLD_COORDS coords = Settings.IMP.MAIN.WORLD_COORDS;
                                    file.toWorld(this.factory, authWorld, coords.X, coords.Y, coords.Z);
                                    break;
                                default:
                                    this.getLogger().error("Incorrect world file type.");
                                    this.server.shutdown();
                                    return;
                            }
                    }
                } catch (IOException var10) {
                    var10.printStackTrace();
                }
            }

            this.authServer = this.factory.createLimbo(authWorld).setName("LimboAuth");
            this.server.getEventManager().unregisterListeners(this);
            this.server.getEventManager().register(this, new AuthListener(this, this.playerDao));
            //this.server.getEventManager().register(this, new Events());

            Executors.newScheduledThreadPool(1, (task) -> {
                return new Thread(task, "purge-cache");
            }).scheduleAtFixedRate(() -> {
                this.checkCache(this.cachedAuthChecks, Settings.IMP.MAIN.PURGE_CACHE_MILLIS);
            }, Settings.IMP.MAIN.PURGE_CACHE_MILLIS, Settings.IMP.MAIN.PURGE_CACHE_MILLIS, TimeUnit.MILLISECONDS);
        }
    }

    public void migrateDb(Dao<RegisteredPlayer, String> playerDao) {
        Set<FieldType> tables = new HashSet();
        Collections.addAll(tables, playerDao.getTableInfo().getFieldTypes());
        String var4 = Settings.IMP.DATABASE.STORAGE_TYPE;
        byte var5 = -1;
        switch(var4.hashCode()) {
            case -2105481388:
                if (var4.equals("postgresql")) {
                    var5 = 1;
                }
                break;
            case 3274:
                if (var4.equals("h2")) {
                    var5 = 0;
                }
                break;
            case 104382626:
                if (var4.equals("mysql")) {
                    var5 = 2;
                }
        }

        String findSql;
        switch (var5) {
            case 0 -> findSql = "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = '" + playerDao.getTableInfo().getTableName() + "';";
            case 1, 2 -> {
                String var10000 = Settings.IMP.DATABASE.DATABASE;
                findSql = "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = '" + var10000 + "' AND TABLE_NAME = '" + playerDao.getTableInfo().getTableName() + "';";
            }
            default -> {
                this.getLogger().error("WRONG DATABASE TYPE.");
                this.server.shutdown();
                return;
            }
        }

        try {
            playerDao.queryRaw(findSql, new String[0]).forEach((e) -> {
                tables.removeIf((q) -> q.getColumnName().equalsIgnoreCase(e[0]));
            });
            tables.forEach((t) -> {
                try {
                    String columnDefinition = t.getColumnDefinition();
                    StringBuilder builder = new StringBuilder("ALTER TABLE \"AUTH\" ADD ");
                    List<String> dummy = new ArrayList();
                    if (columnDefinition == null) {
                        playerDao.getConnectionSource().getDatabaseType().appendColumnArg(t.getTableName(), builder, t, dummy, dummy, dummy, dummy);
                    } else {
                        playerDao.getConnectionSource().getDatabaseType().appendEscapedEntityName(builder, t.getColumnName());
                        builder.append(" ").append(columnDefinition).append(" ");
                    }

                    playerDao.executeRawNoArgs(builder.toString());
                } catch (SQLException var9) {
                    var9.printStackTrace();
                }
            });
        } catch (SQLException var6) {
            var6.printStackTrace();
        }

    }

    public void cacheAuthUser(Player player) {
        String username = player.getUsername();
        this.cachedAuthChecks.remove(username);
        this.cachedAuthChecks.put(username, new LimboAuth.CachedUser(player.getRemoteAddress().getAddress(), System.currentTimeMillis()));
    }

    public void removePlayerFromCache(String username) {
        this.cachedAuthChecks.remove(username);
    }

    public boolean needAuth(Player player) {
//        String username = player.getUsername();
//        if (!this.cachedAuthChecks.containsKey(username)) {
//            return true;
//        } else {
//            return !((LimboAuth.CachedUser)this.cachedAuthChecks.get(username)).getInetAddress().equals(player.getRemoteAddress().getAddress());
//        }
        return true;
    }

    public void authPlayer(Player player) {
        String nickname = player.getUsername();
        if (!this.nicknameValidationPattern.matcher(nickname).matches()) {
            player.disconnect(LegacyComponentSerializer.legacyAmpersand().deserialize(Settings.IMP.MAIN.STRINGS.NICKNAME_INVALID_KICK));
        } else {
            RegisteredPlayer registeredPlayer = AuthSessionHandler.fetchInfo(this.playerDao, nickname);
//            boolean onlineMode = player.isOnlineMode();
//            if ((onlineMode || !Settings.IMP.MAIN.FLOODGATE_NEED_AUTH && this.floodgateApi.isFloodgatePlayer(player.getUniqueId())) && (registeredPlayer == null || registeredPlayer.getHash().isEmpty())) {
//                registeredPlayer = AuthSessionHandler.fetchInfo(this.playerDao, player.getUniqueId());
//                if (registeredPlayer == null || registeredPlayer.getHash().isEmpty()) {
//                    this.postLoginTasks.put(player.getUniqueId(), () -> {
//                        if (onlineMode) {
//                            if (!Settings.IMP.MAIN.STRINGS.LOGIN_PREMIUM.isEmpty()) {
//                                player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Settings.IMP.MAIN.STRINGS.LOGIN_PREMIUM));
//                            }
//
//                            if (!Settings.IMP.MAIN.STRINGS.LOGIN_PREMIUM_TITLE.isEmpty() && !Settings.IMP.MAIN.STRINGS.LOGIN_PREMIUM_SUBTITLE.isEmpty()) {
//                                player.showTitle(Title.title(LegacyComponentSerializer.legacyAmpersand().deserialize(Settings.IMP.MAIN.STRINGS.LOGIN_PREMIUM_TITLE), LegacyComponentSerializer.legacyAmpersand().deserialize(Settings.IMP.MAIN.STRINGS.LOGIN_PREMIUM_SUBTITLE)));
//                            }
//                        } else {
//                            if (!Settings.IMP.MAIN.STRINGS.LOGIN_FLOODGATE.isEmpty()) {
//                                player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Settings.IMP.MAIN.STRINGS.LOGIN_FLOODGATE));
//                            }
//
//                            if (!Settings.IMP.MAIN.STRINGS.LOGIN_FLOODGATE_TITLE.isEmpty() && !Settings.IMP.MAIN.STRINGS.LOGIN_FLOODGATE_SUBTITLE.isEmpty()) {
//                                player.showTitle(Title.title(LegacyComponentSerializer.legacyAmpersand().deserialize(Settings.IMP.MAIN.STRINGS.LOGIN_FLOODGATE_TITLE), LegacyComponentSerializer.legacyAmpersand().deserialize(Settings.IMP.MAIN.STRINGS.LOGIN_FLOODGATE_SUBTITLE)));
//                            }
//                        }
//
//                    });
//                    this.factory.passLoginLimbo(player);
//                    return;
//                }
//            }
            try {
                this.authServer.spawnPlayer(player, new AuthSessionHandler(this.playerDao, player, this, registeredPlayer));
            } catch (Throwable var6) {
                this.getLogger().error("Error", var6);
            }

        }
    }

    public boolean isPremiumExternal(String nickname) {
        try {
            return this.client.send(HttpRequest.newBuilder().uri(URI.create(String.format(Settings.IMP.MAIN.ISPREMIUM_AUTH_URL, URLEncoder.encode(nickname, StandardCharsets.UTF_8)))).build(), BodyHandlers.ofString()).statusCode() == 200;
        } catch (InterruptedException | IOException var3) {
            this.getLogger().error("Unable to authenticate with Mojang", var3);
            return true;
        }
    }

    public boolean isPremium(String nickname) {
        try {
            if (this.isPremiumExternal(nickname)) {
                QueryBuilder<RegisteredPlayer, String> premiumRegisteredQuery = this.playerDao.queryBuilder();
                premiumRegisteredQuery.where().eq("LOWERCASENICKNAME", nickname.toLowerCase(Locale.ROOT)).and().ne("HASH", "");
                premiumRegisteredQuery.setCountOf(true);
                QueryBuilder<RegisteredPlayer, String> premiumUnregisteredQuery = this.playerDao.queryBuilder();
                premiumUnregisteredQuery.where().eq("LOWERCASENICKNAME", nickname.toLowerCase(Locale.ROOT)).and().eq("HASH", "");
                premiumUnregisteredQuery.setCountOf(true);
                if (!Settings.IMP.MAIN.ONLINE_MODE_NEED_AUTH) {
                    return this.playerDao.countOf(premiumRegisteredQuery.prepare()) == 0L;
                } else {
                    return this.playerDao.countOf(premiumRegisteredQuery.prepare()) == 0L && this.playerDao.countOf(premiumUnregisteredQuery.prepare()) != 0L;
                }
            } else {
                return false;
            }
        } catch (SQLException var4) {
            this.getLogger().error("Unable to authenticate with Mojang", var4);
            return true;
        }
    }

    private void checkCache(Map<String, LimboAuth.CachedUser> userMap, long time) {
        Stream var10000 = userMap.entrySet().stream().filter((u) -> {
            return ((LimboAuth.CachedUser)u.getValue()).getCheckTime() + time <= System.currentTimeMillis();
        }).map(Entry::getKey);
        Objects.requireNonNull(userMap);
        var10000.forEach(userMap::remove);
    }

    public Set<String> getUnsafePasswords() {
        return this.unsafePasswords;
    }

    public Map<UUID, Runnable> getPostLoginTasks() {
        return this.postLoginTasks;
    }

    public Logger getLogger() {
        return this.logger;
    }

    public ProxyServer getServer() {
        return this.server;
    }

    public static class CachedUser {
        private final InetAddress inetAddress;
        private final long checkTime;

        public CachedUser(InetAddress inetAddress, long checkTime) {
            this.inetAddress = inetAddress;
            this.checkTime = checkTime;
        }

        public InetAddress getInetAddress() {
            return this.inetAddress;
        }

        public long getCheckTime() {
            return this.checkTime;
        }
    }
}