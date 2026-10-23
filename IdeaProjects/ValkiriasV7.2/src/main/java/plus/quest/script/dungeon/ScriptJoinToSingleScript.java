package plus.quest.script.dungeon;

import plus.quest.AbstractQuestSession;
import plus.quest.Quest;
import plus.quest.script.quest.QuestScript;
import plus.quest.script.quest.QuestScriptFactory;
import plus.util.cfg.Sector;


public class ScriptJoinToSingleScript implements QuestScript {

    public final int questId;
    public final QuestScript onLock;

    public ScriptJoinToSingleScript(Sector s, String path){
        questId = s.getOrSetInt(path+".arena_id", 11);
        onLock = QuestScriptFactory.getScript(s, path+".on_lock");
    }


    @Override
    public void run(AbstractQuestSession session) {
        SimplePartyQuest quest = (SimplePartyQuest) Quest.valueOf(questId);
        if(!quest.attemptToTake(session)){
            onLock.run(session);
        }
    }
}