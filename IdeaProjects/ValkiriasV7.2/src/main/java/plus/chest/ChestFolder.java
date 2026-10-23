package plus.chest;

import org.bukkit.configuration.ConfigurationSection;
import plus.util.YandeRePlugin;
import plus.util.cfg.Config;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


public class ChestFolder {

    private final HashMap<String, Chest> chests = new HashMap<>();

    public ChestFolder(YandeRePlugin p){
        reloadChests(p.getOrCreateYandeReConfig("rpg_chests.yml"));
        //boolean enableAirBoxes = c.getOrSetIfExistBoolean("main.enable_air_boxes", true);
    }


    private void reloadChests(Config c){
        ConfigurationSection sc = c.getConfigurationSection("chests");
        chests.clear();
        if(sc==null){
            System.out.println("| Chests is empty!");
            return;
        }
        for(String name:sc.getKeys(false)) {
            try {
                chests.put(name, new Chest(
                                c.getOrSetString("chests." + name + ".display", ""),
                                c.getOrSetInt("chests." + name + ".size", 27),
                                getOrCreateListString(c, "chests." + name + ".items"),
                                c.getOrSetString("chests." + name + ".count", "e")
                        )
                );
            } catch (NullPointerException e){
                System.out.printf("| Chest [%s] contains error!%n", name);
                e.printStackTrace();
            }
        }
    }


    private List<String> getOrCreateListString(Config c, String path){
        if(c.contains(path)){
            return c.getStringList(path);
        } else {
            c.set(path, Arrays.asList("example", "example"));
            return null;
        }
    }


    public Chest getChest(String key){
        return chests.get(key);
    }
}