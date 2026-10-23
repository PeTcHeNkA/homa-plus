package plus.block;

import org.bukkit.event.player.PlayerInteractEvent;
import plus.ValkiriaManager;

public class ModifitycationTable implements InteractableBlock{


    @Override
    public void regen() {
    }

    @Override
    public void onPlayerClick(PlayerInteractEvent e) {
        ValkiriaManager.OpenArtifactUpgrade(e.getPlayer());
    }

}
