package plus.block;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import plus.Main;
import plus.system.CraftModule;
import plus.system.ModuleLoader;
import plus.util.cfg.Config;
import java.util.HashMap;


public class StationManager {

    public static HashMap<Location, InteractableBlock> stations = new HashMap<>();
    public static HashMap<Location, Location> aliases = new HashMap<>();
    public static Config data;

    public static void Initialize(Config config){

        data = config;

        int count = 0;
        if(data.getConfigurationSection("power_stations")!=null) {
            String[] keys = data.getConfigurationSection("power_stations").getKeys(false).toArray(new String[0]);
            for (String key : keys) {
                addStation(key);
                count+=1;
            }
        }

        System.out.printf("| Uploaded %s Power stations.%n", count);
        count = 0;

        if(Main.floader.bd.tpSender != null && data.getConfigurationSection("global_teleports")!=null) {
            String[] keys = data.getConfigurationSection("global_teleports").getKeys(false).toArray(new String[0]);

            for (String key : keys) {
                addTpTrigger(key);
                count+=1;
            }
        }

        System.out.printf("| Uploaded %s Teleport stations.%n", count);
        count = 0;

        if(ModuleLoader.ChestC != null && data.getConfigurationSection("loot_boxes")!=null) {
            String[] keys = data.getConfigurationSection("loot_boxes").getKeys(false).toArray(new String[0]);
            for (String key : keys) {
                addLotBox(key);
                count+=1;
            }
        }

        System.out.printf("| Uploaded %s Loot boxes.%n", count);
        count = 0;

        if(data.getConfigurationSection("mod_tables")!=null) {
            ModifitycationTable t = new ModifitycationTable();
            for (String key : data.getConfigurationSection("mod_tables").getKeys(false)) {
                stations.put(data.getLocation("mod_tables."+key+".location"), t);
                count+=1;
            }
        }

        System.out.printf("| Uploaded %s Mod tables.%n", count);
        count = 0;

        if(CraftModule.craftTable != null) {
            if (data.getConfigurationSection("craft_tables") != null) {
                CraftTable c = new CraftTable();
                for (String key : data.getConfigurationSection("craft_tables").getKeys(false)) {
                    stations.put(data.getLocation("craft_tables." + key + ".location"), c);
                    count+=1;
                }
            }
        }

        System.out.printf("| Uploaded %s Craft tables.%n", count);
        count = 0;

        if (data.getConfigurationSection("visual_teleports") != null) {
            for (String key : data.getConfigurationSection("visual_teleports").getKeys(false)) {
                addVisualTp(key);
                count+=1;
            }
        }

        System.out.printf("| Uploaded %s Visual teleports.%n", count);
        count = 0;

        if(data.getConfigurationSection("aliases")!=null) {
            String[] keys = data.getConfigurationSection("aliases").getKeys(false).toArray(new String[0]);
            for (String key : keys) {
                addAliase(key);
                count+=1;
            }
        }

        System.out.printf("| Uploaded %s Aliases.%n", count);

        Runnable task = () -> {
            try {
                Thread.sleep(150000);
            } catch (InterruptedException ignored) {}
            while (true) {
                for (InteractableBlock station : stations.values()) {

                    station.regen();
                }
                try {
                    Thread.sleep(300000);
                } catch (InterruptedException ignored) {
                }
            }
        };
        Thread thread = new Thread(task);
        thread.start();
    }


    public static void addAliase(String key){
        String patch = "aliases."+key;
        //Location from = data.getLocation(patch+".from");
        Location to = data.getLocation(patch + ".to");
        if(stations.containsKey(to)) {
            aliases.put(data.getLocation(patch + ".from"), to);
        } else {
            System.out.printf("| Find error in alias of key [%s], removed.%n", key);
            data.set(patch, null);
            data.saveChanges();
        }
    }


    public static void addVisualTp(String key){
        stations.put(
                data.getLocation("visual_teleports." + key + ".location"),
                new GuiTeleportBlock(
                        data.getOrSetString("visual_teleports."+key+".visual_gui", "example")
                )
        );
    }


    public static void addStation(String key){
        String patch = "power_stations."+key;
        PowerStation station = PowerStation.build(data.getInt(patch+".max_power"), data.getInt(patch+".speed_regen"));
        stations.put(data.getLocation(patch+".location"), station);
    }


    public static void addLotBox(String key){
        String patch = "loot_boxes." + key;
        String keyChest = data.getString(patch + ".keychest");
        if(ModuleLoader.ChestC.getChest(keyChest)!=null) {
            LootBox box = new LootBox(keyChest, data.getInt(patch + ".speed_regen", 50));
            stations.put(data.getLocation(patch + ".location"), box);
        }
        else {
            System.out.printf("| Find error in lootbox of key [%s]!%n", key);
            //data.set(patch, null);
            //data.saveChanges();
        }
    }


    private static void addTpTrigger(String key){
        String patch = "loot_boxes." + key;
        RedirectMarker st = new RedirectMarker(
                data.getOrSetString(patch + ".server", "main"),
                data.getOrSetString(patch + ".target_location", "world;0;100;0")
        );
        stations.put(data.getLocation(patch + ".location"), st);
    }


    public static void addVisualTeleportInCfg(Location loc, String key, String name){
        data.set("visual_teleports."+key+"..visual_gui", name);
        data.set("visual_teleports."+key+".location", loc);
        data.saveChanges();
        addVisualTp(key);
    }

    
    public static InteractableBlock getStation(Location loc){
        if(aliases.containsKey(loc))return stations.get(aliases.get(loc));
        else return stations.getOrDefault(loc, null);
    }


    public static void addStationInConfig(Location loc, String name){
        if(stations.containsKey(loc)){
            System.out.printf("| Key [%s]ready in stations!%n", name);
            return;
        }
        data.set("power_stations."+name+".location", loc);
        data.set("power_stations."+name+".max_power", 150);
        data.set("power_stations."+name+".speed_regen", 50);
        data.saveChanges();
        addStation(name);
    }


    public static void addLootBoxInConfig(Location loc, String name, String key){
        data.set("loot_boxes."+name+".location", loc);
        data.set("loot_boxes."+name+".keychest", key);
        data.set("loot_boxes."+name+".speed_regen", 50);
        data.saveChanges();
        addLotBox(name);
    }


    public static void addTableInConfig(Location loc, String name, String type){
        try {
            if (type.equals("craft")) {
                data.set("craft_tables." + name + ".location", loc);
                data.saveChanges();
                stations.put(loc, new CraftTable());
            } else if (type.equals("mod")) {
                   data.set("mod_tables." + name + ".location", loc);
             data.saveChanges();
                stations.put(loc, new ModifitycationTable());
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }


    public static void addAliaseInConfig(String name, Player pl){
        Location to = pl.getLocation().add(0,-1,0).getBlock().getLocation();
        if(!stations.containsKey(to)){
            pl.sendMessage("error!");
            return;
        }
        Location from = pl.getTargetBlock(null, 10).getLocation();
        addAliaseInConfig(name, to, from);
    }


    public static void addAliaseInConfig(String name, Location to, Location from){
        data.set("aliases."+name+".to", to);
        data.set("aliases."+name+".from", from);
        data.saveChanges();
        addAliase(name);
    }


    public static void ReloadHash(){
        if(data != null) {
            data.reload();
            stations.clear();
            aliases.clear();
            Initialize(data);
        } else {
            System.out.println("Stations config is disabled!");
            stations.clear();
            aliases.clear();
        }
    }
}