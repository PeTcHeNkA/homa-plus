package plus.quest.script.quest.trigger;

import plus.quest.AbstractQuestSession;
import plus.quest.script.quest.QuestScript;
import plus.quest.script.quest.QuestScriptFactory;
import plus.quest.script.quest.ScriptProtectedContainer;
import plus.util.cfg.Sector;
import java.util.ArrayList;


public class ScriptBlockedProtectedContainer extends ScriptProtectedContainer {

    private final String blockKey;
    private final QuestScript onBlock;

    public ScriptBlockedProtectedContainer(Sector s, String path){
        super(s, path);
        blockKey = s.getOrSetString(path+".block_key", path);
        onBlock = QuestScriptFactory.getScript(s, path+".on_block");
    }


    @Override
    public void onPostInit(Sector s, String path){
        blocked = syncCache.computeIfAbsent(blockKey, k -> new ArrayList<>());
    }


    @Override
    public void runSync(AbstractQuestSession session) {
        String player = session.getPlayer().getName();
        if(!blocked.contains(player)) {
            blocked.add(player);
            super.runSync(session);
            blocked.remove(player);
        } else {
            onBlock.runScript(session);
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
            } else {
                onBlock.runScript(session);
            }
        }
    }
}