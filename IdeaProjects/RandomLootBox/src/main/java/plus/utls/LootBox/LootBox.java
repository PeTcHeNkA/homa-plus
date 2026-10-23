package plus.utls.LootBox;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;
import plus.main;
import plus.utls.Te4hnoConfig;

import java.util.ArrayList;
import java.util.List;

public class LootBox {

    public List<World> w = new ArrayList<>();
    public List<Integer> MaxX = new ArrayList<>();
    public List<Integer> MinX = new ArrayList<>();

    public Location HashLoc;
    public Inventory inv;

    public int LastIndexRandomWorld = 0;
    public String messageFormat;
    public int cdRemove;
    public String sound = "";

    public String uid;

    public LootBox(String key){
        uid = key;
        ReSetValues();
    }
    public void ReSetValues(){
        Te4hnoConfig c = main.config;
        cdRemove = c.getInt(uid+".long", 5)*1200;
        sound = c.getString(uid+".sound");
        messageFormat = c.getString(uid+".msg_spawnbox");
        try {
            List<String> rw = c.getStringList(uid+".worlds");
            for(String rv : rw){
                String[] vs = rv.split(";");
                World ww = Bukkit.getWorld(vs[0]);
                if(ww==null){
                    System.out.println(String.format("World name is wrong! -> [%s]", vs[0]));
                    System.out.println("--- loaded worlds ---");
                    for(World a:Bukkit.getWorlds()){
                        System.out.println(a.getName());
                    }
                    System.out.println("----------------------");
                    throw new NullPointerException();
                    //System.out.println(String.format("Using defaults", Bukkit.getWorlds().get(0)));
                }
                w.add(ww);
                MinX.add(Integer.parseInt(vs[1]));
                MaxX.add(Integer.parseInt(vs[2]));
            }
        } catch (Exception e){
            e.printStackTrace();
            for(World wr:Bukkit.getWorlds()) {
                w.add(wr);
                MinX.add(-1000);
                MaxX.add(1000);
            }
        }
    }

    public static void TestFor(String value){
        TestFor(Bukkit.getPlayer(value));
    }
    public static void TestFor(Player p){
        Inventory i = Bukkit.createInventory(null, 54);
        i.setContents(main.inv.getRandomContent(54));
        p.openInventory(i);
    }
}
