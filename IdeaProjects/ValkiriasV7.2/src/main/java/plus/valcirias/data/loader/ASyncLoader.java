package plus.valcirias.data.loader;

import org.bukkit.entity.Player;
import plus.valcirias.data.UserFloader;
import java.util.ArrayList;
import java.util.List;


public class ASyncLoader implements AbstractValkiriaLoader {

    private final UserFloader f;
    private static boolean Enabled = true;
    private List<Player> queue = new ArrayList<>();

    public ASyncLoader(UserFloader folder){
        this.f = folder;

        Runnable task = () -> {
            while (Enabled){
                try {
                    Thread.sleep(2000);
                    if(!queue.isEmpty()){
                        do {
                            Player p = queue.remove(0);
                            if(p.isOnline()) {
                                f.loadValkiria(p);
                            }
                        } while (!queue.isEmpty());
                    }
                } catch (InterruptedException ignored) {}
            }
        };
        Thread thread = new Thread(task);
        thread.start();
    }


    @Override
    public void loadValkiria(Player player) {
        if(f.hasValkiria(player.getName())){
            Thread thread = new Thread(() -> f.loadValkiria(player));
            thread.start();
        } else {
            player.getInventory().clear();
            queue.add(player);
        }
    }
}