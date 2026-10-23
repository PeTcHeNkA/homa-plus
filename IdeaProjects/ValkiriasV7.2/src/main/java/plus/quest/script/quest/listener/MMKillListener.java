package plus.quest.script.quest.listener;

import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobDeathEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import plus.Main;

import java.util.HashMap;


public class MMKillListener implements Listener {

    private static boolean notDefined = true;
    private static final HashMap<String, HashMap<String, MMlistener>> cache = new HashMap<>();

    public static void define(){
        if(notDefined){
            Bukkit.getPluginManager().registerEvents(new MMKillListener(), Main.instance);
            notDefined = false;
        }
    }

    public static void addTrigger(String player, String mobId, MMlistener listener){
        HashMap<String, MMlistener> listeners = cache.computeIfAbsent(player, k -> new HashMap<>());
        if(listener != null) listeners.put(mobId, listener);
        else listeners.remove(mobId);
    }


    public static void clearFor(String player){
        cache.remove(player);
    }


    @EventHandler
    public void onMMDeath(MythicMobDeathEvent e){
        if(e.getKiller() instanceof Player player){
            HashMap<String, MMlistener> listeners = cache.get(player.getName());
            if(listeners==null)return;;
            MMlistener listener = listeners.get(e.getMobType().getInternalName());
            if(listener!=null) listener.onKill(player);
        }
    }
}
