package net.minecraftforge.registries;

import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.common.discovery.ASMDataTable.ASMData;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import javax.annotation.Nullable;

/**
 * Internal registry for tracking {@link ObjectHolder} references
 */
public enum ObjectHolderRegistry {
    INSTANCE;
    private List<ObjectHolderRef> objectHolders = Lists.newArrayList();

    private int vmVersion = 17;

    ObjectHolderRegistry(){
        l1: try {
            String version = ManagementFactory.getRuntimeMXBean().getSpecVersion();
            if(version.equals("1.8")){
                vmVersion = 8;break l1;
            }
            vmVersion = Integer.parseInt(version);
        } catch (Exception e){
            e.printStackTrace();
        }
    }


    public void findObjectHolders(ASMDataTable table) {
        FMLLog.log.info("Processing ObjectHolder annotations");
        if(vmVersion>11){
            FMLLog.warning("PROCESSOR DISABLED! USE MANUALLY");
            return;
        }
        Set<ASMData> allObjectHolders = table.getAll(GameRegistry.ObjectHolder.class.getName());
        Map<String, String> classModIds = Maps.newHashMap();
        Map<String, Class<?>> classCache = Maps.newHashMap();
        for (ASMData data : table.getAll(Mod.class.getName())) {
            String modid = (String)data.getAnnotationInfo().get("modid");
            classModIds.put(data.getClassName(), modid);
        }
        for (ASMData data : allObjectHolders) {
            String className = data.getClassName();
            String annotationTarget = data.getObjectName();
            String value = (String) data.getAnnotationInfo().get("value");
            boolean isClass = className.equals(annotationTarget);
            if (isClass)
            {
                scanTarget(classModIds, classCache, className, annotationTarget, value, isClass, false);
            }
        }
        // double pass - get all the class level annotations first, then the field level annotations
        for (ASMData data : allObjectHolders) {
            String className = data.getClassName();
            String annotationTarget = data.getObjectName();
            String value = (String) data.getAnnotationInfo().get("value");
            boolean isClass = className.equals(annotationTarget);
            if (!isClass) {
                scanTarget(classModIds, classCache, className, annotationTarget, value, isClass, false);
            }
        }
        scanTarget(classModIds, classCache, "net.minecraft.init.Blocks", null, "minecraft", true, true);
        scanTarget(classModIds, classCache, "net.minecraft.init.Items", null, "minecraft", true, true);
        scanTarget(classModIds, classCache, "net.minecraft.init.MobEffects", null, "minecraft", true, true);
        scanTarget(classModIds, classCache, "net.minecraft.init.Biomes", null, "minecraft", true, true);
        scanTarget(classModIds, classCache, "net.minecraft.init.Enchantments", null, "minecraft", true, true);
        scanTarget(classModIds, classCache, "net.minecraft.init.SoundEvents", null, "minecraft", true, true);
        scanTarget(classModIds, classCache, "net.minecraft.init.PotionTypes", null, "minecraft", true, true);
        FMLLog.log.info("Found {} ObjectHolder annotations", objectHolders.size());
    }


    private void scanTarget(Map<String, String> classModIds, Map<String, Class<?>> classCache, String className, @Nullable String annotationTarget, String value, boolean isClass, boolean extractFromValue) {
        Class<?> clazz;
        if (classCache.containsKey(className))
        {
            clazz = classCache.get(className);
        }
        else {
            try {
                clazz = Class.forName(className, extractFromValue, getClass().getClassLoader());
                classCache.put(className, clazz);
            }
            catch (ClassNotFoundException ex) {
                // unpossible?
                throw new RuntimeException(ex);
            }
        }
        if (isClass) {
            scanClassForFields(classModIds, className, value, clazz, extractFromValue);
        } else {
            if (value.indexOf(':') == -1) {
                String prefix = classModIds.get(className);
                if (prefix == null) {
                    FMLLog.log.warn("Found an unqualified ObjectHolder annotation ({}) without a modid context at {}.{}, ignoring", value, className, annotationTarget);
                    throw new IllegalStateException("Unqualified reference to ObjectHolder");
                }
                value = prefix + ":" + value;
            }
            try {
                Field f = clazz.getDeclaredField(annotationTarget);

                addHolderReference(new ObjectHolderRef(f, new ResourceLocation(value), extractFromValue));
            } catch (NoSuchFieldException ex) {
                // unpossible?
                throw new RuntimeException(ex);
            }
        }
    }


    private void scanClassForFields(Map<String, String> classModIds, String className, String value, Class<?> clazz, boolean extractFromExistingValues) {
        classModIds.put(className, value);
        for (Field f : clazz.getFields()) {
            int mods = f.getModifiers();
            boolean isMatch = Modifier.isPublic(mods) && Modifier.isStatic(mods) && Modifier.isFinal(mods);
            if (!isMatch || f.isAnnotationPresent(ObjectHolder.class)) {
                continue;
            }
            addHolderReference(new ObjectHolderRef(f, new ResourceLocation(value, f.getName()), extractFromExistingValues));
        }
    }


    private void addHolderReference(ObjectHolderRef ref) {
        if (ref.isValid()) {
            objectHolders.add(ref);
        }
    }


    public void applyObjectHolders() {
        FMLLog.log.info("Applying holder lookups");
        for (ObjectHolderRef ohr : objectHolders) {
            ohr.apply();
        }
        FMLLog.log.info("Holder lookups applied");
    }
}