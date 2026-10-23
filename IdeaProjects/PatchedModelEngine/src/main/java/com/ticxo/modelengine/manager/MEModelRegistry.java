package com.ticxo.modelengine.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ticxo.modelengine.ModelEngine;
import com.ticxo.modelengine.api.generator.ModelBlueprint;
import com.ticxo.modelengine.api.manager.ModelRegistry;
import com.ticxo.modelengine.error.IError;
import com.ticxo.modelengine.generator.component.blueprint.MEModelBlueprint;
import com.ticxo.modelengine.generator.component.blueprint.element.BlueprintTimeLineDeserializer;
import com.ticxo.modelengine.generator.component.blueprint.element.BlueprintTimeline;
import com.ticxo.modelengine.io.FilenameUtils;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.md_5.bungee.api.ChatColor;

public class MEModelRegistry implements ModelRegistry {
    private final Map<String, ModelBlueprint> registeredModel = new ConcurrentHashMap();
    private final Gson gson = (new GsonBuilder()).registerTypeAdapter(BlueprintTimeline.class, new BlueprintTimeLineDeserializer()).create();

    public MEModelRegistry() {
    }

    public void registerModel(String modelId, ModelBlueprint model) {
        this.registeredModel.put(modelId, model);
        ModelEngine.core.getCommandSender().sendMessage(ChatColor.GREEN + "[Model Engine (Infinity)]--Registered " + ChatColor.GOLD + modelId + ChatColor.GREEN + " [" + model.getBoundingBoxWidth() + ", " + model.getBoundingBoxHeight() + "] (" + model.getEyeHeight() + ")");
        ModelEngine.core.getCommandSender().sendMessage(ChatColor.GREEN + "[Model Engine (Infinity)]----State Priority [lowest -> highest]: " + ChatColor.GOLD + model.getAnimationMap().keySet().toString());
        ModelEngine.core.getCommandSender().sendMessage("");
    }

    public void registerModel(File modelFile) throws IOException {
        if (!modelFile.isDirectory() && FilenameUtils.isExtension(modelFile.getName(), "model")) {
            FileReader reader = new FileReader(modelFile);
            String modelId = FilenameUtils.removeExtension(modelFile.getName());
            ModelBlueprint model = (ModelBlueprint)this.gson.fromJson(reader, MEModelBlueprint.class);
            reader.close();
            this.registerModel(modelId, model);
        }
    }

    public ModelBlueprint getModelBlueprint(String modelId) {
        return (ModelBlueprint)this.registeredModel.get(modelId);
    }

    public Map<String, ModelBlueprint> getRegisteredModel() {
        return this.registeredModel;
    }
}