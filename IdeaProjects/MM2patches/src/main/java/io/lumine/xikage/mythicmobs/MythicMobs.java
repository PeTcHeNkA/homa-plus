//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package io.lumine.xikage.mythicmobs;

import io.lumine.xikage.mythicmobs.adapters.AbstractWorld;
import io.lumine.xikage.mythicmobs.adapters.ServerInterface;
import io.lumine.xikage.mythicmobs.adapters.SkillAdapter;
import io.lumine.xikage.mythicmobs.adapters.TaskManager;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitServer;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitSkillTriggers;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitWorld;
import io.lumine.xikage.mythicmobs.adapters.bukkit.listeners.ChunkListeners;
import io.lumine.xikage.mythicmobs.adapters.bukkit.listeners.EggListeners;
import io.lumine.xikage.mythicmobs.adapters.bukkit.listeners.PlayerListeners;
import io.lumine.xikage.mythicmobs.adapters.bukkit.listeners.ThreatTableListeners;
import io.lumine.xikage.mythicmobs.api.bukkit.BukkitAPIHelper;
import io.lumine.xikage.mythicmobs.clock.MythicMobsAsyncClock;
import io.lumine.xikage.mythicmobs.clock.MythicMobsClock;
import io.lumine.xikage.mythicmobs.clock.TimingsHandler;
import io.lumine.xikage.mythicmobs.commands.BaseCommand;
import io.lumine.xikage.mythicmobs.commands.mobs.SpawnCommand;
import io.lumine.xikage.mythicmobs.compatibility.CompatibilityManager;
import io.lumine.xikage.mythicmobs.drops.DropManager;
import io.lumine.xikage.mythicmobs.holograms.HologramManager;
import io.lumine.xikage.mythicmobs.io.ConfigManager;
import io.lumine.xikage.mythicmobs.items.ItemManager;
import io.lumine.xikage.mythicmobs.logging.MythicLogger;
import io.lumine.xikage.mythicmobs.logging.MythicLogger.DebugLevel;
import io.lumine.xikage.mythicmobs.metrics.MetricsLite;
import io.lumine.xikage.mythicmobs.metrics.bStats;
import io.lumine.xikage.mythicmobs.mobs.EntityManager;
import io.lumine.xikage.mythicmobs.mobs.MobManager;
import io.lumine.xikage.mythicmobs.players.PlayerManager;
import io.lumine.xikage.mythicmobs.skills.SkillManager;
import io.lumine.xikage.mythicmobs.skills.placeholders.PlaceholderManager;
import io.lumine.xikage.mythicmobs.skills.variables.Variable;
import io.lumine.xikage.mythicmobs.skills.variables.VariableManager;
import io.lumine.xikage.mythicmobs.skills.variables.VariableSerializer;
import io.lumine.xikage.mythicmobs.spawning.random.RandomSpawnerManager;
import io.lumine.xikage.mythicmobs.spawning.spawners.SpawnerManager;
import io.lumine.xikage.mythicmobs.util.Patterns;
import io.lumine.xikage.mythicmobs.util.jnbt.CompoundTag;
import io.lumine.xikage.mythicmobs.util.jnbt.NBTCompoundSerializer;
import io.lumine.xikage.mythicmobs.util.jnbt.NBTSerializer;
import io.lumine.xikage.mythicmobs.util.jnbt.Tag;
import io.lumine.xikage.mythicmobs.utils.Schedulers;
import io.lumine.xikage.mythicmobs.utils.chat.ColorString;
import io.lumine.xikage.mythicmobs.utils.gson.GsonProvider;
import io.lumine.xikage.mythicmobs.utils.holograms.BukkitHologramFactory;
import io.lumine.xikage.mythicmobs.utils.holograms.HologramFactory;
import io.lumine.xikage.mythicmobs.utils.logging.ConsoleColor;
import io.lumine.xikage.mythicmobs.utils.plugin.LuminePlugin;
import io.lumine.xikage.mythicmobs.utils.version.ServerVersion;
import io.lumine.xikage.mythicmobs.volatilecode.VolatileCodeDisabled;
import io.lumine.xikage.mythicmobs.volatilecode.VolatileCodeHandler;
import io.lumine.xikage.mythicmobs.worlds.WorldManager;
import java.io.IOException;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;

public class MythicMobs extends LuminePlugin {
    private static MythicMobs plugin;
    private ServerInterface server;
    private BaseCommand baseCommand;
    private ConfigManager configuration;
    private EntityManager entityManager;
    private RandomSpawnerManager randomSpawningManager;
    private MobManager mobManager;
    private PlayerManager playerManager;
    private WorldManager worldManager;
    private SkillManager skillManager;
    private DropManager dropManager;
    private ItemManager itemManager;
    private SpawnerManager spawnerManager;
    private VariableManager variableManager;
    private PlaceholderManager placeholderManager;
    private HologramManager hologramManager;
    private CompatibilityManager compatibility;
    private BukkitHologramFactory hologramProvider;
    private VolatileCodeHandler volatileCodeHandler;
    private TimingsHandler timingsHandler;
    private BukkitAPIHelper APIHelper;
    private Boolean isUpdateAvailable = false;
    private static boolean p = false;
    private static final Object volatilityHandler = new Object();

    public MythicMobs() {
    }

    public void load() {
        plugin = this;
    }

    public void enable() {
        this.server = new BukkitServer(this);
        TaskManager.initializeBukkit(this);
        SkillAdapter.initializeBukkit();
        MythicLogger.log("Loading {0} for {1} {2}...", new Object[]{this.getDescription().getName(), ServerVersion.isPaper() ? "Paper" : "Spigot", ServerVersion.get().toString()});
        if (ServerVersion.isPaper()) {
            MythicLogger.log("The server is running PaperSpigot; enabled PaperSpigot exclusive functionality");
        } else {
            MythicLogger.log("The server is running Spigot; disabled PaperSpigot exclusive functionality");
        }

        Patterns.CompilePatterns();
        GsonProvider.update((builder) -> {
            builder.registerTypeAdapter(AbstractWorld.class, GsonProvider.standard().getAdapter(BukkitWorld.class)).registerTypeAdapter(Tag.class, new NBTSerializer()).registerTypeAdapter(CompoundTag.class, new NBTCompoundSerializer()).registerTypeAdapter(Variable.class, new VariableSerializer());
        });
        this.bind(this.configuration = new ConfigManager(this));
        this.hologramProvider = new BukkitHologramFactory();
        this.provideService(HologramFactory.class, this.hologramProvider);
        this.volatileCodeHandler = this.getVolatileCodeHandler();
        this.compatibility = new CompatibilityManager(this);
        this.itemManager = new ItemManager(this);
        this.dropManager = new DropManager(this);
        this.mobManager = new MobManager(this);
        this.playerManager = new PlayerManager(this);
        this.worldManager = new WorldManager(this);
        this.skillManager = new SkillManager(this);
        this.spawnerManager = new SpawnerManager(this);
        this.randomSpawningManager = new RandomSpawnerManager(this);
        this.variableManager = new VariableManager(this);
        this.placeholderManager = new PlaceholderManager(this);
        this.hologramManager = new HologramManager(this, this.compatibility);
        this.getConfiguration().load();
        MythicLogger.log("MythicMobs configuration file loaded successfully.");
        this.timingsHandler = new TimingsHandler();
        this.entityManager = new EntityManager();
        Schedulers.sync().runRepeating(new MythicMobsClock(), 0L, (long)ConfigManager.ClockInterval).bindWith(this);
        Schedulers.async().runRepeating(new MythicMobsAsyncClock(), 0L, (long)ConfigManager.ClockInterval).bindWith(this);
        PluginManager manager = this.getServer().getPluginManager();
        //manager.registerEvents(this.entityManager, this);
        manager.registerEvents(new ChunkListeners(), this);
        manager.registerEvents(new BukkitSkillTriggers(this), this);
        manager.registerEvents(new ThreatTableListeners(), this);
        manager.registerEvents(new EggListeners(), this);
        manager.registerEvents(new PlayerListeners(), this);
        this.APIHelper = new BukkitAPIHelper();
        this.baseCommand = new BaseCommand(this);
        this.registerCommand("mythicmobs", this.baseCommand);
        this.registerCommand("spawnmob", new SpawnCommand(this));
        if (ConfigManager.AllowMetrics) {
            new bStats(this);

            try {
                new MetricsLite(this);
            } catch (IOException var3) {
                MythicLogger.debug(DebugLevel.INFO, "MetricsLite: Failed to submit MetricsLite stats.", new Object[0]);
            }
        }

        MythicLogger.log("" + ConsoleColor.GREEN + ConsoleColor.CHECK_MARK + " MythicMobs" + (isVolatile() ? " Premium" : "") + " v" + this.getVersion() + " (build " + this.getBuildNumber() + ") has been successfully loaded!" + ConsoleColor.RESET);
        inst().getMobManager().ScanWorld();
    }

    public void disable() {
        MythicLogger.log("Disabling Mythic Mobs...");
        this.getSpawnerManager().resetAndSaveAll();
        this.configuration.save();
        this.getMobManager().despawnAllMobs();
        this.entityManager = null;
        this.volatileCodeHandler = null;
        this.timingsHandler = null;
        MythicLogger.log("All active settings have been saved.");
        this.configuration.unload();
        this.compatibility.terminate();
    }

    public static MythicMobs inst() {
        return plugin;
    }

    public static final boolean p() {
        return p;
    }

    public ServerInterface server() {
        return this.server;
    }

    public VolatileCodeHandler getVolatileCodeHandler() {
        if (this.volatileCodeHandler != null) {
            return this.volatileCodeHandler;
        } else {
            VolatileCodeHandler VCH = new VolatileCodeDisabled();
            String packageName = Bukkit.getServer().getClass().getPackage().getName();
            String version = packageName.substring(packageName.lastIndexOf(46) + 1);
            if (version.equals("craftbukkit")) {
                version = "pre";
            }

            try {
                Class<?> clazz = Class.forName("io.lumine.xikage.mythicmobs.volatilecode.VolatileCodeEnabled_" + version);
                if (VolatileCodeHandler.class.isAssignableFrom(clazz)) {
                    VCH = (VolatileCodeHandler)clazz.getConstructor().newInstance();
                }
            } catch (ClassNotFoundException var5) {
                MythicLogger.error(ColorString.get("&6--====|||| &c&lMythicMobs &6||||====--"));
                MythicLogger.error("This version of MythicMobs is not fully compatible with your version of Bukkit.");
                MythicLogger.error("Some features may be limited or disabled until you use a compatible version.");
            } catch (Exception var6) {
                throw new RuntimeException("Unknown exception loading version handler. Volatile code has been disabled.", var6);
            }

            this.volatileCodeHandler = (VolatileCodeHandler)VCH;
            return (VolatileCodeHandler)VCH;
        }
    }

    public static final boolean isVolatile() {
        return true;
    }

    public String getVersion() {
        return this.getDescription().getVersion().split("-")[0];
    }

    public String getBuildNumber() {
        String[] split = this.getDescription().getVersion().split("-");
        if (split.length == 2) {
            return split[1];
        } else {
            return split.length == 3 ? split[2] : "????";
        }
    }

    public BaseCommand getBaseCommand() {
        return this.baseCommand;
    }

    public ConfigManager getConfiguration() {
        return this.configuration;
    }

    public EntityManager getEntityManager() {
        return this.entityManager;
    }

    public RandomSpawnerManager getRandomSpawningManager() {
        return this.randomSpawningManager;
    }

    public MobManager getMobManager() {
        return this.mobManager;
    }

    public PlayerManager getPlayerManager() {
        return this.playerManager;
    }

    public WorldManager getWorldManager() {
        return this.worldManager;
    }

    public SkillManager getSkillManager() {
        return this.skillManager;
    }

    public DropManager getDropManager() {
        return this.dropManager;
    }

    public ItemManager getItemManager() {
        return this.itemManager;
    }

    public SpawnerManager getSpawnerManager() {
        return this.spawnerManager;
    }

    public VariableManager getVariableManager() {
        return this.variableManager;
    }

    public PlaceholderManager getPlaceholderManager() {
        return this.placeholderManager;
    }

    public HologramManager getHologramManager() {
        return this.hologramManager;
    }

    public CompatibilityManager getCompatibility() {
        return this.compatibility;
    }

    public BukkitHologramFactory getHologramProvider() {
        return this.hologramProvider;
    }

    public TimingsHandler getTimingsHandler() {
        return this.timingsHandler;
    }

    public BukkitAPIHelper getAPIHelper() {
        return this.APIHelper;
    }

    public Boolean getIsUpdateAvailable() {
        return this.isUpdateAvailable;
    }
}
