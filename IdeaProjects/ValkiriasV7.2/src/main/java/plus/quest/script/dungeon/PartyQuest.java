package plus.quest.script.dungeon;

import plus.quest.AbstractQuestSession;
import plus.quest.Quest;
import plus.util.cfg.Sector;


public class PartyQuest extends SimplePartyQuest {

    protected final int[] alternativeIds;

    public PartyQuest(Sector s, String path){
        super(s, path);
        String raw = s.getOrSetString(path+".alternative_quests", "none");
        if(raw.equalsIgnoreCase("none"))alternativeIds = new int[0];
        else {
            String[] rawData = raw.split(",");
            alternativeIds = new int[rawData.length];
            for(int i=0;i<rawData.length;i++){
                alternativeIds[i] = Integer.parseInt(rawData[i]);
            }
        }
    }


    @Override
    public boolean attemptToTake(AbstractQuestSession session){
        if(blocked){
            for(int id:alternativeIds){
                Quest quest = Quest.valueOf(id);
                if(quest==null)continue;
                if(quest instanceof SimplePartyQuest q){
                    if(!q.blocked){
                        return session.attemptTakeQuest(q);
                    }
                }
            }
            return false;
        } else return session.attemptTakeQuest(this);
    }
}