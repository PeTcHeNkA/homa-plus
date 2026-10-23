package plus.valcirias.data.loader;

import org.bukkit.entity.Player;
import plus.valcirias.data.UserFloader;


public class SyncLoader implements AbstractValkiriaLoader {

    private final UserFloader f;
    public SyncLoader(UserFloader f){
        this.f = f;
    }

    @Override
    public void loadValkiria(Player player) {
        if(!f.hasValkiria(player.getName())){
            player.getInventory().clear();
        }
        new Thread(() -> f.loadValkiria(player)).start();
    }
}