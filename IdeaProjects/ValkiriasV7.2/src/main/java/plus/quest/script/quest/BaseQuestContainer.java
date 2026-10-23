package plus.quest.script.quest;

import org.bukkit.configuration.ConfigurationSection;
import plus.util.cfg.Sector;
import java.util.ArrayList;


public class BaseQuestContainer {

    protected final QuestScript[] scripts;


    public BaseQuestContainer(Sector s, String path){
        this(s, path, ".scripts");
    }


    public BaseQuestContainer(Sector s, String path, String scriptPath){
        if(s.isString(path + scriptPath)){
            path = s.getOrSetString(path + scriptPath, "none");
            if(path.equalsIgnoreCase("none")){
                scripts = new QuestScript[0];
                onPostInit(s, path);
                return;
            }
        }
        ConfigurationSection section = s.getConfigurationSection(path + scriptPath);
        if(section==null){
            scripts = new QuestScript[0];
            s.getOrSetString(path + ".scripts.example.type", "chat");
        } else {
            ArrayList<QuestScript> questScripts = new ArrayList<>();
            path = path + scriptPath;
            for(String key:section.getKeys(false)){
                QuestScript questScript = QuestScriptFactory.getScript(s, path +"."+ key);
                if(questScript != null){
                    questScripts.add(questScript);
                }
            }
            scripts = questScripts.toArray(new QuestScript[0]);
        }
        onPostInit(s, path);
    }

    protected void onPostInit(Sector s, String path){}
}