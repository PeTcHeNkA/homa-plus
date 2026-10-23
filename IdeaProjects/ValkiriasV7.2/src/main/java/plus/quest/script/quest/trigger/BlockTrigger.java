package plus.quest.script.quest.trigger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import plus.quest.AbstractQuestSession;
import plus.util.cfg.Sector;
import plus.quest.script.quest.listener.BlkListener;
import plus.quest.script.quest.listener.BlockListener;
import java.util.ArrayList;
import java.util.Arrays;


public class BlockTrigger extends Trigger implements BlkListener {

    private final Location[] locations;

    public BlockTrigger(Sector s, String path) {
        super(s, path);
        locations = parseLocations(s, path);
        BlockListener.register(this.locations);
    }


    public static Location[] parseLocations(Sector s, String path){
        World w = Bukkit.getWorld(s.getOrSetString(path+".world", "world"));

        if(w==null){
            w = Bukkit.getWorlds().get(0);
            System.out.println(Bukkit.getWorlds());
        }
        ArrayList<Location> locations = new ArrayList<>();
        for(String key:s.getOrSetStringList(path+".locations", Arrays.asList("0,70,0"))){
            if(key.contains(";")){
                for(String key2:key.strip().split(";")){
                    String[] hs = key2.split(",");
                    locations.add(new Location(w,
                            Double.parseDouble(hs[0]), Double.parseDouble(hs[1]), Double.parseDouble(hs[2])));
                }
            } else {
                String[] hs = key.split(",");
                locations.add(new Location(w,
                        Double.parseDouble(hs[0]), Double.parseDouble(hs[1]), Double.parseDouble(hs[2])));
            }
        }
        return locations.toArray(new Location[0]);
    }


    @Override
    public void onLoad(AbstractQuestSession session) {
        BlockListener.addTrigger(session.getPlayer().getName(), this);
    }


    @Override
    public void onUnload(AbstractQuestSession session) {
        BlockListener.removeTrigger(session.getPlayer().getName(), this);
    }


    @Override
    public void onClick(AbstractQuestSession session) {
        run(session);
        if(needUnloadAfterActivation) unload(session);
    }


    @Override
    public Location[] getLocations() {
        return locations;
    }
}