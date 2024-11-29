package plus.fserver.world.addon;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;


public class ScorePlayerTeamEX extends ScorePlayerTeam {
    private Object2ObjectOpenHashMap<String,Object> customData;

    public ScorePlayerTeamEX(Scoreboard scoreboard, String name) {
        super(scoreboard, name);
    }


    public Object getCustomRaw(String key){
        Object2ObjectOpenHashMap<String,?> customData;
        if((customData = this.customData) == null)return null;
        return customData.get(key);
    }


    public <T> T getCustom(String key){
        return (T) getCustomRaw(key);
    }


    public void setCustom(String key, Object value){
        Object2ObjectOpenHashMap<String,Object> customData;
        if((customData = this.customData) == null){
            customData = this.customData = new Object2ObjectOpenHashMap<>(8);
        }
        customData.put(key, value);
    }
}