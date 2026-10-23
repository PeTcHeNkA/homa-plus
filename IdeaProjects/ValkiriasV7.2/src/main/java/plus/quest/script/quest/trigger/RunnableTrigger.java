package plus.quest.script.quest.trigger;

import plus.quest.AbstractQuestSession;
import plus.util.cfg.Sector;


public class RunnableTrigger extends LoadTrigger implements STrigger{

    private final STrigger parent;

    public RunnableTrigger(STrigger parent, Sector s, String path){
        super(s, path);
        this.parent = parent;
    }


    @Override
    public void onLoad(AbstractQuestSession session) {
        parent.onLoad(session);
        super.onLoad(session);
    }


    @Override
    public void onUnload(AbstractQuestSession session) {
        parent.onUnload(session);
    }


    @Override
    public String toString(){
        return parent.toString();
    }
}