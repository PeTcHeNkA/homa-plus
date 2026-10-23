package plus.quest;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import plus.util.AutoReloadable;
import plus.util.cfg.Config;
import plus.quest.motion.BasicPos;
import plus.quest.motion.TpMotion;
import plus.quest.script.scene.PacketCreateCamera;
import plus.quest.script.scene.ScriptRemoveCamera;
import plus.quest.script.scene.Script;
import plus.quest.script.scene.ScriptFactory;

import java.util.*;


public class Scene {

    private static Cache cs;
    public final String name;
    protected final Script[] scripts;
    protected final boolean needCreateCamera;
    protected static final Random r = new Random();

    public static void setWorkConfig(Config c){
        cs = new Cache(c);
    }


    public static void run(String name, Player player, BasicPos offSet){
        cs.run(name, player, offSet);
    }


    public static void run(String name, Collection<Player> player, BasicPos offSet){
        cs.run(name, player, offSet);
    }


    public static Scene get(String name){
        return cs.getScene(name);
    }


    public static List<String> getListScenes(){
        return cs.scenes;
    }


    public Scene(Config c, String name){
        this.name = name;
        String path = "scenes."+name;

        List<Script> scripts = new ArrayList<>();

        for(String key:c.getConfigurationSection(path+".scripts").getKeys(false)){
            Script s = ScriptFactory.getScript(c, path+".scripts."+key);
            if(s != null)scripts.add(s);
        }

        if(c.getOrSetBoolean(path+".use_auto_entry", true)) {
            needCreateCamera = !(scripts.get(0) instanceof PacketCreateCamera);
        } else needCreateCamera = false;
        if(c.getOrSetBoolean(path+".use_auto_exit", true)) {
            if (!(scripts.get(scripts.size() - 1) instanceof ScriptRemoveCamera)) {
                scripts.add(ScriptRemoveCamera.getInstance());
            }
        }
        this.scripts = scripts.toArray(new Script[0]);
    }


    public void run(Player player, BasicPos offSet){
        new Thread(() -> {
            int uid = r.nextInt(1, Integer.MAX_VALUE);
            if(needCreateCamera){
                Location loc = player.getLocation().clone();
                loc.setY(loc.getY()+0.5);
                new PacketCreateCamera(TpMotion.build(loc), false).run(player, uid, offSet);
            }
            for(Script s:scripts){
                s.run(player, uid, offSet);
            }
        }).start();
    }


    public void run(Collection<Player> players, BasicPos offSet){
        new Thread(() -> {
            int uid = r.nextInt(1, Integer.MAX_VALUE);
            if(needCreateCamera){
                for(Player player:players) {
                    Location loc = player.getLocation().clone();
                    loc.setY(loc.getY() + 0.5);
                    new PacketCreateCamera(TpMotion.build(loc), false).run(player, uid, offSet);
                }
            }
            for(Script s:scripts){
                s.run(players, uid, offSet);
            }
        }).start();
    }
}


class Cache extends AutoReloadable {

    protected final HashMap<String, Scene> cache = new HashMap<>();
    protected final Config c;
    protected List<String> scenes;

    protected Cache(Config c) {
        this.c = c;
        reload();
    }


    private Scene build(Config c, String name){
        try {
            return new Scene(c, name);
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }


    public Scene getScene(String name){
        if(cache.containsKey(name)){
            return cache.get(name);
        } else {
            Scene scene = build(c, name);
            cache.put(name, scene);
            return scene;
        }
    }


    public void run(String name, Player player, BasicPos offSet){
        Scene s = getScene(name);
        if(s==null){
            System.out.println("Null scene! ->"+name);
        } else s.run(player, offSet);
    }


    public void run(String name, Collection<Player> player, BasicPos offSet){
        getScene(name).run(player, offSet);
    }


    @Override
    public void reload() {
        cache.clear();
        c.reload();
        try {
            scenes = new ArrayList<>(c.getConfigurationSection("scenes").getKeys(false));
        } catch (NullPointerException err){
            scenes = null;
        }
    }
}