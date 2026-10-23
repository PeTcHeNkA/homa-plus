package plus.quest.script.quest.listener;

import plus.quest.AbstractQuestSession;
import plus.quest.script.quest.trigger.STrigger;


public interface RgnListener extends STrigger {
    boolean onEnter(AbstractQuestSession session);
}