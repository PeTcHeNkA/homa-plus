package plus.quest.script.quest;

import org.bukkit.entity.Player;
import plus.Main;
import plus.quest.QuestConfig;
import plus.quest.script.quest.logic.Mather;
import plus.quest.script.scene.UniversalScript;
import plus.util.cfg.Sector;
import plus.valcirias.Valkiria;


public class ScriptDamage implements UniversalScript {

    private final Mather damage;

    public ScriptDamage(Sector s, String path){
        if(s instanceof QuestConfig c){
            damage = Mather.build(c.getSingleString(path, ".amount", "NUM:10"));
        } else damage = Mather.build(s.getOrSetString(path+".amount", "NUM:10"));
    }


    @Override
    public void run(Player player) {
        Valkiria v = Main.floader.getValkiria(player);
        v.damage(damage.getValue(v.getQuestSession()).doubleValue());
    }
}