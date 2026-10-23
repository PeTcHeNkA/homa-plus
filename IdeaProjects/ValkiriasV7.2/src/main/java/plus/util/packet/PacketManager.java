package plus.util.packet;

import org.bukkit.entity.Player;
import plus.quest.motion.BasicPos;
import plus.quest.motion.Motion;
import plus.quest.motion.TpMotion;
import java.util.Collection;


public interface PacketManager {

    static PacketManager getGlobal(){
        return GlManager.Global;
    }
    static void setGlobal(PacketManager m){
        GlManager.Global = m;
    }


    void createCamera(Collection<Player> players, int uid, BasicPos loc);
    void createCamera(Player player, int uid, BasicPos loc);


    default void createCamera(Player player, int uid, TpMotion loc){
        createCamera(player, uid, (BasicPos) loc);
    }
    default void createCamera(Collection<Player> players, int uid, TpMotion loc){
        createCamera(players, uid, (BasicPos) loc);
    }


    void setCamera(Player player, int uid);
    void setCamera(Collection<Player> players, int uid);


    void editCamera(Collection<Player> players, int cameraUid, Motion m);
    void editCamera(Player player, int cameraUid, Motion m);


    void teleportCamera(Collection<Player> players, int cameraUid, BasicPos m);
    void teleportCamera(Player player, int cameraUid, BasicPos m);


    void removeCamera(Collection<Player> players, int uid);
    void removeCamera(Player player, int uid);
}


class GlManager{
    protected static PacketManager Global;
}