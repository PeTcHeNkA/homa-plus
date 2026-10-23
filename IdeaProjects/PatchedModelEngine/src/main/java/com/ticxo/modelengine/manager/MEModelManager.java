package com.ticxo.modelengine.manager;

import com.ticxo.modelengine.ModelEngine;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.generator.ModelGenerator;
import com.ticxo.modelengine.api.manager.ModelManager;
import com.ticxo.modelengine.api.manager.ModelRegistry;
import com.ticxo.modelengine.api.manager.ModelRunnable;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.BaseEntity;
import com.ticxo.modelengine.api.model.BukkitEntity;
import com.ticxo.modelengine.api.model.BukkitPlayer;
import com.ticxo.modelengine.api.model.ModeledEntity;
import com.ticxo.modelengine.model.MEModeledEntity;
import com.ticxo.modelengine.model.MEModeledPlayer;
import com.ticxo.modelengine.model.active.MEActiveModel;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class MEModelManager implements ModelManager {
    private ModelGenerator generator;
    private ModelRegistry registry;
    private MEModelRunnable modelTicker;

    public MEModelManager() {
    }

    public void init() {
        this.generator = ModelEngine.core.getModelGenerator();
        this.registry = new MEModelRegistry();
        this.modelTicker = new MEModelRunnable();
        Bukkit.getScheduler().runTaskAsynchronously(ModelEngineAPI.api, () -> {
            if (ModelEngine.core.getConfigManager().generateOnStart) {
                ModelEngine.core.getCommandSender().sendMessage(ChatColor.AQUA + "[Model Engine] Generator Activated.");
                this.generator.generateModels();
            }

            this.registerModels();
        });
        this.modelTicker.start();
    }

    public void registerModels() {
        try {
            File pluginFolder = new File(ModelEngine.core.getDataFolder(), "cache");
            if (!pluginFolder.exists()) {
                pluginFolder.mkdirs();
            }

            //int i = 0;
            File[] var3 = pluginFolder.listFiles();
            if(var3==null)return;
            //int var4 = var3.length;

            for (File modelFile : var3) {
                //++i;
//                if (i > 6) {
//                    break;
//                }
                //System.out.println(i);

                this.registry.registerModel(modelFile);
            }
        } catch (Exception var7) {
            var7.printStackTrace();
        }

    }

    public void removeModeledEntity(UUID id) {
        this.modelTicker.removeModeledEntity(id);
    }

    public void removeAllModeledEntity() {
        this.modelTicker.removeAllModeledEntity();
    }

    public ModelRegistry getModelRegistry() {
        return this.registry;
    }

    public ModelRunnable getModelTicker() {
        return this.modelTicker;
    }

    public ModeledEntity createModeledEntity(Entity entity) {
        return entity instanceof Player ? new MEModeledPlayer(new BukkitPlayer((Player)entity)) : this.createModeledEntity(new BukkitEntity(entity));
    }

    public ModeledEntity createModeledEntity(BaseEntity<?> entity) {
        return new MEModeledEntity(entity);
    }

    public ModeledEntity restoreModeledEntity(Entity entity) {
        if (entity instanceof Player) {
            BukkitPlayer bp = new BukkitPlayer((Player)entity);
            bp.saveModelList(new HashMap());
            return null;
        } else {
            return this.restoreModeledEntity(new BukkitEntity(entity));
        }
    }

    public ModeledEntity restoreModeledEntity(final BaseEntity<?> entity) {
        if (entity == null) {
            return null;
        } else if (entity.isDead()) {
            entity.remove();
            return null;
        } else {
            ModeledEntity me = this.getModeledEntity(entity.getUniqueId());
            if (me == null) {
                List<String> ids = entity.getModelList();
                if (ids == null || ids.isEmpty()) {
                    return null;
                }

                me = this.createModeledEntity(entity);

                for (String id : ids) {
                    if (ModelEngineAPI.api.getModelBlueprint(id) != null) {
                        me.addActiveModel(createActiveModel(id));
                    }
                }

                me.setNametag(entity.getCustomName());
                me.setNametagVisible(entity.isCustomNameVisible());
                //try {
                if (entity.getMountPoint() != null) {
                    me.setRideable(true);
                    Entity e = Bukkit.getEntity(entity.getMountPoint());
                    if (e != null) {
                        e.remove();
                    }
                }

                //MEModeledEntity m = new MEModeledEntity(null);
                //m.getActiveModel(null).generatePartEntities();
                //} catch (Exception err){
                //    entity.remove();
                //}
            }

            return me;
        }
    }

    public ModeledEntity getModeledEntity(UUID id) {
        return this.modelTicker == null ? null : this.modelTicker.getModeledEntity(id);
    }

    public ActiveModel createActiveModel(String modelId) {
        return new MEActiveModel(modelId);
    }
}