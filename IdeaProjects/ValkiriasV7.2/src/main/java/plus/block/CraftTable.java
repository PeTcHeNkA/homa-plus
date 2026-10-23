package plus.block;

import org.bukkit.event.player.PlayerInteractEvent;
import plus.system.CraftModule;


public class CraftTable implements InteractableBlock{


    @Override
    public void onPlayerClick(PlayerInteractEvent e) {
        CraftModule.craftTable.Open(e.getPlayer(), -1, 0);
    }
}