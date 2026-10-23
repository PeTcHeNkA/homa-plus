package plus.chest.reward;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;
import plus.valcirias.Valkiria;
import plus.Main;
import java.util.ArrayList;
import java.util.List;


public class RewardEvents implements Listener {
    private final RewardBoxFolder f;

    private final List<String> playerInProcess = new ArrayList<>();


    public RewardEvents(RewardBoxFolder f) {
        this.f = f;
    }


    @EventHandler
    public void onPlayerClick(final PlayerInteractEvent e){
        if(e.getAction()== Action.RIGHT_CLICK_BLOCK) {
            if(e.getClickedBlock()==null)return;
            if(playerInProcess.contains(e.getPlayer().getName()))return;

            RewardBox box = f.getRewardMox(e.getClickedBlock().getLocation());

            if (box != null) {
                Valkiria v = Main.floader.getValkiria(e.getPlayer());
                box.rewardS(v);
                playerInProcess.remove(e.getPlayer().getName());

                Thread t = new Thread(() -> {
                    try {
                        Thread.sleep(300);
                    }catch (Exception ignored){}
                    if(e.getPlayer().isOnline()) {
                        Player pl = e.getPlayer();
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                pl.kickPlayer("");
                            }
                        }.runTaskLater(Main.instance, 0);
                    }
                    playerInProcess.remove(e.getPlayer().getName());
                });
                t.start();
            }
        }
    }
}