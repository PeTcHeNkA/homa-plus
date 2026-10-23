package plus.quest.script.party;

import plus.quest.memory.AbstractMemorySector;
import plus.quest.memory.QuestMemory;
import plus.util.cfg.Sector;
import plus.valcirias.DungeonValkiria;


public class ScriptPartyMemory implements AbstractPartyScript {

    private final String request;
    private final String type;
    private final String value;


    public ScriptPartyMemory(Sector s, String path){
        request = s.getOrSetString(path+".memory_sector", "example");
        type = s.getOrSetString(path+".type_memory", "int / bool / str");
        value = s.getOrSetString(path+".value", "none");
    }


    @Override
    public void run(PartySelector selector) {
        AbstractMemorySector memSector = QuestMemory.parseMemory(type, value);
        for(DungeonValkiria v:selector.getSessions()){
            v.getQuestSession().getMemory().Sections().put(request, memSector);
        }
    }
}