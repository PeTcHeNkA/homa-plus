package plus.quest.script.party;

import org.bukkit.configuration.ConfigurationSection;
import plus.util.cfg.Sector;
import java.util.ArrayList;


public class PartyScriptContiner implements AbstractPartyScript{

    private final AbstractPartyScript[] scripts;

    public PartyScriptContiner(Sector s, String path){
        ConfigurationSection section = s.getConfigurationSection(path + ".scripts");
        if(section==null){
            scripts = new AbstractPartyScript[0];
            s.getOrSetString(path+".scripts.example.type", "script");
        } else {
            ArrayList<AbstractPartyScript> scripts = new ArrayList<>();
            String lPath = path+".scripts.";
            for(String key:section.getKeys(false)){
                AbstractPartyScript script = PartyScript.getScript(s, lPath+key);
                if(script==null)continue;
                scripts.add(script);
            }
            this.scripts = scripts.toArray(new AbstractPartyScript[0]);
        }
    }


    @Override
    public void run(PartySelector selector) {
        for(AbstractPartyScript s:scripts){
            s.run(selector);
        }
    }
}