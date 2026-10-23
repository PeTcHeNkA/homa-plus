package plus.valcirias.data.coder;

import org.bukkit.entity.Player;
import plus.ValkiriaManager;
import plus.valcirias.DungeonValkiria;
import plus.valcirias.data.loader.genetaror.DungeonGenerator;
import plus.valcirias.Valkiria;
import plus.quest.QuestSession;
import java.util.ArrayList;
import java.util.List;


public class DungeonValkiriaEncoder extends BasicCoder implements ValkiriaEncoder {


    public DungeonValkiriaEncoder(ItemCoder coder) {
        super(coder, new DungeonGenerator());
    }


    public Valkiria encode(Player player, List<String> data){
        DungeonValkiria v;
        if(data==null){
            v = (DungeonValkiria) encodeV(null);
            v.setPlayerName(player);
            v.updateAll();
            v.setShield(v.getMaxShield());
            v.setQuestSession(new QuestSession());
            return v;
        } else {
            List<String> rawData = new ArrayList<>();
            v = (DungeonValkiria) encodeV(data.get(0));
            rawData.add(data.get(1)); //0
            v.setArtifacts(c.decodeItems(data.get(2)));
            rawData.add(data.get(2)); //1
            rawData.add(data.get(3)); //2
            rawData.add(data.get(4)); //3

            v.setPlayerName(player);
            v.setReward(data.get(5));
            v.setRawData(rawData);
            if(v.CombatMode()){
                v.setCombatMode(false);
            }
        }
        ValkiriaManager.updateArtifactsStats(v);
        v.updateSwordStats();
        v.updateAll();
        return v;
    }


    public List<String> code(Valkiria v){
        List<String> data = new ArrayList<>();
        List<String> rawData = ((DungeonValkiria)v).getRawData();
        data.add(codeV(v));
        data.add(rawData.get(0));
        data.add(rawData.get(1));
        data.add(rawData.get(2));
        data.add(rawData.get(3));
        data.add(v.getReward());
        return data;
    }
}