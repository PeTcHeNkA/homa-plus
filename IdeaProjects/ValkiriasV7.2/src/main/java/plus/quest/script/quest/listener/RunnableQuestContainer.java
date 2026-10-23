package plus.quest.script.quest.listener;

import plus.quest.AbstractQuestSession;
import plus.util.cfg.Sector;
import plus.quest.script.quest.BaseQuestContainer;
import plus.quest.script.quest.QuestScript;


public class RunnableQuestContainer extends BaseQuestContainer implements QuestScript {

    public RunnableQuestContainer(Sector s, String path) {
        super(s, path);
    }

    @Override
    public void run(AbstractQuestSession session){
        new Thread(() -> {
            for (QuestScript script : scripts) {
                if (script.runScript(session)) break;
            }
            Thread.currentThread().stop();
        }).start();
    }
}