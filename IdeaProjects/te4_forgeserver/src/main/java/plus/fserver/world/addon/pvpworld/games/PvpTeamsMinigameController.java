package plus.fserver.world.addon.pvpworld.games;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.world.GameType;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import org.jetbrains.annotations.Nullable;
import plus.fserver.api.ChatPacketBuilder;
import plus.fserver.api.IPlayerDataModel;
import plus.fserver.world.addon.ScorePlayerTeamEX;
import plus.fserver.world.addon.pvpworld.MinigameController;
import plus.fserver.world.addon.pvpworld.MinigameProvider;
import plus.fserver.world.addon.pvpworld.MinigameWorld;
import plus.fserver.world.addon.pvpworld.stats.CombatData;
import plus.fserver.world.addon.pvpworld.stats.PlayerData;
import plus.fserver.world.addon.pvpworld.stats.WorldData;
import plus.tson.TsonMap;
import java.util.List;
import java.util.Set;


public class PvpTeamsMinigameController extends MinigameController {
    public static class ScoreCombatTeam extends ScorePlayerTeamEX {
        public final CombatData data = new CombatData();
        public ScoreCombatTeam(Scoreboard scoreboard, String name) {
            super(scoreboard, name);
        }
    }
    public static class CombatScoreboard extends Scoreboard{
        @Override
        protected ScorePlayerTeam newTeam(String name) {
            return new ScoreCombatTeam(this, name);
        }
    }
    public static class PData extends PlayerData{
        int pendingToRespawn;
    }
    public static class WData extends CommonWorldData{
        @Override
        public PlayerData create() {
            return new PData();
        }
    }

    private int pendingToRespawn = 20*15;
    private boolean canRespawnIfAllDeath = true;

    private ChatPacketBuilder onAddPlayer, onEndPreparation, onTimeOut,
            onEndCommon, onNewRound, onFirtsRound, onKillPlayerWinner, onKillOther;

    @Override
    public void init(MinigameProvider.Map.Info pro, TsonMap map) {
        onAddPlayer        = pro.getMsg("msg.on-add");
        onKillPlayerWinner = pro.getMsg("msg.on-kill.winner-team");
        onKillOther        = pro.getMsg("msg.on-kill.other-team");
        onNewRound         = pro.getMsg("msg.game.new-round");
        onFirtsRound       = pro.getMsg("msg.game.first-round");
        onEndCommon        = pro.getMsg("msg.game.timeout.common");
        onTimeOut          = pro.getMsg("msg.game.timeout.game");
        onEndPreparation   = pro.getMsg("msg.game.timeout.preparation");
    }


    @Override
    public void onAddPlayer(EntityPlayerMP player) {
        Team team = player.getTeam();
        if(team != null) {
            MinigameWorld mw = (MinigameWorld) player.world;
            Packet<INetHandlerPlayClient> msg = onAddPlayer.build(player);

            for (EntityPlayerMP player1 : mw.getMembers()) {
                if(team.isSameTeam(player1.getTeam()))
                    ChatPacketBuilder.send(player1, msg);
            }
        }
    }


    @Nullable
    public static EntityPlayerMP geyFirstAliveMember(ScorePlayerTeam team, MinigameWorld world){
        List<EntityPlayer> players = world.playerEntities;
        Set<String> members = team.getMembershipCollection();
        for(int j2 = 0; j2 < players.size(); ++j2) {
            EntityPlayer player = players.get(j2);
            if(player instanceof EntityPlayerMP && player.isEntityAlive()){
                if(members.contains(player.getName()))
                    return (EntityPlayerMP) player;
            }
        }
        return null;
    }


    @Override
    public WorldData createData(MinigameWorld world) {
        return new WData();
    }


    @Override
    public Scoreboard createScoreboard(MinigameWorld world) {
        return new CombatScoreboard();
    }


    @Override
    public void onNewGameInitialized(MinigameWorld world, EntityPlayerMP[][] teams, TsonMap settings) {;
        Scoreboard score = world.getScoreboard();
        int cursor = 0;
        for (EntityPlayerMP[] team:teams){
            if(team.length == 0)continue;
            ScorePlayerTeam scoreTeam = score.createTeam("Team#" + cursor);
            for (EntityPlayerMP player:team){
                scoreTeam.add(player);
            }
        }
    }


    @Override
    public void onDeath(LivingDeathEvent event) {
        Entity entity = event.getEntity();
        if(entity instanceof EntityPlayerMP){
            EntityPlayerMP dPlayer = (EntityPlayerMP) entity;
            MinigameWorld world = (MinigameWorld) entity.world;
            if(!world.isGameMember(dPlayer))return;
            WData data = world.data();

            event.setCanceled(true);
            dPlayer.onPlayerDeath(event.getSource(), false, false);
            dPlayer.setGameType(GameType.SPECTATOR);

            if(!data.isTimeOut) {
                PData uStats = (PData) data.stats(dPlayer);
                uStats.addDeath(1);
                uStats.addScore(-1);

                ScoreCombatTeam dTeam = (ScoreCombatTeam) dPlayer.getTeam();
                if(dTeam != null){
                    dTeam.data.addDeath(1);
                    dTeam.data.addScore(-1);
                }

                Entity cause = event.getSource().getTrueSource();
                IPlayerDataModel toDTeam;

                if (cause != null && cause instanceof EntityPlayerMP) {
                    data.addKill(cause, 1);

                    int score = 5;
                    if(event.getSource().isProjectile()){
                        score = 8;
                    } else if(event.getSource().isExplosion()){
                        score = 12;
                    } else if(event.getSource().isFireDamage()){
                        score = 4;
                    }

                    data.addScore(cause, score);

                    ScoreCombatTeam wTeam = (ScoreCombatTeam) cause.getTeam();
                    if(wTeam != null){
                        wTeam.data.addKill(1);
                        wTeam.data.addScore(score);
                    }

                    toDTeam = ChatPacketBuilder.model(dPlayer, "killer", cause.getName());

                    IPlayerDataModel toWTeam = ChatPacketBuilder
                            .model((EntityPlayerMP) cause, "target", dPlayer.getName());

                    onKillPlayerWinner.sendToTeam(toWTeam);
                } else {
                    toDTeam = ChatPacketBuilder.model(dPlayer, "killer", "???");
                }
                onKillOther.sendToTeam(toDTeam);

                if(dTeam != null){
                    EntityPlayerMP player2 = geyFirstAliveMember(dTeam, world);
                    boolean needRespawn = pendingToRespawn != -1;
                    if(pendingToRespawn > 20 || !needRespawn) {
                        if (player2 != null)
                            dPlayer.setSpectatingEntity(player2);
                    }
                    if(needRespawn && (canRespawnIfAllDeath || player2 != null)){
                        uStats.pendingToRespawn = pendingToRespawn;
                    } else {
                        uStats.pendingToRespawn = -1;
                    }
                }
            }

//            if(nextRound(world)){
//                world.clearDelaydTasks(); //todo
//                world.addDelayedTask(this::newRound, winDelay);
//            } else {
//                onComplete(world);
//            }
        }
    }
}
