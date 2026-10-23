package plus.Motions;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import plus.Camera;
import plus.Character.AnimatedCharacter;
import plus.Character.Character;
import plus.util.Config.Config;
import plus.util.CordMath;
import java.lang.reflect.InvocationTargetException;
import java.util.*;


public class CatScene {

    private static Config c = null;
    private static final HashMap<String,CatScene> cashe = new HashMap<>();
    public static final ProtocolManager pm = ProtocolLibrary.getProtocolManager();
    protected static final List<String> ListNames = new ArrayList<>();
    private final List<Motion> motions = new ArrayList<>();
    private int speed;
    private World sellectedWorld;
    protected Character character;
    private Location spawnPos;
    private Config customConfig = null;
    private final String Name;
    private boolean runned = false;
    private int motionIndex = 0;
    private boolean multiPlayer = false;
    private final int uid = new Random().nextInt(Integer.MAX_VALUE);
    private final List<Player> players = new ArrayList<>();


    public CatScene(Config cfg, String name){
        Name = name;
        customConfig = cfg;
        init(name, cfg);
    }


    public CatScene(String name){
        Name = name;
        init(name);
    }

    private void init(String name){
        init(name, c);
    }


    public CatScene(String name, Location loc){
        Name = name;
        init(name, c, loc.getX(), loc.getY(), loc.getZ(), false);
    }


    private void init(String name, Config c){
        init(name, c, 0, 0, 0, true);
    }


    private void init(String name, Config c, double startX, double startY, double startZ, boolean save){
        String path = "scenes." + name;
        speed = c.getInt(path+".speed", 10);
        String look_target = c.getString(path+".look_target");

        if(c.contains(path+".character")){
            character = new AnimatedCharacter(
                    c.getString(path + ".character.model"),
                    c.getString(path + ".character.name", null),
                    c.getBoolean(path + ".character.no_damage", false)
            );
            String[] hs = c.getString(path+".character.rotation", "0;0").split(";");

            float[] yawPith = new float[]{
                    Float.parseFloat(hs[0]),
                    Float.parseFloat(hs[1])
            };

            sellectedWorld = Bukkit.getWorld(c.getOrSetIfExistString(path+".world", "world"));
            if(sellectedWorld == null){
                System.out.println("World name is wrong!");
                sellectedWorld = Bukkit.getWorlds().get(0);
                System.out.println(Bukkit.getWorlds());
            }

            double[] spawncds = CordMath.parseCords(look_target);
            spawnPos = new Location(
                    sellectedWorld,
                    spawncds[0]+startX,
                    spawncds[1]+startY,
                    spawncds[2]+startZ,
                    yawPith[0],
                    yawPith[1]
            );
        } else {
            character = null;
        }


        for(String key:c.getConfigurationSection(path + ".motions").getKeys(false)){
            String np = path+".motions."+key;

            Motion m = new Motion(this,
                    c.getString(np + ".type"),
                    c.getStringList(np + ".data"),
                    CordMath.parseCords(c.getString(np + ".start_pos"), startX, startY, startZ),
                    CordMath.parseCords(c.getString(np + ".look_target", look_target), startX, startY, startZ),
                    c.getOrSetIfExistInt(np + ".time", 25),
                    c.getOrSetIfExistBoolean(np + ".invert", false)
            );

            if(c.contains(np+".dark")) {
                String[] cs = c.getString(np+".dark").split(";");
                m.addDarkEffect(Integer.parseInt(cs[0]), Integer.parseInt(cs[1]));
            }

            if(character != null && c.contains(np+".model_animation")){
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

        if(save) {
            cashe.put(name, this);
        }
    }



    public static CatScene getCatScene(String key){
        if(cashe.containsKey(key)){
            return cashe.get(key);
        } else {
            return new CatScene(key);
        }
    }


    public static void runCatScene(String key, Player player, Location loc){
        new CatScene(key, loc).safeRun(player);
    }


    public static void runCatScene(String key, List<Player> player, Location loc){
        if(player.size()>1) new CatScene(key, loc).run(player);
        else runCatScene(key, player.get(0), loc);
    }


    public static void runCatScene(String key, Player player){
        getCatScene(key).run(player);
    }


    public static void setConfig(Config cfg){
        c = cfg;
        ListNames.clear();
    }


    public static void reloadAllCatScenes(){
        Collection<CatScene> Scenes = cashe.values();
        for(CatScene s:Scenes){
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


    public void run(Player p){
        if(!runned) {
            runned = true;
            run2(p);
        } else {
            multiPlayer = true;
            players.add(p);
        }
    }


    public void run(List<Player> pl){
        players.addAll(pl);
        if(!runned){
            runned = true;
            preporationForStart();
        } else {
            createCamera(pl);
        }
    }


    public void safeRun(Player p){
        if(!p.getWorld().equals(sellectedWorld))return;
        if(character != null) return;

        Runnable task = () -> {
            PacketContainer[] c = createCameraPacket(uid, p.getLocation());
            try {
                pm.sendServerPacket(p, c[0]);
                pm.sendServerPacket(p, c[1]);
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            for(Motion m:motions){
                m.run(p, uid, speed);
                if(!p.isOnline()){
                    onEndAnimation();
                    break;
                }
            }
            resetPlayerCamera(p);
        };
        Thread thread = new Thread(task);
        thread.start();
    }


    public void run2(Player p){
        if(!p.getWorld().equals(sellectedWorld))return;

        if(character != null){
            character.spawn(spawnPos);
        }

        Runnable task = () -> {
            PacketContainer[] c = createCameraPacket(uid, p.getLocation());
            try {
                pm.sendServerPacket(p, c[0]);
                pm.sendServerPacket(p, c[1]);
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            for(motionIndex = 0;motionIndex<motions.size();motionIndex++){
                Motion m = motions.get(motionIndex);
                m.run(p, uid, speed);
                if(!p.isOnline()){
                    onEndAnimation();
                    break;
                }
                if(multiPlayer){
                    players.add(p);
                    runSceneThread();
                    break;
                }
            }
            if(!multiPlayer) {
                resetPlayerCamera(p);
                onEndAnimation();
            }
        };
        Thread thread = new Thread(task);
        thread.start();
    }


    private void preporationForStart(){
        if(players.isEmpty())return;
        for(Player p:players) {
            if (!p.getWorld().equals(sellectedWorld)) return;
        }
        if(character != null){
            character.spawn(spawnPos);
        }
        Runnable task = () -> {
            createCamera(players);
            runSceneThread();
        };
        Thread thread = new Thread(task);
        thread.start();
    }


    private void createCamera(List<Player> pls){
        for(Player p:pls){
            PacketContainer[] pkt = createCameraPacket(uid, p.getLocation());
            try {
                pm.sendServerPacket(p,pkt[0]);
                pm.sendServerPacket(p,pkt[1]);
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }


    private void runSceneThread(){
        for(motionIndex = 0;motionIndex<motions.size();motionIndex++){
            Motion m = motions.get(motionIndex);
            m.run(players, uid, speed);
            players.removeIf(p -> !p.isOnline());
            if(players.isEmpty()){
                onEndAnimation();
                break;
            }
        }
        resetPlayerCamera();
        onEndAnimation();
    }


    private void resetPlayerCamera(Player p){
        PacketContainer s = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);
        List<Integer> l = new ArrayList<>();
        l.add(uid);
        s.getIntLists().write(0, l);
        try {
            pm.sendServerPacket(p, s);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        setCamera(p, p.getEntityId());
    }


    private void resetPlayerCamera(){
        PacketContainer s = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);
        List<Integer> l = new ArrayList<>();
        l.add(uid);
        s.getIntLists().write(0, l);

        try {
            for(Player p:players) {
                pm.sendServerPacket(p, s);
                setCamera(p, p.getEntityId());
            }
            players.clear();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }


    private void onEndAnimation(){
        runned = false;
        motionIndex = 0;
        multiPlayer = false;

        if(character !=null) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    character.remove();
                }
            }.runTaskLater(Camera.c, 0);
        }
    }


    public static void setCamera(Player player, int EntityUID){
        try {
            PacketContainer cam = new PacketContainer(PacketType.Play.Server.CAMERA);
            cam.getIntegers().write(0, EntityUID);
            pm.sendServerPacket(player, cam);
        } catch (InvocationTargetException err) {
            throw new RuntimeException(
                    "Cannot send packet ", err);
        }
    }


    //private static final Random r = new Random();


//    public static int createCamera(Player player, int cdRemove){
//        Location l = player.getLocation();
//        l.setY(l.getY()+0.5);
//        return createCamera(player, l, cdRemove);
//    }


//    public static int createCamera(Player player, Location loc, int cdRemove){
//        int uid = createCamera(player, loc);
//        new BukkitRunnable(){
//            @Override
//            public void run() {
//                try {
//                    PacketContainer s = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);
//                    List<Integer> l = new ArrayList<>();
//                    l.add(uid);
//                    s.getIntLists().write(0, l);
//                    pm.sendServerPacket(player, s);
//
//                } catch (InvocationTargetException invocationTargetException) {
//                    invocationTargetException.printStackTrace();
//                }
//                setCamera(player, player.getEntityId());
//            }
//        }.runTaskLater(Camera.c, cdRemove);
//        return uid;
//    }


//    public static int createCamera(Player player, Location loc){
//        int uid = r.nextInt(Integer.MAX_VALUE);
//        try {
//            PacketContainer[] pp = createCameraPacket(uid, loc);
//            pm.sendServerPacket(player, pp[0]);
//            pm.sendServerPacket(player, pp[1]);
//        } catch (InvocationTargetException err) {
//            err.printStackTrace();
//        }
//        return uid;
//    }

    protected static PacketContainer[] createCameraPacket(int uid, Location loc){
        PacketContainer p1 = new PacketContainer(PacketType.Play.Server.SPAWN_ENTITY_LIVING);

        p1.getIntegers().write(0, uid)
                .write(1, 1);
        p1.getDoubles().write(0, loc.getX())
                .write(1, loc.getY())
                .write(2, loc.getZ());

        PacketContainer p2 = new PacketContainer(PacketType.Play.Server.CAMERA);
        p2.getIntegers().write(0, uid);
        return new PacketContainer[]{p1, p2};
    }
}