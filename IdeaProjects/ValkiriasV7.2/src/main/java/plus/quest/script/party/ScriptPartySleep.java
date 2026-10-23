package plus.quest.script.party;

import plus.quest.QuestConfig;
import plus.util.cfg.Sector;


public class ScriptPartySleep implements AbstractPartyScript {

    private final int time;

    public ScriptPartySleep(Sector s, String path){
        if(s instanceof QuestConfig c){
            time = c.getSingleInt(path, ".time", 1000);
        } else time = s.getOrSetInt(path+".time", 1000);
    }


    @Override
    public void run(PartySelector selector) {
        try { Thread.sleep(time);
        } catch (InterruptedException ignored) {}
    }
}