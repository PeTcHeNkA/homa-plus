package plus.chest;

import org.bukkit.inventory.ItemStack;
import plus.util.cfg.Config;

import java.util.Arrays;
import java.util.List;
import plus.util.map.RangableMap;
import plus.util.map.Range;


public class LvlableSimpleChest {


    protected final RangableMap<SimpleChest> map = new RangableMap<>();


    public LvlableSimpleChest(Config c, String name){
        //super(c, name);
        List<String> keys = c.getOrSetStringList("loot_tables." + name + ".lvltable", Arrays.asList("example", "example"));

        for(String key:keys){
            String[] hs = key.split(":");
            Range r = new Range(SimpleChest.parseRange(hs[0]));
            SimpleChest chest = new SimpleChest(c, hs[1], true);
            map.put(chest, r);
        }
    }


//    public ItemStack[] dropContent(int lvl){
//        return map.getOrDefault(lvl, this).dropContent();
//    }


    public ItemStack[] dropContent(double lvl){
        return map.getOrFixNull(lvl).dropContent();
    }


    public SimpleChest getFrom(double lvl){
        return map.getOrFixNull(lvl);
    }
}