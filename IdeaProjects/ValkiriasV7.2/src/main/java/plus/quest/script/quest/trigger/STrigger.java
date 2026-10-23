package plus.quest.script.quest.trigger;

import plus.quest.AbstractQuestSession;


public interface STrigger {


    void onLoad(AbstractQuestSession session);


    default void load(AbstractQuestSession session){
        if(session.loadTrigger(this)) onLoad(session);

    }


    default void unload(AbstractQuestSession session){
        if(session.unloadTrigger(this)) onUnload(session);
    }


    void onUnload(AbstractQuestSession session);
}