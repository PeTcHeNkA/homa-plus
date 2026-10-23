package plus.quest.script.quest;

import plus.quest.AbstractQuestSession;
import plus.quest.script.system.ScriptQuestContainer;
import plus.util.cfg.Sector;
import java.util.ArrayList;
import java.util.HashMap;


public class ScriptProtectedContainer extends ScriptQuestContainer {

    protected static final HashMap<String, ArrayList<String>> syncCache = new HashMap<>();
    protected ArrayList<String> blocked;

    public ScriptProtectedContainer(Sector s, String path) {
        super(s, path);
    }


    @Override
    public void onPostInit(Sector s, String path){
        blocked = syncCache.computeIfAbsent(path, k -> new ArrayList<>());
    }


    @Override
    public void runSync(AbstractQuestSession session) {
        String player = session.getPlayer().getName();
        if(!blocked.contains(player)) {
            blocked.add(player);
            super.runSync(session);
            blocked.remove(player);
        }
    }


    @Override
    public void run(AbstractQuestSession session){
        if(useSync){
            runSync(session);
        } else {
            String player = session.getPlayer().getName();
            if(!blocked.contains(player)) {
                new Thread(() -> {
                    blocked.add(player);
                    super.runSync(session);
                    blocked.remove(player);
                    Thread.currentThread().stop();
                }).start();
            }
        }
    }
}