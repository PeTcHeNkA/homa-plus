package plus.versions;

import org.bukkit.entity.Player;
import plus.quest.motion.BasicPos;
import plus.quest.script.scene.ScriptPlayMusic;


public abstract class SoundPlayer {

    public static SoundPlayer G;

    public static void loadForCurrentBukkitVersion(){
        try {
            G = new SoundPlayer1_17();
        } catch (Error e){
            e.printStackTrace();
        }
    }


    public abstract void play(ScriptPlayMusic scriptPlayMusic, Player player, BasicPos pos);
}