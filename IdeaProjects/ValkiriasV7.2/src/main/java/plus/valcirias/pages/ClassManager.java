package plus.valcirias.pages;

import org.bukkit.Bukkit;
import plus.util.YandeRePlugin;
import plus.util.cfg.Config;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


public class ClassManager {

    private final HashMap<String, ClassSet> sets = new HashMap<>();
    private final String defaultKey;



    public ClassManager(YandeRePlugin p){
        Config c = p.getOrCreateYandeReConfig("classes.yml");

        List<String> keys;
        try {
            keys = new ArrayList<>(c.getConfigurationSection("class_sets").getKeys(false));
        } catch (Exception e){
            System.out.println("classets is null!");
            c.set("class_sets.example.classes.class1.classname", "valkiria");
            c.set("class_sets.example.classes.class1.big_ico", 0);
            c.set("class_sets.example.classes.class1.small_ico", 0);
            c.set("class_sets.example.classes.class1.lore", Arrays.asList("lore1", "lore2"));
            c.set("class_sets.example.classes.class1.big_ico", 0);
            c.set("class_sets.example.classes.class1.big_ico", 0);
            c.set("class_sets.example.classes.class1.big_ico", 0);
            c.saveChanges();
            keys = new ArrayList<>(c.getConfigurationSection("class_sets").getKeys(false));
        }
        defaultKey = keys.get(0);
        for(String key:keys){
            ClassSet s = new ClassSet(c, "class_sets."+key);
            sets.put(s.getName(), s);
        }
        Bukkit.getPluginManager().registerEvents(new ClassPageEvents(this), p);
    }
    public ClassSet getClassSet(String key){
        if(key==null)return sets.get(defaultKey);
        else return sets.get(key);
    }

    public List<String> getClassSets(){
        return new ArrayList<>(sets.keySet());
    }

}
