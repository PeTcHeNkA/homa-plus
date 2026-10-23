package plus.block;

import org.bukkit.event.player.PlayerInteractEvent;
import plus.Main;


public class RedirectMarker implements InteractableBlock{

    protected final String server;
    protected final String coedLoc;
    public RedirectMarker(String server, String codedLoc){
        this.server = server;
        this.coedLoc = codedLoc;
    }


    @Override
    public void onPlayerClick(PlayerInteractEvent e) {
        Main.floader.bd.Redirect(e.getPlayer().getName(), server);
        Main.floader.bd.tpSender.teleportPlayer(e.getPlayer().getName(), coedLoc);
    }
}
