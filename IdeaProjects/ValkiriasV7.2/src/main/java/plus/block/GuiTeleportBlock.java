package plus.block;

import org.bukkit.event.player.PlayerInteractEvent;
import plus.block.teleport.GuiTeleport;


public class GuiTeleportBlock implements InteractableBlock{

    protected final GuiTeleport gt;
    public GuiTeleportBlock(String nameGui){
        gt = GuiTeleport.get(nameGui);
    }


    @Override
    public void onPlayerClick(PlayerInteractEvent e) {
        gt.open(e.getPlayer(),0,0);
    }
}
