package plus.quest.script.quest;

import org.bukkit.entity.Player;
import plus.Main;
import plus.quest.QuestConfig;
import plus.quest.script.scene.UniversalScript;
import plus.util.cfg.Sector;
import plus.valcirias.Valkiria;


public class ScriptTakeBattlePass implements UniversalScript {

    private final int count;
    public ScriptTakeBattlePass(Sector s, String path){
        if(s instanceof QuestConfig c){
            count = c.getSingleInt(path, ".amount", 1);
        } else count = s.getOrSetInt(path+".amount", 1);
    }


    @Override
    public void run(Player player) {
        Valkiria v = Main.floader.getValkiria(player);
        v.setCountBattlePass(v.getCountBattlePass()-count);
    }
}
