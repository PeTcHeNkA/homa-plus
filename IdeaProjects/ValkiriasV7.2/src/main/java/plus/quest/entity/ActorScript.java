package plus.quest.entity;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import plus.Main;
import plus.quest.AbstractQuestSession;
import plus.util.cfg.Sector;
import plus.quest.motion.BasicPos;
import plus.quest.script.quest.QuestScript;
import plus.quest.script.scene.Script;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;


public class ActorScript implements QuestScript, Script {

    private final String modelId;
    private final Location loc;
    protected final int ticRate;
    private final ModelScript[] scripts;

    public ActorScript(Sector s, String path){
        modelId = s.getOrSetString(path+".model_id", "none");
        loc = s.getLocation(path+".location");
        ticRate = s.getOrSetInt(path+".tick_rate", 50);
        ArrayList<ModelScript> scripts = new ArrayList<>();
        for(String scriptData:s.getOrSetStringList(path+".animate",
                Arrays.asList("anim: pos;0;0;1", "sleep: 1000", "move: world;0;70;0;90;180"))){
            scripts.add(ModelScript.build(scriptData, this));
        }
        this.scripts = scripts.toArray(new ModelScript[0]);
    }


    private void run(Player player){
        new BukkitRunnable() {
            @Override
            public void run() {
                ArmorStand e = Te4ModelledEntity.buildEntity(loc);
                Thread t = new Thread(() -> {
                    Te4ModelledEntity me = new Te4ModelledEntity(e, modelId);
                    me.addToParentPlayer(player);
                    for (ModelScript s : scripts) {
                        s.run(me);
                    }
                    me.removePlayer(player);
                    me.remove();

                    //syncRemove(e);
                    Thread.currentThread().stop();
                });
                t.start();
            }
        }.runTaskLater(Main.instance, 0);
    }


    @Override
    public void run(AbstractQuestSession session) {
        run(session.getPlayer());
    }


    @Override
    public void run(Player player, int uid, BasicPos pos) {
        run(player);
    }


    @Override
    public void run(Collection<Player> players, int uid, BasicPos pos) {
        new BukkitRunnable() {
            @Override
            public void run() {
                ArmorStand e = Te4ModelledEntity.buildEntity(loc);
                Thread t = new Thread(() -> {
                    Te4ModelledEntity me = new Te4ModelledEntity(e, modelId);
                    for(Player player:players) me.addToParentPlayer(player);

                    for(ModelScript s:scripts){
                        s.run(me);
                    }
                    for(Player player:players) me.removePlayer(player);

                    me.remove();
                    //syncRemove(e);
                    Thread.currentThread().stop();
                });
                t.start();
            }
        }.runTaskLater(Main.instance, 0);
    }


//    public static Location encodeLoc(String data){
//        String[] d = data.split(";");
//        World w = Bukkit.getWorld(d[0]);
//        if(w==null){
//            System.out.println(Bukkit.getWorlds().get(0));
//            w = Bukkit.getWorlds().get(0);
//        }
//        double x = Double.parseDouble(d[1]);
//        double y = Double.parseDouble(d[2]);
//        double z = Double.parseDouble(d[3]);
//        float yaw = Float.parseFloat(d[4]);
//        float pith =  Float.parseFloat(d[5]);
//        return new Location(w, x, y, z, yaw, pith);
//    }

}