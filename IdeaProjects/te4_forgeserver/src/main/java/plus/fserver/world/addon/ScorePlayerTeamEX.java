package plus.fserver.world.addon;

import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import plus.tson.utl.Te4HashMap;


public class ScorePlayerTeamEX extends ScorePlayerTeam {
    private Te4HashMap customData;

    public ScorePlayerTeamEX(Scoreboard scoreboard, String name) {
        super(scoreboard, name);
    }


    public Object getCustomRaw(Object key){
        Te4HashMap customData;
        if((customData = this.customData) == null)return null;
        return customData.get(key);
    }


    public <T> T getCustom(Object key){
        return (T) getCustomRaw(key);
    }


    public Te4HashMap customData(){
        return customData;
    }


    public void setCustom(Object key, Object value){
        Te4HashMap customData;
        if((customData = this.customData) == null){
            customData = this.customData = new Te4HashMap<>(8);
        }
        customData.fput(key, value);
    }
}