package plus.block.teleport;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import plus.Main;
import plus.util.ConfigHelper;
import plus.util.cfg.BasicSector;
import plus.util.cfg.Config;
import plus.util.cfg.Sector;
import plus.valcirias.Valkiria;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;


public abstract class VisualLocation {

    public final int price;
    public final ItemStack bigIco;
    public final ItemStack nullLore;
    public final ItemStack smallIco;
    public final String name;

    private static final HashMap<String, VisualLocation> hash = new HashMap<>();


    public static VisualLocation build(Config cfg, String name){
        VisualLocation l = hash.get(name);
        if(l==null){
            if(cfg.contains("labels."+name+".loc")) {
                l = new WorldVisualLocation(cfg, name);
            } else {
                l = new ServerVisualLocation(cfg, name);
            }
            hash.put(name, l);
        }
        return l;
    }


    public VisualLocation(Sector cfg, String name){
        this.name = name;
        String path = "labels."+name;

        price = cfg.getOrSetInt(path+".price", 50);

        ArrayList<String> lore = new ArrayList<>(cfg.getOrSetStringList(path+".lore", Arrays.asList("lore1", "lore2")));

        for(int i = 0;i<lore.size();i++){
            lore.set(i, BasicSector.parseColor(lore.get(i)).replace("%price%", String.valueOf(price)));
        }

        String viewName = lore.remove(0);

        nullLore = ConfigHelper.nullSlot.clone();
        ItemMeta meta = nullLore.getItemMeta();
        meta.setLore(lore);
        meta.setDisplayName(viewName);
        nullLore.setItemMeta(meta);

        bigIco = new ItemStack(Material.DIAMOND);
        meta = bigIco.getItemMeta();
        meta.setLore(lore);
        meta.setDisplayName(viewName);
        meta.setCustomModelData(cfg.getOrSetInt(path+".big_cmd", 0));
        bigIco.setItemMeta(meta);

        smallIco = new ItemStack(Material.DIAMOND);
        meta = bigIco.getItemMeta();
        meta.setLore(lore);
        meta.setDisplayName(viewName);
        meta.setCustomModelData(cfg.getOrSetInt(path+".small_cmd", 0));
        smallIco.setItemMeta(meta);
    }


    public abstract void teleport(Valkiria player);
}


class WorldVisualLocation extends VisualLocation{

    public final Location loc;

    public WorldVisualLocation(Sector cfg, String name) {
        super(cfg, name);
        loc = cfg.getLocation("labels."+name+".loc");
    }

    @Override
    public void teleport(Valkiria player) {
        player.toPlayer().teleport(loc);
    }
}


class ServerVisualLocation extends VisualLocation{

    private final String server;
    private final int dungeonQuestId;

    public ServerVisualLocation(Sector c, String name) {
        super(c, name);//session.setDungeonQuest(questId);
        server = c.getOrSetString("labels."+name+".server", "main");
        dungeonQuestId = c.getOrSetInt("labels."+name+".dungeon_quest_id", -1);
    }


    @Override
    public void teleport(Valkiria player) {
        if(dungeonQuestId>-1) player.getQuestSession().setDungeonQuest(dungeonQuestId);
        Main.floader.bd.Redirect(player.toPlayerName(), server);
    }
}