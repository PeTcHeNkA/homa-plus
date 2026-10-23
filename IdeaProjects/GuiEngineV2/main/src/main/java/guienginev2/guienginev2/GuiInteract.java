package guienginev2.guienginev2;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Random;


public class GuiInteract implements Listener {

    Random r = new Random();

    public int maxX = 1000;
    public int minX = -1000;

    public List<World> w;

    public Location HashLoc;
    public Inventory inv;

    public Location getRandomLoc(){

        int Rx = getRandomPos();
        int Ry = getRandomPos();

        BlockIterator b = new BlockIterator(getRandomWorld(), new Vector(Rx, 254, Ry), new Vector(Rx, 255, Ry), 0, 255);
        //Location l = b.next().getLocation();
        return b.next().getLocation();
        //Player p = null;
        //p.getTargetBlock(null, 10);
        //l.setY(l.getY()+1);
        //l.getBlock().setType(Material.CHEST);
    }
    public int getRandomPos(){
        return r.nextInt(maxX-minX)+minX;
    }
    public World getRandomWorld(){
        return w.get(r.nextInt(w.size()));
    }

    public Location getReqLoc(){
        boolean Nreq = true;
        while (Nreq){
            Location l = getRandomLoc();
            if(l.getBlock().getType() != Material.WATER)return l;
        }
        return null;
    }

    @EventHandler
    public void onClick(PlayerInteractEvent e){
        if(HashLoc==null)return;
        if(e.getClickedBlock().getLocation().equals(HashLoc))
            e.getPlayer().openInventory(inv);
    }
    public void generateBlock(){
        Location l = getReqLoc();
        l.setY(l.getY()+1);
        l.getBlock().setType(Material.CHEST);
        HashLoc = l;
        inv = generateRandomInv();
    }
    public Inventory generateRandomInv(){
        return null;
    }

}
