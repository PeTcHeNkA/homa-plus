package plus.quest.script.scene;

import org.bukkit.entity.Player;
import plus.quest.motion.BasicPos;
import java.util.Collection;


public interface Script {

    void run(Player player, int uid, BasicPos pos);


    default void run(Collection<Player> players, int uid, BasicPos pos){
        for(Player player:players)run(player, uid, pos);
    }
}
