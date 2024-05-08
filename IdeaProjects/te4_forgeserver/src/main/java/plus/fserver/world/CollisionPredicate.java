package plus.fserver.world;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.Team;
import static plus.fserver.ElcaCV.SERVER_COMPILE;


public class CollisionPredicate implements Predicate<Entity> {
    private Team team = null;
    private Team.CollisionRule rule;

    public Predicate<Entity> forEntity(final Entity entityIn) {
        final Team team = entityIn.getTeam();
        final Team.CollisionRule rule = team == null ? Team.CollisionRule.ALWAYS : team.getCollisionRule();
        if (rule == Team.CollisionRule.NEVER) return Predicates.alwaysFalse();
        this.team = team;
        this.rule = rule;
        return this;
    }


    @Override
    public boolean apply(Entity entity) {
        if (!entity.canBePushed()) return false;
        if (entity instanceof EntityPlayer) {
            if (((EntityPlayer) entity).isSpectator()) return false;
        }

        if ((!entity.world.isRemote || SERVER_COMPILE) || entity instanceof EntityPlayer && ((EntityPlayer) entity).isUser()) {
            Team team1 = entity.getTeam();
            Team.CollisionRule rule2 = team1 == null ? Team.CollisionRule.ALWAYS : team1.getCollisionRule();

            if (rule2 == Team.CollisionRule.NEVER) {
                return false;
            } else {
                boolean flag = team != null && team.isSameTeam(team1);

                if ((rule == Team.CollisionRule.HIDE_FOR_OWN_TEAM || rule2 == Team.CollisionRule.HIDE_FOR_OWN_TEAM) && flag) {
                    return false;
                } else {
                    return rule != Team.CollisionRule.HIDE_FOR_OTHER_TEAMS && rule2 != Team.CollisionRule.HIDE_FOR_OTHER_TEAMS || flag;
                }
            }
        }
        return false;
    }
}
