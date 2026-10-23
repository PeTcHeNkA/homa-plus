package plus.quest.script.scene;

import org.bukkit.entity.Player;
import plus.util.cfg.Sector;
import plus.quest.motion.BasicPos;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class ScriptContainer implements Script{

    protected final Script[] scripts;
    protected final int contRepeat;
    protected final boolean isParallel;

    public ScriptContainer(Sector c, String path){
        List<Script> scripts = new ArrayList<>();

        try {
            for(String key:c.getConfigurationSection(path+".scripts").getKeys(false)){
                Script s = ScriptFactory.getScript(c, path+".scripts."+key);
                if(s != null)scripts.add(s);
            }
        } catch (NullPointerException e){
            c.set(path+".scripts.example.type", "motion");
            scripts.add(ScriptFactory.getScript(c, path+".scripts.example"));
        }

        this.scripts = scripts.toArray(new Script[0]);
        contRepeat = c.getOrSetInt(path+".count_repeat", 1);
        isParallel = c.getOrSetBoolean(path+".is_parallel", true);
    }


    @Override
    public void run(Player player, int uid, BasicPos pos) {
        if(isParallel) {
            new Thread(() -> {
                for (Script s : scripts) {
                    s.run(player, uid, pos);
                }
            }).start();
        } else {
            for (Script s : scripts) {
                s.run(player, uid, pos);
            }
        }
    }


    @Override
    public void run(Collection<Player> players, int uid, BasicPos pos) {
        if(isParallel) {
            new Thread(() -> {
                for (Script s : scripts) {
                    s.run(players, uid, pos);
                }
            }).start();
        } else {
            for (Script s : scripts) {
                s.run(players, uid, pos);
            }
        }
    }
}