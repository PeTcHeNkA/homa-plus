package plus.quest.script.scene;

import org.bukkit.entity.Player;
import plus.quest.motion.BasicPos;
import plus.quest.motion.Motion;
import plus.util.packet.PacketManager;
import plus.quest.parser.Parser;
import java.util.Collection;
import java.util.List;


public class PacketMotions implements Script{

    private final Motion[] Motions;
    protected final int delay;

    public PacketMotions(List<String> rawMotions, Parser parser, BasicPos lookTarget, int time, boolean invert, int delay){
        Motions = parser.parse(rawMotions, lookTarget, time, invert);
        this.delay = delay;
    }


    @Override
    public void run(Player player, int uid, BasicPos pos) {
        PacketManager pm = PacketManager.getGlobal();
        for(Motion m:Motions){
            pm.editCamera(player, uid, m);
            sleep();
        }
    }


    private void sleep(){
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void run(Collection<Player> players, int uid, BasicPos pos) {
        PacketManager pm = PacketManager.getGlobal();
        for(Motion m:Motions){
            pm.editCamera(players, uid, m);
            sleep();
        }
    }
}