package plus.quest.script.scene;

import org.bukkit.entity.Player;
import plus.quest.motion.BasicPos;
import plus.quest.motion.TpMotion;
import plus.util.packet.PacketManager;

import java.util.Collection;
import java.util.List;


public class PacketCreateCamera implements Script{

    protected final BasicPos motion;
    protected final boolean isRelative;

    public PacketCreateCamera(BasicPos motion){
        this(motion, true);
    }


    public PacketCreateCamera(BasicPos motion, boolean isRelative) {
        this.motion = motion;
        this.isRelative = isRelative;
    }


    @Override
    public void run(Player player, int uid, BasicPos pos) {
        if(isRelative){
            if(motion instanceof TpMotion m) {
                PacketManager.getGlobal().createCamera(
                        player, uid, new TpMotion(
                                motion.X + pos.X,
                                motion.Y + pos.Y,
                                motion.Z + pos.Z,
                                m.yaw, m.pith
                        )
                );
            } else {
                PacketManager.getGlobal().createCamera(
                        player, uid, new BasicPos(
                                motion.X + pos.X,
                                motion.Y + pos.Y,
                                motion.Z + pos.Z
                        )
                );
            }
        }else PacketManager.getGlobal().createCamera(player, uid, motion);
        PacketManager.getGlobal().setCamera(player, uid);
    }


    @Override
    public void run(Collection<Player> players, int uid, BasicPos pos) {
        if(isRelative){
            if(motion instanceof TpMotion m) {
                PacketManager.getGlobal().createCamera(
                        players, uid, new TpMotion(
                                motion.X + pos.X,
                                motion.Y + pos.Y,
                                motion.Z + pos.Z,
                                m.yaw, m.pith
                        )
                );
            } else {
                PacketManager.getGlobal().createCamera(
                        players, uid, new BasicPos(
                                motion.X + pos.X,
                                motion.Y + pos.Y,
                                motion.Z + pos.Z
                                )
                );
            }
        }else PacketManager.getGlobal().createCamera(players, uid, motion);
        PacketManager.getGlobal().setCamera(players, uid);
    }
}