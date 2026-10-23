package plus.quest.script.scene;

import org.bukkit.entity.Player;
import plus.quest.motion.BasicPos;
import plus.util.packet.PacketManager;
import java.util.Collection;


public class ScriptRemoveCamera implements Script{

    private static ScriptRemoveCamera I;

    public static ScriptRemoveCamera getInstance(){
        if(I==null)I = new ScriptRemoveCamera();
        return I;
    }


    @Override
    public void run(Player player, int uid, BasicPos pos) {
        PacketManager pm = PacketManager.getGlobal();
        pm.removeCamera(player, uid);
    }


    @Override
    public void run(Collection<Player> players, int uid, BasicPos pos) {
        PacketManager pm = PacketManager.getGlobal();
        pm.removeCamera(players, uid);
    }
}