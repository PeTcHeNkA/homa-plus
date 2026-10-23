package plus.mobs.utl;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import org.apache.commons.io.IOUtils;
import plus.mobs.Te4mobsMain;
import plus.mobs.ai.AiBuilder;
import plus.mobs.debug.TestMob;
import plus.mobs.frame.AbcMob;
import plus.mobs.frame.EntityAttributeApplicator;
import plus.mobs.frame.MobModel;
import plus.tson.TsonFile;
import plus.tson.TsonList;
import plus.tson.TsonMap;
import plus.tson.TsonObj;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;


public class MobRegManager {

    private final TsonFile config = new TsonFile(
            new File(Loader.instance().getConfigDir(), "te4mob-config.tson"));
    private final TsonMap defaultMobConfigMap = new TsonMap();
    private final HashMap<String, Biome> biomes = getAllBiomes();
    private final RegistryEvent.Register<EntityEntry> event;
    private int ID = 0;
    private final String codeFrame;
    public final String expressionFrame;
    public final MobCompiler compiler = new MobCompiler();
    public final HashMap<String, AbcMob.IEntityApplicator> aiModelCache = new HashMap<>();
    private boolean blocked = false;

    public MobRegManager(RegistryEvent.Register<EntityEntry> event) {
        String codeFrame1;
        String expFrame;
        this.event = event;
        try {
            InputStream stream = Te4mobsMain.class.getResourceAsStream("/assets/MobFrame.java");
            codeFrame1 = IOUtils.toString(stream, StandardCharsets.UTF_8);
            stream = Te4mobsMain.class.getResourceAsStream("/assets/ExpressionFrame.java");
            expFrame = IOUtils.toString(stream, StandardCharsets.UTF_8);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        codeFrame = codeFrame1;
        expressionFrame = expFrame;
        initDefaultMobSettings();
        //buildAllEntities();
    }


    private Class<? extends AbcMob> createClass(String name){
        String code = codeFrame.replace("%MOB_CLASS%", name);
        return (Class<? extends AbcMob>)
                compiler.load(compiler.compile(name, code), name);
    }


    private void initDefaultMobSettings(){
        TsonMap spawner = defaultMobConfigMap.addMap("spawner");
        spawner.put("chance", 10);
        spawner.put("min", 1);
        spawner.put("max", 2);
        TsonList list = spawner.addList("biomes");
        list.add("forest");
        TsonMap mob = defaultMobConfigMap.addMap("mob");
        mob.put("id", "example_id");
        mob.put("name", "example_name");
        TsonList tracker = mob.addList("tracker");
        tracker.add(64);
        tracker.add(2);
        tracker.add(false);
        list = defaultMobConfigMap.addList("model");
        list.add("geo/bat.geo.json");
        list.add("textures/model/entity/bat.png");
        list.add("animations/bat.animation.json");
        new EntityAttributeApplicator(mob.addMap("attributes"));
        new AbcMob.MobHitbox(mob.addMap("hitbox"));

        TsonMap animation = mob.addMap("animation");
        animation.put("fly", "animation.bat.fly");
        animation.put("idle", "animation.bat.idle");
        animation.put("walk", "animation.bat.walk");

        new AnimationFolder(animation);

        TsonMap ai = mob.addMap("ai");
        TsonList aiModel = ai.addList("normal");
        TsonMap aiModel2 = ai.addMap("logical");
        aiModel2.addList("normal").add("clear");

        ai.put("name","exmpLgc");
        ai.put("code", "mob.chunkCoordY > 100");
        aiModel.add("revenge-only");
        aiModel.add("aggressive");
        ai = ai.addMap("low-hp");
        ai.put("min-hp", 0.5);
        aiModel = ai.addList("normal");
        aiModel.add("clear");
        aiModel.add("peaceful");
        aiModel.add("like-item:minecraft#stone");
    }


    private HashMap<String, Biome> getAllBiomes(){
        HashMap<String, Biome> biomes = new HashMap<>();
        for(BiomeDictionary.Type t:BiomeDictionary.Type.getAll()){
            for(Biome biome:BiomeDictionary.getBiomes(t)){
                biomes.put(biome.getBiomeName(), biome);
            }
        }
        return biomes;
    }


    private ArrayList<Biome> encodeBiomesFromList(TsonList raw){
        ArrayList<Biome> biomes = new ArrayList<>();
        for(TsonObj obj:raw){
            Biome biome = this.biomes.get(obj.getStr());
            if(biome != null){
                biomes.add(biome);
            }
        }
        return biomes;
    }


    public static  <T extends AbcMob> void registerEntityRender(Class<T> clazz, TsonMap mobData){
        TsonList list = mobData.getList("model");
        registerEntityRender(clazz, list.getStr(0), list.getStr(1), list.getStr(2));
    }


    public static <T extends AbcMob> void registerEntityRender(Class<T> clazz, String model, String texture, String animation){
        IRenderFactory<? super T> claz0 = (IRenderFactory<T>) manager ->
                new GeoEntityRenderer<T>(manager, new MobModel<>(
                        model, texture, animation
                )) {};
        RenderingRegistry.registerEntityRenderingHandler(clazz, claz0);
    }

    private final HashSet<String> idCache = new HashSet<>();

    private String getId(TsonMap mobParams){
        ID+=1;
        String result = mobParams.getStr("id");
        if(idCache.contains(result)){
            throw new RuntimeException("Id "+result+" ready exists!");
        }
        return result;
    }

    private EntityEntry buildEntryEntry(Class<? extends AbcMob> clazz, TsonMap mobData) {
        TsonMap mobParams = mobData.getMap("mob");

        insert(clazz, "ATTRIBUTES", new EntityAttributeApplicator(mobParams.getMap("attributes")));
        insert(clazz, "HITBOX", new AbcMob.MobHitbox(mobParams.getMap("hitbox")));
        insert(clazz, "ANIM_FOLDER", new AnimationFolder(mobParams.getMap("animation")));
        insert(clazz, "MODEL", new AiBuilder(this, mobParams.getMap("ai")).build());

        TsonList tracker = mobParams.getList("tracker");
        EntityEntryBuilder<Entity> builder = EntityEntryBuilder
                .create()
                .entity(clazz)
                .id(getId(mobParams), ID)
                .name(mobParams.getStr("name"))
                .tracker(tracker.getInt(0), tracker.getInt(1), tracker.getBool(2));

        if (mobData.containsKey("spawner")) {
            TsonMap spawnData = mobData.getMap("spawner");
            builder.spawn(
                    EnumCreatureType.MONSTER,
                    spawnData.getInt("chance"),
                    spawnData.getInt("min"),
                    spawnData.getInt("max"),
                    encodeBiomesFromList(spawnData.get("biomes").getList())
            );
        }
        return builder.build();
    }


    protected <T> void insert(Class<?> clazz, String label, T value){
        try {
            clazz.getField(label).set(null, value);
        }catch (Exception e){}
    }


    protected void buildAndRegEntity(Class<? extends AbcMob> clazz){
        String className = clazz.getName();
        className = className.substring(className.lastIndexOf(".")+1);
        if(config.containsKey(className)) {
            TsonMap mobData = config.getMap(className);
            if (mobData.isEmpty()) {
                mobData = defaultMobConfigMap;
                config.put(className, defaultMobConfigMap);
                config.save();
            }
            event.getRegistry().register(buildEntryEntry(clazz, mobData));
            registerEntityRender(clazz, mobData);
            System.out.printf("CLASS: %s defined!%n", clazz.getName());
        } else {
            System.out.printf("Data for class [%s] not founded! ignored.", clazz.getName());
        }
    }


    public void buildAllEntities(){
        if(blocked){
            System.out.println("Аll Entities ready created!");
            return;
        }
        blocked = true;
        config.load();
        if(Te4mobsMain.PROD && config.isEmpty()){
            config.put("ExpMob", defaultMobConfigMap);
            config.save();
        }

        System.out.printf("Classes to load: [%s] [%s]%n",config.size(),
                String.join(",",config.keySet()));

        if(Te4mobsMain.PROD) {
            for (String className : config.keySet()) {
                buildAndRegEntity(createClass(className));
            }
        } else {
            for (String className : config.keySet()) {
                if(className.equals("TestMob"))continue;
                buildAndRegEntity(createClass(className));
            }
            if(!config.containsKey("TestMob")){
                config.put("TestMob", defaultMobConfigMap);
                config.save();
            }
            buildAndRegEntity(TestMob.class);
        }
        Te4mobsMain.onInitTasks.accept(this);
        Te4mobsMain.onInitTasks = null;
    }
}