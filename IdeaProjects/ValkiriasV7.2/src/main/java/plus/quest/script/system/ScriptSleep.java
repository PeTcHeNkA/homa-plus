package plus.quest.script.system;

import plus.util.cfg.Sector;
import plus.quest.QuestConfig;


public class ScriptSleep implements SystemScript {

    private final int delay;

    public ScriptSleep(Sector s, String path) {
        if(s instanceof QuestConfig c){
            this.delay = c.getSingleInt(path, ".time", 500);
        } else this.delay = s.getOrSetInt(path+".time", 500);
    }


    @Override
    public void run() {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}