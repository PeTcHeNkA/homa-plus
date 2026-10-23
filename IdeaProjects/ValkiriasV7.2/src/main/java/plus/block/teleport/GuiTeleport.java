package plus.block.teleport;

import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import plus.util.ConfigHelper;
import plus.util.chat.ChatHelper;
import plus.valcirias.pages.ClassSet;
import plus.Main;
import plus.util.cfg.Config;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


public class GuiTeleport {

    protected static String GuiNamePrefix = "tpgui:";
    protected static final HashMap<String, GuiTeleport> hash = new HashMap<>();
    public static Config c;
    protected static TextComponent[] msgLittleEnergy;
    protected static TextComponent[] msgTeleportProcessing;
    protected static ItemStack bg;

    public static void defineStatic(){

        c = new Config(Main.instance, "gui_teleports.yml");
        bg = ConfigHelper.generateMenuItem(c, "system.bg", Material.DIAMOND);
        msgLittleEnergy = ChatHelper.Parse(
                c.getOrSetStringList("system.msg_no_enegry", Arrays.asList("No energy!"))
        );
        msgTeleportProcessing = ChatHelper.Parse(
                c.getOrSetStringList("system.msg_on_teleport", Arrays.asList("Teleported!"))
        );
    }


    public static GuiTeleport get(String name){
        GuiTeleport t = hash.get(name);
        if(t==null){
            GuiEvents.getInstance();
            t = new GuiTeleport(c, name);
            hash.put(name, t);
        }
        return t;
    }


    protected final VisualLocation[] locations;
    protected final String name;

    public GuiTeleport(Config c, String name){
        this.name = name;
        String path = "tpguis."+name;
        List<String> connections = c.getOrSetStringList(path+".connections", Arrays.asList("example"));
        List<VisualLocation> locs = new ArrayList<>();
        for (String key:connections){
            locs.add(VisualLocation.build(c, key));
        }
        locations = locs.toArray(new VisualLocation[0]);
    }


    public void open(Player player, int shift, int sellected){
        player.openInventory(generateInventory(shift, sellected));
    }


    protected Inventory generateInventory(int shift, int sellected){
        if(shift<0 || shift>=locations.length)shift = 0;
        if(sellected<0)sellected = 0;
        else if(sellected>=locations.length)sellected = locations.length-1;
        VisualLocation vs = locations[sellected];
        Inventory inv = Bukkit.createInventory(null, 54, GuiNamePrefix+name+":" + shift + ":" + sellected);

        inv.setItem(45, bg);
        inv.setItem(46, vs.nullLore);
        inv.setItem(47, vs.nullLore);
        inv.setItem(48, vs.nullLore);
        inv.setItem(49, vs.nullLore);
        inv.setItem(50, vs.nullLore);
        inv.setItem(51, vs.nullLore);
        inv.setItem(52, vs.nullLore);
        inv.setItem(53, vs.bigIco);
        inv.setItem(18, locations[shift].smallIco);
        if(shift>0){
            inv.setItem(9, locations[shift-1].smallIco);
        }
        if(shift+1<locations.length){
            inv.setItem(27, locations[shift+1].smallIco);
        }
        return inv;
    }
}