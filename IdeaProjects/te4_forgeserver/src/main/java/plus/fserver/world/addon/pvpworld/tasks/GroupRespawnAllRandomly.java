package plus.fserver.world.addon.pvpworld.tasks;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.scoreboard.Team;
import net.minecraft.world.WorldServer;
import plus.fserver.utls.math.FastRandom;
import plus.fserver.world.addon.LobbyTeleporter;
import plus.tson.TsonList;
import plus.tson.TsonObj;
import java.util.*;


public class GroupRespawnAllRandomly {
    private RespawnAllRandomly[] respawners;

    public GroupRespawnAllRandomly(){}

    public void fromTson(TsonList list){
        respawners = new RespawnAllRandomly[list.size()];
        int i = 0;
        for (TsonObj obj:list){
            (respawners[i] = new RespawnAllRandomly()).fromTson(obj.getList());
            ++i;
        }
    }


    public void tpToRandom(EntityPlayerMP player){
        Random rand = player.world.rand;
        RespawnAllRandomly group = respawners[rand.nextInt(respawners.length)];
        LobbyTeleporter[] tps = group.teleporters;
        tps[rand.nextInt(tps.length)].placeEntity(player.world, player, player.rotationYaw);
    }


    public void tpToNearest(EntityPlayerMP player, double x, double y, double z){
        findNearest(x, y, z).placeEntity(player.world, player, player.rotationYaw);
    }


    public LobbyTeleporter findNearest(double x, double y, double z){
        LobbyTeleporter nearest = null;
        double minDistance = Double.MAX_VALUE;

        for (RespawnAllRandomly rsp:respawners){
            for (LobbyTeleporter check:rsp.teleporters){
                double distance = check.getPos().squareDistanceTo(x, y, z);
                if(distance < minDistance || nearest == null){
                    nearest = check;
                }
            }
        }
        return nearest;
    }


    public void respawnTeams(WorldServer world, List<EntityPlayerMP> players){
        if(players.isEmpty())return;
        Random random = world.rand;

        IdentityHashMap<Team, LinkedList<EntityPlayerMP>> teams = new IdentityHashMap<>();
        RespawnAllRandomly[] respawners = this.respawners;

        for (EntityPlayerMP player:players){
            Team cur = player.getTeam();
            if(cur != null){
                LinkedList<EntityPlayerMP> list = teams.get(cur);
                if(list == null){
                    teams.put(cur, list = new LinkedList<>());
                }
            } else {
                respawners[random.nextInt(respawners.length)].respawn(world, Collections.singletonList(player));
            }
        }
        respawners = respawners.clone();
        FastRandom.shuffle(random, respawners, respawners.length);

        int index = 0;

        for (Map.Entry<Team,LinkedList<EntityPlayerMP>> entry:teams.entrySet()){
            if(index >= respawners.length) index = 0;
            respawners[index].respawn(world, entry.getValue());
            ++index;
        }
    }
}
