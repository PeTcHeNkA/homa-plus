package plus.quest.script.quest;

import plus.quest.AbstractQuestSession;
import plus.util.cfg.Sector;
import plus.quest.motion.TpMotion;
import plus.quest.script.scene.ScriptContainer;


public class ScriptScene extends ScriptContainer implements QuestScript{

    public ScriptScene(Sector c, String path){
        super(c, path);
    }


    @Override
    public void run(AbstractQuestSession session) {
        super.run(session.getPlayer(), session.getCameraUid(), TpMotion.build(session.getPlayer().getLocation()));
    }
}