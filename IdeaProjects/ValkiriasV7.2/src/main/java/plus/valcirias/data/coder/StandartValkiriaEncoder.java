package plus.valcirias.data.coder;

import org.bukkit.entity.Player;
import plus.ValkiriaManager;
import plus.valcirias.data.StartItemKit;
import plus.valcirias.data.loader.genetaror.StandartGenerator;
import plus.valcirias.Valkiria;
import plus.quest.QuestSession;
import java.util.ArrayList;
import java.util.List;


public class StandartValkiriaEncoder extends BasicCoder implements ValkiriaEncoder {


    public StandartValkiriaEncoder(ItemCoder coder){
        super(coder, new StandartGenerator());
    }


    public Valkiria encode(Player player, List<String> data){
        Valkiria v;
        if(data==null){
            v = encodeV(null);
            v.setPlayerName(player);
            v.updateAll();
            v.setShield(v.getMaxShield());
            v.setQuestSession(new QuestSession());
            return v;
        } else {
            v = encodeV(data.get(0));
            v.setHItems(c.decodeItems(data.get(1)));
            v.setArtifacts(c.decodeItems(data.get(2)));
            v.setBackPack(c.decodeItems(data.get(3)));
            player.getInventory().setContents(c.decodeItems(data.get(4)));
            v.setPlayerName(player);
            v.setReward(data.get(5));
            if(v.CombatMode()){
                StartItemKit skills = v.getSKit();
                player.getInventory().setItem(0, v.getWeapon());
                player.getInventory().setItem(7, skills.NORMAL_SKILL);
                player.getInventory().setItem(8, skills.HEIGHT_SKILL);
            }
        }
        ValkiriaManager.updateArtifactsStats(v);
        v.updateSwordStats();
        v.updateAll();
        return v;
    }


    public List<String> code(Valkiria v){
        List<String> data = new ArrayList<>();
        data.add(codeV(v));
        data.add(c.codeItems(v.getHItems()));
        data.add(c.codeItems(v.getArtifacts()));
        data.add(c.codeItems(v.getBackPck()));
        data.add(c.codeItems(v.toPlayer().getInventory().getContents()));
        //data.add(BinaryLib.BooleanListToString(v.getQuestHistory()));
        data.add(v.getReward());
        return data;
    }

}