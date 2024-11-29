package plus.fserver.world.addon.pvpworld.tasks;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.scoreboard.Team;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import plus.fserver.utls.math.IntRef;
import plus.fserver.world.addon.ScorePlayerTeamEX;
import plus.fserver.world.addon.pvpworld.PvpWorld;
import java.util.function.Consumer;


public class RewardOnKill implements Consumer<LivingDeathEvent> {
    private final String key;
    private final int points;
    private final int pointsToReward;

    public RewardOnKill(String key, int points, int pointsToReward) {
        this.key = key;
        this.points = points;
        this.pointsToReward = pointsToReward;
    }


    @Override
    public void accept(LivingDeathEvent event) {
        Entity entity = event.getEntity();
        int points = getPointsAtTarget(entity);
        if(points > 0){
            EntityPlayerMP player = (EntityPlayerMP) entity;

            EntityLivingBase attacker = player.getCombatTracker().getBestAttacker();
            if(attacker == null)return;
            Team team = attacker.getTeam();
            if(team == null)return;

            ScorePlayerTeamEX sTeam = (ScorePlayerTeamEX) team;

            IntRef score = sTeam.getCustom(key);
            if(score == null){
                score = new IntRef(points);
                sTeam.setCustom(key, score);
            } else {
                score.add(points);
            }
            if(score.get() >= pointsToReward){
                PvpWorld pw = ((PvpWorld) player.world);
                pw.getArena().onComplete(pw);
            }
        }
    }


    public int getPointsAtTarget(Entity entity){
        if(entity instanceof EntityPlayerMP){
            return points;
        }
        return 0;
    }
}