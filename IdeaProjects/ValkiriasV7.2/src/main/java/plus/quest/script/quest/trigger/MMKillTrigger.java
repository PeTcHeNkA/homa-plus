package plus.quest.script.quest.trigger;

import org.bukkit.entity.Player;
import plus.Main;
import plus.quest.AbstractQuestSession;
import plus.quest.memory.AbstractMemorySector;
import plus.util.cfg.Sector;
import plus.quest.memory.MemorySector;
import plus.quest.script.quest.listener.MMKillListener;
import plus.quest.script.quest.listener.MMlistener;


public class MMKillTrigger extends Trigger implements MMlistener {

    private final String mobType;
    private final int count;
    private final String memSection;

    public MMKillTrigger(Sector s, String path){
        super(s, path);
        MMKillListener.define();
        mobType = s.getOrSetString(path+".mob_type", "example");
        count = s.getOrSetInt(path+".count", 10);
        memSection = s.getOrSetString(path+".memory_section", mobType+"_kills");
    }


    @Override
    public void onLoad(AbstractQuestSession session) {
        AbstractMemorySector memSec = session.getMemory().getSection(memSection);
        if(memSec == null){
            if(count>1) session.getMemory().addMemorySection(memSection, new MemorySector(0));
        } else {
            if((Integer)memSec.getValue()>=count){
                run(session);
                return;
            }
        }
        MMKillListener.addTrigger(session.getPlayer().getName(), mobType, this);
    }


    @Override
    public void onUnload(AbstractQuestSession session) {
        MMKillListener.addTrigger(session.getPlayer().getName(), mobType, null);
    }


    @Override
    public void onKill(Player player) {
        AbstractQuestSession session = Main.floader.getValkiria(player).getQuestSession();
        if(count>1) {
            AbstractMemorySector section = session.getMemory().getSection(memSection);
            if(section==null){
                section = new MemorySector(0);
                session.getMemory().addMemorySection(memSection, section);
            }
            Integer i = (Integer) section.getValue() + 1;

            if (i >= count) {
                run(session);
                if(needUnloadAfterActivation) unload(session);//session.unloadTrigger(this);
            } else {
                section.set(i);
            }
        } else {
            run(session);
            if(needUnloadAfterActivation) unload(session);
        }
    }
}