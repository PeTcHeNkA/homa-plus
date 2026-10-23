package plus.quest.script.scene;

import org.bukkit.entity.Player;
import plus.quest.motion.BasicPos;
import plus.quest.motion.TpMotion;
import plus.util.packet.PacketManager;
import java.util.Collection;


public class PacketTeleport implements Script{

    protected final BasicPos motion;
    protected final boolean isRelative;

    public PacketTeleport(BasicPos m){
        this(m, true);
    }


    public PacketTeleport(BasicPos m, boolean isRelative){
        this.motion = m;
        this.isRelative = isRelative;
    }


    @Override
    public void run(Player player, int uid, BasicPos pos) {
        if(isRelative){
            if(motion instanceof TpMotion m) {
                PacketManager.getGlobal().teleportCamera(
                        player, uid, new TpMotion(
                                motion.X + pos.X,
                                motion.Y + pos.Y,
                                motion.Z + pos.Z,
                                m.yaw, m.pith
                        )
                );
            } else {
                PacketManager.getGlobal().teleportCamera(
                        player, uid, new BasicPos(
                                motion.X + pos.X,
                                motion.Y + pos.Y,
                                motion.Z + pos.Z
                        )
                );
            }
        }else PacketManager.getGlobal().teleportCamera(player, uid, motion);
    }


    @Override
    public void run(Collection<Player> players, int uid, BasicPos pos) {
        if(isRelative){
            if(motion instanceof TpMotion m) {
                PacketManager.getGlobal().teleportCamera(
                        players, uid, new TpMotion(
                                motion.X + pos.X,
                                motion.Y + pos.Y,
                                motion.Z + pos.Z,
                                m.yaw, m.pith
                        )
                );
            } else {
                PacketManager.getGlobal().teleportCamera(
                        players, uid, new BasicPos(
                                motion.X + pos.X,
                                motion.Y + pos.Y,
                                motion.Z + pos.Z
                        )
                );
            }
        }else PacketManager.getGlobal().teleportCamera(players, uid, motion);
    }
}