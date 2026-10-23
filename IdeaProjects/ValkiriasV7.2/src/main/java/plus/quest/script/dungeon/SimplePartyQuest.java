package plus.quest.script.dungeon;

import plus.quest.script.party.AbstractPartyScript;
import plus.quest.script.party.PartySelector;
import plus.quest.script.party.PartyScript;
import plus.quest.AbstractQuestSession;
import plus.quest.DungeonQuestSession;
import plus.quest.Quest;
import plus.util.cfg.Sector;
import plus.valcirias.DungeonValkiria;


public class SimplePartyQuest extends Quest {

    protected final AbstractPartyScript onComplete;
    protected final AbstractPartyScript onTimeOut;
    protected final AbstractPartyScript onTake;
    protected final PartySelector selector;
    protected boolean competed = true;
    protected boolean blocked = false;
    protected int maxTime;
    protected Thread t;

    public SimplePartyQuest(Sector s, String path) {
        super(s, path);
        maxTime = s.getOrSetInt(path+".max_time", 300000);
        selector = PartySelector.build(this, s.getOrSetString(path+".party_selector", "none"));

        onComplete = PartyScript.getScript(s, path+".on_complete");
        onTimeOut = PartyScript.getScript(s, path+".on_time_out");
        onTake = PartyScript.getScript(s, path+".on_take");
    }


    public boolean attemptToTake(AbstractQuestSession session){
        if(blocked)return false;
        else return session.attemptTakeQuest(this);
    }


    @Override
    public void onTake(AbstractQuestSession sessionStarter){
        blocked = true;
        t = new Thread(() -> {
            competed = false;
            for(DungeonValkiria v:selector.selectPlayers(sessionStarter)){
                DungeonQuestSession session = (DungeonQuestSession) v.getQuestSession();
                Default.load(session);
                session.setSelector(selector);
            }
            onTake.run(selector);

            try { Thread.sleep(maxTime);
            } catch (InterruptedException ignored) {}
            if(!competed) {
                onTimeOut.run(selector);
                unLock();
            }
        });
        t.start();
    }


    public void unLock(){
        if(blocked) {
            competed = true;
            if (t != null && t.isAlive()) {
                t.stop();
            }
            for(DungeonValkiria v:selector.getSessions()){
                DungeonQuestSession session = (DungeonQuestSession) v.getQuestSession();
                session.setSelector(null);
            }
            blocked = false;
            selector.clear();
        }
    }


    public void complete(){
        onComplete.run(selector);
        unLock();
    }


    public boolean isBlocked(){
        return blocked;
    }


    public PartySelector getSelector(){
        return selector;
    }
}