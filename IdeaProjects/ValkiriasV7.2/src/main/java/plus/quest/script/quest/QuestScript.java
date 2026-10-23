package plus.quest.script.quest;

import plus.quest.AbstractQuestSession;


public interface QuestScript {

    void run(AbstractQuestSession session);

    default boolean runScript(AbstractQuestSession session){
        run(session);
        return false; /*need stop?*/
    }
}