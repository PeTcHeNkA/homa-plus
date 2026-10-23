package plus.quest.script.dungeon;

import org.bukkit.entity.Player;
import plus.quest.AbstractQuestSession;
import plus.quest.DungeonQuestSession;
import plus.quest.script.party.AbstractPartyScript;
import plus.quest.script.party.PartyScript;
import plus.quest.script.party.PartySelector;
import plus.quest.script.quest.QuestScript;
import plus.util.cfg.Sector;
import java.util.Collections;
import java.util.List;


public class ScriptRunForSelector implements QuestScript {

    public final AbstractPartyScript script;

    public ScriptRunForSelector(Sector s, String path){
        script = PartyScript.getScript(s, path+".script");
    }


    @Override
    public void run(AbstractQuestSession session) {
        if(session instanceof DungeonQuestSession s){
            script.run(s.getSelector());
        } else {
            script.run(new PartySelector(null) {
                @Override
                public List<Player> select(AbstractQuestSession session) {
                    return Collections.singletonList(session.getPlayer());
                }
            });
        }
    }
}