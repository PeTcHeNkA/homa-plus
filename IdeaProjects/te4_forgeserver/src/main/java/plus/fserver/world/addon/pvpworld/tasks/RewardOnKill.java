package plus.fserver.world.addon.pvpworld.tasks;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.scoreboard.Team;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import plus.fserver.world.addon.pvpworld.MinigameWorld;

import java.util.function.Consumer;


public class RewardOnKill /*implements Consumer<LivingDeathEvent>*/ {
    private final int points;
    private final int pointsToReward;

    public RewardOnKill(int points, int pointsToReward) {
        this.points = points;
        this.pointsToReward = pointsToReward;
    }


//    @Override
//    public void accept(LivingDeathEvent event) {
//        Entity entity = event.getEntity();
//        int points = getPointsAtTarget(entity);
//        if(points > 0){
//            EntityPlayerMP player = (EntityPlayerMP) entity;
//
//            EntityLivingBase attacker = player.getCombatTracker().getBestAttacker();
//            if(attacker == null)return;
//            Team team = attacker.getTeam();
//            if(team == null)return;
//
//            int score = Vars.SCORE.addInt(team, points);
//            if(attacker instanceof EntityPlayerMP) {
//                EntityPlayerMP atk = (EntityPlayerMP) attacker;
//                Vars.SCORE.addInt(atk, score);
//                Vars.KILLS.addInt(atk, 1);
//            }
//            Vars.DEATHS.addInt(player, 1);
//
//            if(score >= pointsToReward){
//                MinigameWorld pw = ((MinigameWorld) player.world);
//                pw.controller().onComplete(pw);
//            }
//        }
//    }
//
//
//    public int getPointsAtTarget(Entity entity){
//        if(entity instanceof EntityPlayerMP){
//            return points;
//        }
//        return 0;
//    }
}