package plus.quest.script.quest.listener;

import org.bukkit.Location;
import org.bukkit.event.player.PlayerInteractEvent;
import plus.Main;
import plus.block.InteractableBlock;
import plus.block.StationManager;
import java.util.HashMap;


public class BlockListener implements InteractableBlock {

    private final HashMap<String, BlkListener> localCache = new HashMap<>();

    public static void register(Location... locations){
        for(Location loc:locations){
            BlockListener listener = new BlockListener();
            StationManager.stations.put(loc, listener);
        }
    }


    public static void addTrigger(String player, BlkListener listener){
        for(Location loc:listener.getLocations()) {
            BlockListener l2 = (BlockListener) StationManager.stations.get(loc);
            l2.localCache.put(player, listener);
        }
    }


    public static void removeTrigger(String player, BlkListener listener){
        for (Location loc : listener.getLocations()) {
            BlockListener l2 = (BlockListener) StationManager.stations.get(loc);
            l2.localCache.remove(player);
        }
    }


    @Override
    public void onPlayerClick(PlayerInteractEvent e) {
        BlkListener l = localCache.get(e.getPlayer().getName());
        if(l==null)return;
        l.onClick(Main.floader.getValkiria(e.getPlayer()).getQuestSession());
    }
}