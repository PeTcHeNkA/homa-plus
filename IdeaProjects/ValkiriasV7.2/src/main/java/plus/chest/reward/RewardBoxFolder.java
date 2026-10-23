package plus.chest.reward;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import plus.util.YandeRePlugin;
import plus.util.cfg.Config;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;


public class RewardBoxFolder {

    private final HashMap<Location, RewardBox> boxes = new HashMap<>();
    private final HashMap<Location, Location> aliases = new HashMap<>();
    private final Config c;
    public static RewardBoxFolder i;

    public RewardBoxFolder(YandeRePlugin p){
        i = this;
        this.c = p.getOrCreateYandeReConfig("rpg_reward_boxes.yml");
        ConfigurationSection section = c.getConfigurationSection("reward_boxes");
        if(section==null)return;

        for(String key:section.getKeys(false)){
            RewardBox b = new RewardRandomBox(c, key);
            boxes.put(c.getLocation("reward_boxes."+key+".location"), b);
        }
        if(!boxes.isEmpty()){
            Bukkit.getPluginManager().registerEvents(new RewardEvents(this), p);
        }
    }

    public RewardBox getRewardMox(Location loc){
        return boxes.get(loc);
    }


    public void putToConfigRewardBox(Player pl, String name){
        if(name==null || name.isEmpty())name = "reward_box_"+new Random().nextInt();
        Location to = pl.getLocation().add(0,-1,0).getBlock().getLocation();
        String path = "reward_boxes."+name;
        c.set(path+".items", Arrays.asList("item1", "item2"));
        c.set(path+".location", to);
        c.saveChanges();
    }


    public void putToConfigAlias(Player pl, String name){
        if(name==null || name.isEmpty())name = "alias_"+new Random().nextInt();
        Location to = pl.getLocation().add(0,-1,0).getBlock().getLocation();
        if(!aliases.containsKey(to)){
            pl.sendMessage("error!");
            return;
        }
        Location from = pl.getTargetBlock(null, 10).getLocation();
        c.set("aliases."+name+".to", to);
        c.set("aliases."+name+".from", from);
        c.saveChanges();
        aliases.put(from, to);
    }
}