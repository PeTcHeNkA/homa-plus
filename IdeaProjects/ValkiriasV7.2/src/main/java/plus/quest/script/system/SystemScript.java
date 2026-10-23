package plus.quest.script.system;

import org.bukkit.entity.Player;
import plus.quest.AbstractQuestSession;
import plus.quest.motion.BasicPos;
import plus.quest.script.scene.UniversalScript;
import java.util.List;


public interface SystemScript extends UniversalScript {

    void run();

    @Override
    default void run(Player player){
        run();
    }

    @Override
    default void run(Player player, int uid, BasicPos pos){
        run();
    }


    @Override
    default void run(AbstractQuestSession session){
        run();
    }


    default void run(List<Player> players, int uid, BasicPos pos){
        run();
    }
}