package plus.quest.script.system;

import plus.quest.AbstractQuestSession;
import plus.quest.script.quest.BaseQuestContainer;
import plus.quest.script.quest.QuestScript;
import plus.util.cfg.Sector;


public class ScriptQuestContainer extends BaseQuestContainer implements SystemScript {

    protected final boolean useSync;

    public ScriptQuestContainer(Sector s, String path){
        super(s, path);
        useSync = s.getOrSetBoolean(path+".synchronous", false);
    }


    public void runSync(AbstractQuestSession session) {
        for (QuestScript script : scripts) {
            if (script.runScript(session)) break;
        }
    }


    @Override
    public void run() {
        for (QuestScript script : scripts) {
            if (script.runScript(null)) break;
        }
    }


    @Override
    public void run(AbstractQuestSession session){
        if(useSync){
            runSync(session);
        } else {
            new Thread(() -> {
                runSync(session);
                Thread.currentThread().stop();
            }).start();
        }
    }
}