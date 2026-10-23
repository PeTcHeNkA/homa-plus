package plus.quest.script.scene;

import org.bukkit.entity.Player;
import plus.util.cfg.Sector;
import plus.quest.motion.BasicPos;
import java.util.Collection;


public class ScriptEditRelativePos implements Script{

    protected final BasicPos pos;
    protected final boolean isRelative;

    public ScriptEditRelativePos(Sector s, String path){
        this.pos = new BasicPos(s.getOrSetString(path+".location", "0;0;0"));
        this.isRelative = s.getOrSetBoolean(path+".relative", true);
    }


    private void editPos(BasicPos pos){
        if(isRelative){
            pos.edit(this.pos.X+pos.X, this.pos.Y+pos.Y, this.pos.Z+pos.Z);
        } else {
            pos.edit(this.pos.X, this.pos.Y, this.pos.Z);
        }
    }


    @Override
    public void run(Player player, int uid, BasicPos pos) {
        editPos(pos);
    }


    @Override
    public void run(Collection<Player> players, int uid, BasicPos pos) {
        editPos(pos);
    }
}