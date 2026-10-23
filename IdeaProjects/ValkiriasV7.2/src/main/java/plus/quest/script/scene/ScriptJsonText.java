package plus.quest.script.scene;

import org.bukkit.entity.Player;
import plus.util.cfg.Sector;
import plus.quest.QuestConfig;


public class ScriptJsonText implements UniversalScript{

    private final String message;

    public ScriptJsonText(Sector s, String path){
        if(s instanceof QuestConfig c) message =
            c.getSingleString(path, ".message", "[\"\",{\"text\":\"Hello World!\"}]");
        else message =
                s.getOrSetString(path+".message", "[\"\",{\"text\":\"Hello World!\"}]");
    }


    @Override
    public void run(Player player) {
        player.sendRawMessage(message);
    }
}