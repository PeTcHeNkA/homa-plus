package plus.quest.script.quest;

import plus.quest.AbstractQuestSession;
import plus.util.cfg.Sector;
import plus.quest.script.quest.trigger.Trigger;
import java.util.Arrays;
import java.util.HashSet;


public class ScriptLoadTrigger implements QuestScript {

    private HashSet<String> triggersToLoad;
    private HashSet<String> triggersToUnload;

    public ScriptLoadTrigger(Sector s, String path){

        if(s.contains(path+".load")) {
            triggersToLoad = new HashSet<>();
            for (String rawKey : s.getOrSetStringList(path+".load", Arrays.asList("example"))) {
                if(rawKey.isEmpty())continue;
                if(rawKey.charAt(0)!='&') {
                    String[] pt = path.split("\\.");
                    rawKey = pt[0] + "." + pt[1] + "."+pt[2] + "."+rawKey;
                } else {
                    rawKey = rawKey.substring(1);
                }
                triggersToLoad.add(rawKey);
            }
            if(triggersToLoad.isEmpty())triggersToLoad = null;
        }
        if(s.contains(path+".unload")) {
            triggersToUnload = new HashSet<>();
            for (String rawKey : s.getOrSetStringList(path+".unload", Arrays.asList("example"))) {

                if(rawKey.charAt(0)!='&') {
                    String[] pt = path.split("\\.");
                    rawKey = pt[0] + "." + pt[1] + "."+pt[2] + "."+rawKey;
                } else {
                    rawKey = rawKey.substring(1);
                }
                triggersToUnload.add(rawKey);
            }
            if(triggersToUnload.isEmpty())triggersToUnload = null;
        }
        if(triggersToLoad == null && triggersToUnload == null){
            s.getOrSetStringList(path+".load", Arrays.asList("example"));
            s.getOrSetStringList(path+".unload", Arrays.asList("example"));
        }
    }


    @Override
    public void run(AbstractQuestSession session) {
        if(triggersToLoad != null) {
            for (String key : triggersToLoad) {
                Trigger.getOf(key).load(session);
            }
        }
        if(triggersToUnload != null) {
            for (String key : triggersToUnload) {
                Trigger.getOf(key).unload(session);
            }
        }
    }


    @Override
    public boolean runScript(AbstractQuestSession session){
        if(session.getPlayer().isOnline()){
            run(session);
            return false;
        } else return true;
    }
}