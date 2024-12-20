package plus.fserver.world.addon.pvpworld.games;

import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import org.jetbrains.annotations.Nullable;
import plus.fserver.api.ITextSender;
import plus.fserver.utls.McTsonUtils;
import plus.fserver.world.addon.CleanLobbyTeleporter;
import plus.fserver.world.addon.pvpworld.MinigameProvider;
import plus.fserver.world.addon.pvpworld.MinigameController;
import plus.fserver.world.addon.pvpworld.MinigameWorld;
import plus.fserver.world.addon.pvpworld.stats.WorldData;
import plus.fserver.world.addon.pvpworld.tasks.GiveItems;
import plus.fserver.world.addon.pvpworld.tasks.RespawnAllRandomly;
import plus.tson.TsonMap;
import plus.tson.TsonObj;
import java.util.*;
import static plus.tson.ext.TsonAccessUtils.*;
import static plus.fserver.api.ServerCommand.*;


public class PvpMinigameController extends MinigameController {
    private GameType type;
    private int rounds;
    private boolean isImmutable;
    private int preparationTicks = 20*15;
    private int commonTicks      = 20*90;
    private int timeOut          = 20*45;
    private int winDelay         = 20*2;
    private int nextRDelay       = 20*5;
    private RespawnAllRandomly respawner = new RespawnAllRandomly();
    private ITextSender onAddPlayer;
    private ITextSender onKillPlayerWinner;
    private ITextSender onKillOther;
    private ITextSender onNewRound;
    private ITextSender onFirtsRound;
    private ITextSender onEndCommon;
    private ITextSender onTimeOut;
    private ITextSender onEndPreparation;
    private GiveItems         defaultKit;
    private Map<String,GiveItems> kitMap;

    @Override
    public void init(MinigameProvider.Map.Info pro, TsonMap map) {
        super.init(pro, map);
        onAddPlayer        = pro.getMsg("msg.on-add");
        onKillPlayerWinner = pro.getMsg("msg.on-kill.winner");
        onKillOther        = pro.getMsg("msg.on-kill.other");
        onNewRound         = pro.getMsg("msg.game.new-round");
        onFirtsRound       = pro.getMsg("msg.game.first-round");
        onEndCommon        = pro.getMsg("msg.game.timeout.common");
        onTimeOut          = pro.getMsg("msg.game.timeout.game");
        onEndPreparation   = pro.getMsg("msg.game.timeout.preparation");
    }


    @Override
    public void loadConfigurations(TsonMap map) {
        respawner.fromTson(map.getList("map.positions"));
        type               = McTsonUtils.getGameType(getR(map, "map.gametype"));

        isImmutable        = getR(map, "map.immutable"         , true        );
        preparationTicks   = getR(map, "game.preparation-ticks", preparationTicks);
        commonTicks        = getR(map, "game.common-ticks"     , commonTicks     );
        timeOut            = getR(map, "game.timeout-ticks"    , timeOut         );
        winDelay           = getR(map, "game.win-delay-ticks"  , winDelay        );
        nextRDelay         = getR(map, "game.next-round-delay" , nextRDelay      );
        rounds             = getR(map, "game.def-rounds"       , rounds          );

        TsonObj items      = getR(map, "game.items"                             );
        if(items != null) defaultKit = new GiveItems(items.getList());

        TsonObj kits       = getR(map, "game.kits"                              );

        if(kits != null){
            kitMap = GiveItems.kitMap(kits.getMap());
        }
    }


    @Override
    public void onAddPlayer(EntityPlayerMP player) {
        MinigameWorld mw = (MinigameWorld) player.world;
        onAddPlayer.send(player, mw.getMembers());
    }


    @Override
    public void onRemPlayer(MinigameWorld prev, EntityPlayerMP player) {}


    @Override
    public void onPreRemPlayer(EntityPlayerMP player) {}


    @Override
    public WorldData createData(MinigameWorld world) {
        return new PvWData();
    }


    @Override
    public void onDeath(LivingDeathEvent event) {
        Entity entity = event.getEntity();
        if(entity instanceof EntityPlayerMP){
            EntityPlayerMP player = (EntityPlayerMP) entity;
            MinigameWorld world = (MinigameWorld) entity.world;
            if(!world.isGameMember(player))return;
            CommonWorldData data = world.data();

            event.setCanceled(true);
            player.onPlayerDeath(event.getSource(), false, false);
            player.setGameType(GameType.SPECTATOR);

            if(!data.isTimeOut) {
                data.addDeath(player, 1);
                //Vars.DEATHS.addInt((EntityPlayerMP) entity, 1);

                Entity cause = event.getSource().getTrueSource();
                if (cause != null && cause instanceof EntityPlayerMP) {
                    data.addKill(cause, 1);
                    //Vars.KILLS.addInt((EntityPlayerMP) cause, 1);
                }
            }

            if(nextRound(world)){
                world.clearDelaydTasks(); //todo
                world.addDelayedTask(this::newRound, winDelay);
            } else {
                onComplete(world);
            }
        }
    }


    private boolean nextRound(MinigameWorld world){
        if (this.rounds > 1) {
            CommonWorldData data = world.data();
            return (data.rounds += 1) < this.rounds;
        }
        return false;
    }


    private void newRound(WorldServer worldServer){
        MinigameWorld world = (MinigameWorld) worldServer;
        List<EntityPlayerMP> members = world.getMembers();
        CommonWorldData data = world.data();
        data.isTimeOut = false;

        for (EntityPlayerMP player:members){
            player.setGameType(GameType.SPECTATOR);
        }
        onNewRound.send(members);

        world.cleanEntities();
        world.addDelayedTask(this::onCompletePreparation, nextRDelay);
    }


    @Override
    public void onTick(MinigameWorld world) {
        if(!world.finished()){
            if(world.getMembers().size() > 1){
                onComplete(world);
            }
        }
    }


    @Override
    public boolean onCommand(MinigameWorld world, ICommandSender sender, @Nullable ICommand command, String cmdName, String[] args) {
        if(sender instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) sender;
            if (cmdName.equals("syskit")) {
                PvWData data = world.data();

                if(data.usedKit != null || kitMap == null){
                    reply(sender, "&cChoose kit is locked!");
                } else {
                    PvPlData pData = (PvPlData)data.stats(player);

                    GiveItems kit = kitMap.get(args[0]);
                    if(kit == null && !args[0].equals("null")){
                        reply(sender, "&cKit ["+args[0]+"] not founded!");
                    } else {
                        pData.usedKit = kit;
                    }
                }
                return true;
            }
            return !player.server.getPlayerList().canSendCommands(player.getGameProfile());
        }
        return false;
    }


    @Nullable
    @Override
    public List<String> onCompleteCommand(MinigameWorld world, ICommandSender sender, @Nullable ICommand icommand, String[] args, @Nullable BlockPos pos) {
        if(sender instanceof EntityPlayerMP){
            EntityPlayerMP player = (EntityPlayerMP) sender;

            if(!player.server.getPlayerList().canSendCommands(player.getGameProfile())){
                return Collections.emptyList();
            }
        }
        return super.onCompleteCommand(world, sender, icommand, args, pos);
    }


    @Override
    public void onNewGameInitialized(MinigameWorld world, EntityPlayerMP[][] teams, TsonMap settings) {
        if(settings != null){
            TsonObj items = getR(settings, "game.items");
            if(items != null){
                PvWData data = world.data();
                data.usedKit = new GiveItems(items.getList());
            }
            //todo
//            TsonObj pItems = getR(settings, "game.player-items");
//            if(pItems != null){
//
//            }
        }
    }


    @Override
    public void onNewGameStarted(MinigameWorld world, EntityPlayerMP[] players) {
        onFirtsRound.send(world.getMembers());
        world.addDelayedTask(this::onCompletePreparation, preparationTicks);
    }


    @Override
    public void onComplete(MinigameWorld world) {
        //todo
        CommonWorldData data = world.data();

        WorldData.Results results = data.dumpResults(WorldData.Sorting.SCORE);
        results.get(0); //is winner

        super.onComplete(world);
    }


    private void onCompletePreparation(WorldServer worldServer){
        MinigameWorld world = (MinigameWorld) worldServer;

        List<EntityPlayerMP> members = world.getMembers();
        respawner.respawn(world, members);
        for(EntityPlayerMP player:members){
            CleanLobbyTeleporter.cleanPlayer(player);
            player.setGameType(type);
        }

        giveAllKits(defaultKit, world, members);

        onEndPreparation.send(members);
        world.addDelayedTask(this::onEndCommon, commonTicks);
    }


    public static void giveAllKits(GiveItems defaultKit, MinigameWorld world, List<EntityPlayerMP> members){
        PvWData wData = world.data();
        for (EntityPlayerMP player:members){
            PvPlData pData = (PvPlData) wData.stats(player);
            if(pData.usedKit != null){
                pData.usedKit.accept(player);
            } else {
                if(wData.usedKit != null){
                    wData.usedKit.accept(player);
                } else {
                    if (defaultKit != null)
                        defaultKit.accept(player);
                }
            }
        }
    }


    public static void giveKit(GiveItems defaultKit, EntityPlayerMP player){
        PvWData wData = ((MinigameWorld)player.world).data();
        PvPlData pData = (PvPlData) wData.stats(player);
        if(pData.usedKit != null){
            pData.usedKit.accept(player);
        } else {
            if(wData.usedKit != null){
                wData.usedKit.accept(player);
            } else {
                if (defaultKit != null)
                    defaultKit.accept(player);
            }
        }
    }


    private void onEndCommon(WorldServer worldServer){
        MinigameWorld world = (MinigameWorld) worldServer;
        if(world.finished())return;
        List<EntityPlayerMP> members = world.getMembers();

        for (EntityPlayerMP player:members){
            player.addPotionEffect(new PotionEffect(MobEffects.GLOWING, Integer.MAX_VALUE, 1, true, false));
        }
        onEndCommon.send(members);

        world.addDelayedTask(this::onTimeOut, timeOut);
    }


    private void onTimeOut(WorldServer worldServer){
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


    @Override
    public boolean isImmutable() {
        return type == GameType.ADVENTURE || isImmutable;
    }
}