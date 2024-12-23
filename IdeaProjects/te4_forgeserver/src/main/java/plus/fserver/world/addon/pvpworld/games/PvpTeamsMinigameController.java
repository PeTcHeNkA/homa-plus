package plus.fserver.world.addon.pvpworld.games;

import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import org.jetbrains.annotations.Nullable;
import plus.fserver.api.IPlayerDataModel;
import plus.fserver.api.ITextSender;
import plus.fserver.api.placeholders.PlayerVarProvider;
import plus.fserver.utls.McTsonUtils;
import plus.fserver.world.addon.CleanLobbyTeleporter;
import plus.fserver.world.addon.ScorePlayerTeamEX;
import plus.fserver.world.addon.pvpworld.MinigameController;
import plus.fserver.world.addon.pvpworld.MinigameProvider;
import plus.fserver.world.addon.pvpworld.MinigameWorld;
import plus.fserver.world.addon.pvpworld.stats.CombatData;
import plus.fserver.world.addon.pvpworld.stats.PlayerData;
import plus.fserver.world.addon.pvpworld.stats.WorldData;
import plus.fserver.world.addon.pvpworld.tasks.GiveItems;
import plus.fserver.world.addon.pvpworld.tasks.GroupRespawnAllRandomly;
import plus.tson.TsonMap;
import plus.tson.TsonObj;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static plus.tson.ext.TsonAccessUtils.getR;


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
    public static class PData extends PvPlData{
        int pendingToRespawn;
    }
    public static class WData extends PvWData{
        boolean lockKit = false;
        @Override
        public PlayerData create() {
            return new PData();
        }
    }

    private int pendingToRespawn = 20*15;
    private int preparationTicks = 20*15;
    private int commonTicks      = 20*90;
    private int timeOut          = 20*20;
    private int killsToWin       = 20;
    //private boolean canRespawnIfAllDeath = true;
    private final GroupRespawnAllRandomly respawner = new GroupRespawnAllRandomly();

    private ITextSender onAddPlayer;
    private ITextSender onEndPreparation;
    private ITextSender onTimeOut;
    private ITextSender onEndCommon;
    private ITextSender onNewRound;
    private ITextSender onFirtsRound;
    private ITextSender onKillPlayerWinner;
    private ITextSender onKillOther;

    private GameType gameType;
    private GiveItems defaultKit;
    private Map<String,GiveItems> kitMap;
    private boolean lockKit = true;
    private boolean isImmutable;

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
    public void loadConfigurations(TsonMap map) {
        respawner.fromTson(map.getList("map.gpositions"));
        gameType = McTsonUtils.getGameType(getR(map, "map.gametype"));

        isImmutable        = getR(map, "map.immutable"         , true        );

        pendingToRespawn   = getR(map, "game.respawn-ticks"    , pendingToRespawn);
        preparationTicks   = getR(map, "game.preparation-ticks", preparationTicks);
        commonTicks        = getR(map, "game.common-ticks"     , commonTicks     );
        timeOut            = getR(map, "game.timeout-ticks"    , timeOut         );
        lockKit            = getR(map, "game.lock-kit"         , lockKit         );

        TsonObj items = getR(map, "game.items");
        if(items != null) defaultKit = new GiveItems(items.getList());

        TsonObj kits       = getR(map, "game.kits"                              );

        if(kits != null){
            kitMap = GiveItems.kitMap(kits.getMap());
        }
    }


    private boolean canRespawnIfAllDeath(){
        return killsToWin != -1;
    }


    @Override
    public boolean onCommand(MinigameWorld world, ICommandSender sender, @Nullable ICommand command, String cmdName, String[] args) {
        if(world.finished())return false;
        WData data = world.data();
        if(!data.lockKit && GiveItems.processKitCommand(kitMap, world, sender, command, cmdName, args))
            return true;
        if(sender instanceof EntityPlayerMP)
            return !((EntityPlayerMP) sender).isOp();
        else return false;
    }


    @Override
    public void onAddPlayer(EntityPlayerMP player) {
        Team team = player.getTeam();
        if(team != null) {
            MinigameWorld mw = (MinigameWorld) player.world;
            ArrayList<EntityPlayerMP> players = new ArrayList<>();

            for (EntityPlayerMP player1 : mw.getMembers()) {
                if(team.isSameTeam(player1.getTeam()))
                    players.add(player1);
            }
            onAddPlayer.send(player, players);
        }
    }


    @Override
    public void onPreRemPlayer(EntityPlayerMP player) {
        MinigameWorld world = (MinigameWorld) player.world;
        if(!world.finished()){
            player.onKillCommand();
        }
    }


    @Nullable
    public static EntityPlayerMP getFirstAliveMember(ScorePlayerTeam team, MinigameWorld world){
        List<EntityPlayer> players = world.playerEntities;
        Set<String> members = team.getMembershipCollection();
        for(int j2 = 0; j2 < players.size(); ++j2) {
            EntityPlayer player = players.get(j2);
            if(player instanceof EntityPlayerMP && player.isEntityAlive() && !player.isSpectator()){
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


    private boolean isAllOtherTeamsIsDeath(MinigameWorld world, EntityPlayerMP player){
        List<EntityPlayerMP> members = world.getMembers();
        Team team = player.getTeam();
        for (EntityPlayerMP other:members){
            if(other.isEntityAlive()) {
                if(team != other.getTeam())return false;
            }
        }
        return true;
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
            dPlayer.inventory.clear();
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
                IPlayerDataModel toDTeam = null;

                l1: if (cause != null && cause instanceof EntityPlayerMP) {
                    if(!world.isGameMember((EntityPlayerMP) cause)){
                        cause = null;
                        break l1;
                    }
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

                    toDTeam = PlayerVarProvider.model(dPlayer, "killer", cause.getName());

                    IPlayerDataModel toWTeam = PlayerVarProvider
                            .model((EntityPlayerMP) cause, "target", dPlayer.getName());

                    onKillPlayerWinner.sendToTeam(toWTeam);
                }
                if(toDTeam == null)
                    toDTeam = PlayerVarProvider.model(dPlayer, "killer", "???");

                onKillOther.sendToTeam(toDTeam);

                if(dTeam != null){
                    EntityPlayerMP player2 = getFirstAliveMember(dTeam, world);
                    boolean needRespawn = pendingToRespawn != -1;
                    if(pendingToRespawn > 20 || !needRespawn) {
                        if (player2 != null)
                            dPlayer.setSpectatingEntity(player2);
                    }
                    if(needRespawn && (canRespawnIfAllDeath() || player2 != null)){
                        uStats.pendingToRespawn = pendingToRespawn;
                    } else {
                        uStats.pendingToRespawn = -1;
                    }
                }

                if(!canRespawnIfAllDeath()) {
                    if(cause != null && cause instanceof EntityPlayerMP) {
                        if(isAllOtherTeamsIsDeath(world, (EntityPlayerMP) cause)){
                            onComplete(world, (EntityPlayerMP) cause);
                            return;
                        }
                    }
                } else {
                    ScoreCombatTeam wTeam = (ScoreCombatTeam) cause.getTeam();
                    if(wTeam != null){
                        if(wTeam.data.getKills() >= killsToWin){
                            onComplete(world, (EntityPlayerMP) cause);
                            return;
                        }
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


    public void onComplete(MinigameWorld world, EntityPlayerMP player){
        onComplete(world);
    }


    @Override
    public void onComplete(MinigameWorld world) {
        super.onComplete(world);
    }


    @Override
    public void onNewGameStarted(MinigameWorld world, EntityPlayerMP[] players) {
        onFirtsRound.send(world.getMembers());
        world.addDelayedTask(this::onCompletePreparation, preparationTicks);
    }


    private void onCompletePreparation(WorldServer worldServer){
        MinigameWorld world = (MinigameWorld) worldServer;
        WData data = world.data();
        data.lockKit = lockKit;

        List<EntityPlayerMP> members = world.getMembers();
        respawner.respawnTeams(world, members);

        for(EntityPlayerMP player:members){
            CleanLobbyTeleporter.cleanPlayer(player);
            player.setGameType(gameType);
        }

        PvpMinigameController.giveAllKits(defaultKit, world, members);

        onEndPreparation.send(members);
        world.addDelayedTask(this::onEndCommon, this::onUpdateCommon, commonTicks);
    }


    public void onUpdateCommon(WorldServer worlds) {
        MinigameWorld world = (MinigameWorld) worlds;
        if(world.finished())return;
        WData wData = world.data();
        if(wData.isTimeOut)return;

        List<EntityPlayerMP> members = world.getMembers();

        for (EntityPlayerMP player:members){
            PData data = (PData)wData.stats(player);
            if(data.pendingToRespawn > 0){
                if(--data.pendingToRespawn == 0){
                    EntityPlayerMP player2 = getFirstAliveMember((ScorePlayerTeam)player.getTeam(), world);
                    if(player2 == null && canRespawnIfAllDeath()){
                        respawner.tpToRandom(player);
                        onRespawn(player);
                    } else {
                        if(player2 != null){
                            respawner.tpToNearest(player, player2.posX, player2.posY, player2.posZ);
                            onRespawn(player);
                        } else {
                            data.pendingToRespawn = -1;
                        }
                    }
                }
            }
        }
    }


    private void onEndCommon(WorldServer worldServer) {
        MinigameWorld world = (MinigameWorld) worldServer;
        if(world.finished())return;
        List<EntityPlayerMP> members = world.getMembers();

        for (EntityPlayerMP player:members){
            player.addPotionEffect(new PotionEffect(MobEffects.GLOWING, Integer.MAX_VALUE, 1, true, false));
        }
        onEndCommon.send(members);

        world.addDelayedTask(this::onTimeOut, timeOut);
    }


    private void onTimeOut(WorldServer worldServer) {
        MinigameWorld world = (MinigameWorld) worldServer;
        if(world.finished())return;

        world.finish();
        CommonWorldData data = world.data();
        data.isTimeOut = true;

        List<EntityPlayerMP> members = world.getMembers();

        for (EntityPlayerMP player:members){
            player.onKillCommand();
        }
        onTimeOut.send(members);
    }


    public void onRespawn(EntityPlayerMP player){
        CleanLobbyTeleporter.cleanPlayer(player);
        player.setGameType(gameType);
        PvpMinigameController.giveKit(defaultKit, player);
    }


    @Override
    public boolean isImmutable() {
        return gameType == GameType.ADVENTURE || isImmutable;
    }


    @Override
    public boolean onDropItem(MinigameWorld world, ItemStack item, EntityPlayer player) {
        return false;
    }
}