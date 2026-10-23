package plus.quest.script.quest.listener;

import plus.quest.AbstractQuestSession;
import plus.quest.script.quest.trigger.STrigger;
import plus.util.cmd.CommandArgs;


public interface CmdListener extends STrigger {
    boolean onCommand(AbstractQuestSession session, CommandArgs args);
}