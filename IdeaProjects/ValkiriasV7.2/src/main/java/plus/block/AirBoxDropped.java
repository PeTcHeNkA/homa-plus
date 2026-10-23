package plus.block;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import plus.chest.Chest;
import plus.util.ConfigHelper;

public class AirBoxDropped implements InteractableBlock{


    protected Inventory lotBox;
    protected boolean finded = false;

    public AirBoxDropped(Chest chest){
        lotBox = chest.getRandomChest();
    }


    @Override
    public void onPlayerClick(PlayerInteractEvent e) {
        if(lotBox.isEmpty()){
            e.getPlayer().spigot().sendMessage(ConfigHelper.empetyChest);
        }
        else {
            e.getPlayer().openInventory(lotBox);
        }
        if(!finded){
            finded = true;
            for(Player p: Bukkit.getOnlinePlayers()){
                p.sendMessage(ConfigHelper.findBoxFormat);
            }
        }
    }
}
