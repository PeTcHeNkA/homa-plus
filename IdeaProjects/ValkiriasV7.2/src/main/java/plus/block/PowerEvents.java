package plus.block;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;


public class PowerEvents implements Listener {

    @EventHandler
    public void onPlayerClick(PlayerInteractEvent e){

        if(e.getAction()==Action.RIGHT_CLICK_BLOCK && e.getHand() == EquipmentSlot.HAND) {
            InteractableBlock block = StationManager.getStation(e.getClickedBlock().getLocation());
            if (block != null) {
                block.onPlayerClick(e);
            }
        }
    }
}