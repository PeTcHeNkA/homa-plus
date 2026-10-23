package plus.quest.script.quest.trigger;

import plus.quest.AbstractQuestSession;
import plus.util.cfg.Sector;
import plus.quest.script.quest.QuestScript;
import plus.quest.script.quest.QuestScriptFactory;


public class LoadTrigger implements STrigger{

    protected final QuestScript onLoad;
    protected final String ID;

    public LoadTrigger(Sector s, String path) {
        onLoad = QuestScriptFactory.getScript(s, path+".on_load");
        ID = path;
    }

    @Override
    public void onLoad(AbstractQuestSession session) {
        onLoad.run(session);
    }


    @Override
    public void onUnload(AbstractQuestSession session) {}


    public String toString(){
        return ID;
    }
}