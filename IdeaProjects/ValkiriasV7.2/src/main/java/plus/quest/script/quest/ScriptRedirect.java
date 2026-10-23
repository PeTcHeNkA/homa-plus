package plus.quest.script.quest;

import org.bukkit.entity.Player;
import plus.Main;
import plus.quest.AbstractQuestSession;
import plus.quest.script.scene.UniversalScript;
import plus.util.cfg.Sector;


public class ScriptRedirect implements UniversalScript{

    private final String server;
    private final int dungeonQuestId;

    public ScriptRedirect(Sector s, String path){
        server = s.getOrSetString(path+".server", "server");
        dungeonQuestId = s.getOrSetInt(path+".dungeon_quest_id", -1);
    }


    @Override
    public void run(Player player) {
        if(dungeonQuestId>-1){
            Main.floader.getValkiria(player).getQuestSession().setDungeonQuest(dungeonQuestId);
        }
        Main.floader.bd.Redirect(player.getName(), server);
    }


    @Override
    public boolean runScript(AbstractQuestSession session){
        run(session.getPlayer());
        return true;
    }
}