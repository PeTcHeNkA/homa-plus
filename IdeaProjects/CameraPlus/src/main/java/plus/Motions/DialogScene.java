package plus.Motions;

import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import plus.Camera;
import plus.Character.AnimatedCharacter;
import plus.util.Config.Config;
import plus.util.CordMath;

import javax.naming.Name;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class DialogScene {

    private static Config c = null;
    private Config customConfig;
    private static final HashMap<String,DialogScene> cashe = new HashMap<>();
    protected static final List<String> ListNames = new ArrayList<>();
    private int speed;
    private double[] startPos;
    private Location spawnPos;
    protected plus.Character.Character character;
    private World sellectedWorld;

    private String Name;

    private final List<MotionWithBreakPoints> motions = new ArrayList<>();

    public static void setConfig(Config c){
        DialogScene.c = c;
    }
    public DialogScene(String name){
        init(name, c);
    }

    public DialogScene(String name, Config c){
        customConfig = c;
        init(name, c);
    }

    public static void reloadAllCatScenes(){
        Collection<DialogScene> Scenes = cashe.values();
        for(DialogScene s:Scenes){
            s.reload();
        }
        ListNames.clear();
    }
    public void reload(){
        if(customConfig==null) {
            cashe.remove(Name);
        } else {
            init(Name, customConfig);
        }
    }

    public static List<String> getListNames(){
        if(ListNames.isEmpty()){
            ListNames.addAll(c.getConfigurationSection("scenes").getKeys(false));
        }
        return ListNames;
    }

    private void init(String name, Config c){
        Name = name;
        //c = cg;
        String path = "dialog_scenes." + name;
        Collection<String> keys = c.getConfigurationSection(path + ".motions").getKeys(false);

        speed = c.getInt(path+".speed", 10);
        String look_target = c.getString(path+".look_target");
        startPos = CordMath.parseCords(c.getString(path+".start_pos"));

        float[] yawPith;
        if(c.contains(path+".character")){
            character = new AnimatedCharacter(
                    c.getString(path + ".character.model"),
                    c.getString(path + ".character.name", null),
                    c.getBoolean(path + ".character.no_damage", false)
            );
            String[] hs = c.getString(path+".character.rotation", "0;0").split(";");

            yawPith = new float[]{
                    Float.parseFloat(hs[0]),
                    Float.parseFloat(hs[1])
            };
            activeRuns = new ArrayList<>();
        } else {
            character = null;
            yawPith = new float[]{0,0};
        }

        sellectedWorld = Bukkit.getWorld(c.getOrSetIfExistString(path+".world", "world"));

        if(sellectedWorld == null){
            System.out.println("World name is wrong!");
            sellectedWorld = Bukkit.getWorlds().get(0);
            System.out.println(Bukkit.getWorlds());
        }
        double[] spawncds = CordMath.parseCords(look_target);
        spawnPos = new Location(
                sellectedWorld,
                spawncds[0],
                spawncds[1],
                spawncds[2],
                yawPith[0],
                yawPith[1]
        );

        for(String key:keys){
            String np = path+".motions."+key;

            MotionWithBreakPoints m = new MotionWithBreakPoints(this,
                    c.getString(np + ".type"),
                    c.getStringList(np + ".data"),
                    c.getString(np + ".start_pos"),
                    c.getString(np + ".look_target", look_target),
                    c.getOrSetIfExistInt(np + ".time", 25),
                    c.getOrSetIfExistBoolean(np + ".invert", false)
            );

            if(c.getOrSetIfExistBoolean(np+".break_point", false)){
                m.createBreakPoint();
            }

            if(c.contains(np+".model_animation")){
                String[] hs = c.getString(np+".model_animation").split(":");
                m.setAnimation(hs[0], Double.parseDouble(hs[1]));
            }

            if(c.contains(np+".messages")){
                Collection<String> rawMsgKeys = c.getConfigurationSection(np + ".messages").getKeys(false);
                if(!rawMsgKeys.isEmpty()) {
                    for (String rmk : rawMsgKeys) {
                        int k = Integer.parseInt(rmk.split("=")[1]);
                        m.addMessages(k, c.getStringList(np + ".messages." + rmk));
                    }
                }
            }
            motions.add(m);
        }
        cashe.put(name, this);
    }

    protected List<String> activeRuns;
    private final int uid = new Random().nextInt(Integer.MAX_VALUE);
    private boolean runned = false;
    //int motionIndex = 0;

    public static DialogScene getDialogScene(String name){
        return cashe.getOrDefault(name, new DialogScene(name, c));
    }
    public static void runDialogScene(String name, Player p){
        getDialogScene(name).run(p);
    }

    public void run(Player p){
        if(!p.getWorld().equals(sellectedWorld))return;
        if(character != null){
            if(!runned) {
                character.spawn(spawnPos);
                runned = true;
                run2(p);
            }
        } else {
            if(players.contains(p.getName())){
                resume(p);
            } else {
                if(!activeRuns.contains(p.getName())){
                    activeRuns.add(p.getName());
                    run2(p);
                }

            }
        }
    }

    protected final List<String> players = new ArrayList<>();

    public void resume(Player p){
        players.remove(p.getName());
    }

    private void run2(Player p){
        Runnable task = () -> {
            PacketContainer[] c = CatScene.createCameraPacket(uid, p.getLocation());
            try {
                CatScene.pm.sendServerPacket(p, c[0]);
                CatScene.pm.sendServerPacket(p, c[1]);
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            for(int motionIndex = 0;motionIndex<motions.size();motionIndex++){
                MotionWithBreakPoints m = motions.get(motionIndex);
                if(m.hasBreakPoint()){
                    players.add(p.getName());
                }
                m.run(p, uid, speed);
                if(!p.isOnline()){
                    onEndAnimation(p);
                    break;
                }
            }
            CatScene.setCamera(p, p.getEntityId());
            if(runned) {
                onEndAnimation(p);
            }
        };
        Thread thread = new Thread(task);
        thread.start();
    }

    private void onEndAnimation(Player p){
        runned = false;
        if(character !=null) {
            BukkitTask r = new BukkitRunnable() {
                @Override
                public void run() {
                    character.remove();
                }
            }.runTaskLater(Camera.c, 0);
        } else {
            activeRuns.remove(p.getName());
        }
    }


}
