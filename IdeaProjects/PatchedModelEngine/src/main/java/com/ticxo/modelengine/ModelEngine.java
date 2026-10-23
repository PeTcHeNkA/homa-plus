package com.ticxo.modelengine;

import com.google.gson.Gson;
import com.ticxo.modelengine.animation.MEAnimationManager;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.animation.AnimationManager;
import com.ticxo.modelengine.api.event.ModelEngineInitializeEvent;
import com.ticxo.modelengine.api.generator.ModelGenerator;
import com.ticxo.modelengine.api.manager.ControllerManager;
import com.ticxo.modelengine.api.manager.ModelManager;
import com.ticxo.modelengine.api.model.ModeledEntity;
import com.ticxo.modelengine.api.nms.NMSUtils;
import com.ticxo.modelengine.api.util.ConfigManager;
import com.ticxo.modelengine.citizens.ModelTrait;
import com.ticxo.modelengine.commands.MECommands;
import com.ticxo.modelengine.generator.MEModelGenerator;
import com.ticxo.modelengine.listener.EntityListener;
import com.ticxo.modelengine.listener.InteractListener;
import com.ticxo.modelengine.listener.PlayerListener;
import com.ticxo.modelengine.listener.WorldListener;
import com.ticxo.modelengine.manager.MEControllerManager;
import com.ticxo.modelengine.manager.MEModelManager;
import com.ticxo.modelengine.mythicmobs.listener.MythicListener;
import com.ticxo.modelengine.nms.v1_14_R1.NMSUtil_v1_14_R1;
import com.ticxo.modelengine.nms.v1_15_R1.NMSUtil_v1_15_R1;
import com.ticxo.modelengine.nms.v1_16_R1.NMSUtil_v1_16_R1;
import com.ticxo.modelengine.nms.v1_16_R2.NMSUtil_v1_16_R2;
import com.ticxo.modelengine.nms.v1_16_R3.NMSUtil_v1_16_R3;
import com.ticxo.modelengine.nms.v1_17_1_R1.NMSUtil_v1_17_1_R1;
import com.ticxo.modelengine.nms.v1_17_R1.NMSUtil_v1_17_R1;
//import net.citizensnpcs.api.CitizensAPI;
//import net.citizensnpcs.api.trait.TraitInfo;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

public class ModelEngine extends ModelEngineAPI {
    public static ModelEngine core;

    public boolean hasMythicMobs = false;

    public boolean hasCitizens = false;

    public boolean hasMCMMO = false;

    private PluginManager pm;

    private ConsoleCommandSender cs;

    private Gson gson;

    private String version;

    public void onEnable() {
        api = this;
        core = this;
        this.gson = new Gson();
        this.pm = Bukkit.getServer().getPluginManager();
        this.cs = Bukkit.getServer().getConsoleSender();
        modelsKey = new NamespacedKey((Plugin)this, "models");
        mountKey = new NamespacedKey((Plugin)this, "mount");
        getVersion();
        this.pm.registerEvents((Listener)new PlayerListener(), (Plugin)this);
        this.pm.registerEvents((Listener)new InteractListener(), (Plugin)this);
        this.pm.registerEvents((Listener)new EntityListener(), (Plugin)this);
        this.pm.registerEvents((Listener)new WorldListener(), (Plugin)this);
        if (this.pm.getPlugin("MythicMobs") != null) {
            this.hasMythicMobs = true;
            this.pm.registerEvents((Listener)new MythicListener(), (Plugin)this);
        }
//        if (this.pm.getPlugin("Citizens") != null) {
//            this.hasCitizens = true;at com.ticxo.modelengine.manager.MEModelManager.restoreModeledEntity(MEModelManager.java:138) ~[?:?]
//            CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(ModelTrait.class).withName("model"));
//        }
        this.hasMCMMO = (this.pm.getPlugin("mcMMO") != null);
        this.cs.sendMessage(ChatColor.AQUA + "[Model Engine] Engine Activated.");
        getCommand("meg").setExecutor((CommandExecutor)new MECommands());
        setConfigManager(new ConfigManager());
        getConfigManager().readConfig(false, true);
        runDelayedTask();
    }

    public void onDisable() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            ModelEngineAPI.api.getNMSUtils().removePlayer(player);
            ModelEngineAPI.api.getModelManager().getModelTicker().removePlayer(player);
        }
        this.cs.sendMessage(ChatColor.RED + "[Model Engine] Core shut down.");
    }

    public PluginManager getPluginManager() {
        return this.pm;
    }

    public ConsoleCommandSender getCommandSender() {
        return this.cs;
    }

    public Gson getGson() {
        return this.gson;
    }

    private void getVersion() {
        String name = Bukkit.getServer().getClass().getPackage().getName();
        this.version = name.substring(name.lastIndexOf('.') + 1);
    }

    private void runDelayedTask() {
        Bukkit.getScheduler().runTask((Plugin)this, () -> {
            applyNMS();
            setModelGenerator((ModelGenerator)new MEModelGenerator());
            setModelManager((ModelManager)new MEModelManager());
            setControllerManager((ControllerManager)new MEControllerManager());
            setAnimationManager((AnimationManager)new MEAnimationManager());
            Bukkit.getPluginManager().callEvent((Event)new ModelEngineInitializeEvent());
            this.nmsUtils.init();
            this.modelGenerator.init();
            this.modelManager.init();
            this.controllerManager.init();
            this.animationManager.init();
            for (Player player : Bukkit.getOnlinePlayers()) {
                this.nmsUtils.injectPlayer(player);
                double range = (getConfigManager()).activeRange;
                for (Entity entity : player.getNearbyEntities(range, range, range)) {
                    ModeledEntity model = getModelManager().restoreModeledEntity(entity);
                    if (model != null)
                        model.addPlayer(player);
                }
            }
        });
    }

    private void applyNMS() {
        if (this.nmsUtils != null)
            return;
        switch (this.version) {
            case "v1_14_R1":
                setNMSUtils((NMSUtils)new NMSUtil_v1_14_R1());
                break;
            case "v1_15_R1":
                setNMSUtils((NMSUtils)new NMSUtil_v1_15_R1());
                break;
            case "v1_16_R1":
                setNMSUtils((NMSUtils)new NMSUtil_v1_16_R1());
                break;
            case "v1_16_R2":
                setNMSUtils((NMSUtils)new NMSUtil_v1_16_R2());
                break;
            case "v1_16_R3":
                setNMSUtils((NMSUtils)new NMSUtil_v1_16_R3());
                break;
            case "v1_17_R1":
                if (Bukkit.getBukkitVersion().contains("1.17.1")) {
                    setNMSUtils((NMSUtils)new NMSUtil_v1_17_1_R1());
                    break;
                }
                setNMSUtils((NMSUtils)new NMSUtil_v1_17_R1());
                break;
        }
    }
}
