package plus.quest.script.quest.trigger;

import plus.quest.AbstractQuestSession;
import plus.util.cfg.Sector;
import plus.quest.script.quest.listener.CmdListener;
import plus.quest.script.quest.listener.CommandListener;
import plus.util.cmd.CommandArgs;


public class CmdTrigger extends Trigger implements CmdListener {

    private final String keyActivation;

    public CmdTrigger(Sector s, String path) {
        super(s, path);
        keyActivation = s.getOrSetString(path+".activation_key", "example");
    }


    @Override
    public void onLoad(AbstractQuestSession session) {
        CommandListener.addCmdListener(session.getPlayer().getName(), keyActivation, this);
    }


    @Override
    public void onUnload(AbstractQuestSession session) {
        CommandListener.addCmdListener(session.getPlayer().getName(), keyActivation, null);
    }


    @Override
    public boolean onCommand(AbstractQuestSession session, CommandArgs args) {
        run(session);
        return needUnloadAfterActivation;
    }
}