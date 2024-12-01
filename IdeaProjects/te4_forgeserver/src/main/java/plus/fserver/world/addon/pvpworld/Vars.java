package plus.fserver.world.addon.pvpworld;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.scoreboard.Team;
import plus.fserver.utls.math.IntRef;
import plus.fserver.world.addon.ScorePlayerTeamEX;
import plus.tson.utl.Te4HashMap;


@Deprecated
public enum Vars {
    WINNER, LOST, DATA, FUTURE, ROUND, USERDATA, KILLS, DEATHS, SCORE, PHASE, TIMEOUT;

//    public <T> T get(MinigameWorld world){
//        return world.getVar(this);
//    }
//
//    public void set(MinigameWorld world, Object value){
//        world.setVar(this, value);
//    }
//
//
//    public <T> T get(EntityPlayerMP player){
//        MinigameWorld world = (MinigameWorld) player.world;
//        return (T) world.data(player).get(this);
//    }
//
//
//    public void set(EntityPlayerMP player, Object value){
//        MinigameWorld world = (MinigameWorld) player.world;
//        world.data(player).fput(this, value);
//    }
//
//
//    public void set(Team team, Object value){
//        ((ScorePlayerTeamEX)team).setCustom(this, value);
//    }
//
//
//    public <T> T get(Team team){
//        return ((ScorePlayerTeamEX)team).getCustom(this);
//    }
//
//
//    private int addInt(Te4HashMap data, int num){
//        IntRef ref = (IntRef) data.get(this);
//        if(ref == null){
//            ref = new IntRef(num);
//            data.fput(this, ref);
//            return num;
//        } else {
//            ref.add(num);
//            return ref.get();
//        }
//    }
//
//
//    public int getInt(Te4HashMap data){
//        if(data == null)return 0;
//        IntRef ref = (IntRef) data.get(this);
//        if(ref == null)return 0;
//        return ref.get();
//    }
//
//
//    public int addInt(MinigameWorld world, int num){
//        return addInt(world.data(), num);
//    }
//
//
//    public int getInt(MinigameWorld world){
//        return getInt(world.data());
//    }
//
//
//    public int addInt(EntityPlayerMP player, int num){
//        return addInt(((MinigameWorld) player.world).data(player), num);
//    }
//
//
//    public int getInt(EntityPlayerMP player){
//        return getInt(((MinigameWorld) player.world).getDataIfExists(player));
//    }
//
//
//    public int getInt(Team team){
//        return getInt(((ScorePlayerTeamEX)team).customData());
//    }
//
//
//    public int addInt(Team team, int num){
//        return addInt(((ScorePlayerTeamEX)team).customData(), num);
//    }
}