package plus.valcirias;

import plus.quest.AbstractQuestSession;
import plus.quest.QuestSession;


public interface QuestCarrier {

    void setQuestSession(AbstractQuestSession session);

    default void setQuestSession(String data){
        if(data==null || data.equalsIgnoreCase("null")){
            setQuestSession(new QuestSession());
        } else {
            setQuestSession(new QuestSession(data));
        }
    }


    AbstractQuestSession getQuestSession();
}