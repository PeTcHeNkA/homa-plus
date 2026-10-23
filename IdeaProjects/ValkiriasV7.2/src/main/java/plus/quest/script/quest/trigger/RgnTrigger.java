package plus.quest.script.quest.trigger;

import plus.quest.AbstractQuestSession;
import plus.util.cfg.Sector;
import plus.quest.script.quest.listener.RegionListener;
import plus.quest.script.quest.listener.RgnListener;


public class RgnTrigger extends Trigger implements RgnListener {

    private final int regionId;
    private final boolean needUnloadAfterActivation;

    public RgnTrigger(Sector s, String path) {
        super(s, path);
        regionId = s.getOrSetInt(path+".region_id", 0);
        needUnloadAfterActivation = !s.getOrSetBoolean(path+".can_repeat", true);
    }


    @Override
    public void onLoad(AbstractQuestSession session) {
        //System.out.println("LOAD "+session.getPlayer().getName()+", id="+regionId);
        RegionListener.addTrigger(session.getPlayer().getName(), regionId, this);
    }

    @Override
    public void onUnload(AbstractQuestSession session) {
        //System.out.println("UNLOAD "+session.getPlayer().getName()+", id="+regionId);
        RegionListener.addTrigger(session.getPlayer().getName(), regionId, null);
    }


    @Override
    public boolean onEnter(AbstractQuestSession session) {
        //System.out.println("ACTIVE "+session.getPlayer().getName()+", id="+regionId);
        run(session);
        return needUnloadAfterActivation;
    }
}