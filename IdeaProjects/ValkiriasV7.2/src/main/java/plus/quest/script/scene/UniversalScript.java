package plus.quest.script.scene;

import org.bukkit.entity.Player;
import plus.quest.AbstractQuestSession;
import plus.quest.motion.BasicPos;
import plus.quest.script.quest.QuestScript;
import java.util.List;


public interface UniversalScript extends Script, QuestScript {

    void run(Player player);

    @Override
    default void run(Player player, int uid, BasicPos pos){
        run(player);
    }


    @Override
    default void run(AbstractQuestSession session){
        run(session.getPlayer());
    }


    default void run(List<Player> players, int uid, BasicPos pos){
        for(Player player:players)run(player);
    }
}