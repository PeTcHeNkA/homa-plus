package plus.fserver.world.addon.pvpworld.games;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import plus.fserver.world.addon.pvpworld.MinigameController;
import plus.fserver.world.addon.pvpworld.MinigameWorld;
import plus.fserver.world.addon.pvpworld.stats.WorldData;
import plus.fserver.world.addon.pvpworld.tasks.RespawnAllRandomly;
import java.util.*;


public class PvpMinigameController extends MinigameController {
    private GameType type;
    private int rounds;
    private boolean isImmutable;
    private int preparationTicks = 20*15;
    private int commonTicks      = 20*90;
    private int timeOut          = 20*45;
    private int winDelay         = 20*2;
    private int nextRDelay       = 20*5;
    private RespawnAllRandomly respawner;

    @Override
    public void onAddPlayer(EntityPlayerMP player) {}


    @Override
    public void onRemPlayer(MinigameWorld prev, EntityPlayerMP player) {}


    @Override
    public void onPreRemPlayer(EntityPlayerMP player) {}


    public static class Data extends WorldData {
        public int rounds;
        public boolean isTimeOut, ticked;
    }


    @Override
    public WorldData createData(MinigameWorld world) {
        return new Data();
    }


    @Override
    public void onDeath(LivingDeathEvent event) {
        Entity entity = event.getEntity();
        if(entity instanceof EntityPlayerMP){
            EntityPlayerMP player = (EntityPlayerMP) entity;
            MinigameWorld world = (MinigameWorld) entity.world;
            if(!world.isGameMember(player))return;
            WorldData data = world.data();

            event.setCanceled(true);
            player.onPlayerDeath(event.getSource(), false);

            data.addDeath(player, 1);
            //Vars.DEATHS.addInt((EntityPlayerMP) entity, 1);

            Entity cause = event.getSource().getTrueSource();
            if(cause != null && cause instanceof EntityPlayerMP){
                data.addKill(cause, 1);
                //Vars.KILLS.addInt((EntityPlayerMP) cause, 1);
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
            Data data = world.data();
            return (data.rounds += 1) < this.rounds;
        }
        return false;
    }


    private void newRound(WorldServer worldServer){
        MinigameWorld world = (MinigameWorld) worldServer;
        List<EntityPlayerMP> members = world.getMembers();
        for (EntityPlayerMP player:members){
            player.setGameType(GameType.SPECTATOR);
        }
        world.addDelayedTask(this::onCompletePreparation, nextRDelay);
    }


    @Override
    public void onTick(MinigameWorld world) {
        Data data = world.data();

        if(!data.ticked){
            data.ticked = true;
            world.addDelayedTask(this::onCompletePreparation, preparationTicks);
        }
        if(!world.finished()){
            if(world.getMembers().size() > 1){
                onComplete(world);
            }
        }
    }


    @Override
    public void onComplete(MinigameWorld world) {
        //todo
        Data data = world.data();

        WorldData.Results results = data.dumpResults(WorldData.Sorting.SCORE);
        results.get(0); //is winner

        super.onComplete(world);
    }


    private void onCompletePreparation(WorldServer worldServer){
        MinigameWorld world = (MinigameWorld) worldServer;
        List<EntityPlayerMP> members = world.getMembers();
        respawner.respawn(world, members);

        world.addDelayedTask(this::onEndCommon, commonTicks);
    }


    private void onEndCommon(WorldServer worldServer){
        MinigameWorld world = (MinigameWorld) worldServer;
        if(world.finished())return;
        List<EntityPlayerMP> members = world.getMembers();

        for (EntityPlayerMP player:members){
            player.addPotionEffect(new PotionEffect(MobEffects.GLOWING, Integer.MAX_VALUE, 1, true, false));
        }

        world.addDelayedTask(this::onTimeOut, timeOut);
    }


    private void onTimeOut(WorldServer worldServer){
        MinigameWorld world = (MinigameWorld) worldServer;
        if(world.finished())return;

        world.finish();
        Data data = world.data();
        data.isTimeOut = true;

        List<EntityPlayerMP> members = world.getMembers();

        for (EntityPlayerMP player:members){
            player.onKillCommand();
        }
    }


    @Override
    public boolean isImmutable() {
        return type == GameType.ADVENTURE || isImmutable;
    }
}