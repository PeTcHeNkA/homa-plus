package plus.quest.script.party;

import plus.quest.QuestConfig;
import plus.quest.Scene;
import plus.quest.motion.BasicPos;
import plus.quest.motion.TpMotion;
import plus.util.cfg.Sector;


public class ScriptPartyScene implements AbstractPartyScript {

    private final String sceneName;
    private final BasicPos motion;

    public ScriptPartyScene(Sector s, String path){
        if(s instanceof QuestConfig c){
            sceneName = c.getSingleString(path, ".scene", "scene");
        } else sceneName = s.getOrSetString(path+".scene", "scene");
        if(s.contains(path+".basic_bos")){
            motion = TpMotion.build(s.getString(path+".basic_bos"));
        } else motion = new BasicPos(0,0,0);
    }


    @Override
    public void run(PartySelector selector) {
        Scene.run(sceneName, selector.toPlayers(), motion);
    }
}