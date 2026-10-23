package plus.block;

import org.bukkit.event.player.PlayerInteractEvent;


public interface InteractableBlock {
    default void regen(){};
    void onPlayerClick(PlayerInteractEvent e);
}
