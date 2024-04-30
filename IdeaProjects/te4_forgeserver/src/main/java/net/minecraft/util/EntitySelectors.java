package net.minecraft.util;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.Team;


public final class EntitySelectors {
    public static final Predicate<Entity> IS_ALIVE = Entity::isEntityAlive;
    public static final Predicate<Entity> IS_STANDALONE = entity -> entity.isEntityAlive() && !entity.isBeingRidden() && !entity.isRiding();
    public static final Predicate<Entity> HAS_INVENTORY = entity -> entity instanceof IInventory && entity.isEntityAlive();
    public static final Predicate<Entity> CAN_AI_TARGET = entity -> !(entity instanceof EntityPlayer) || !((EntityPlayer)entity).isSpectator()
            && !((EntityPlayer)entity).isCreative();

    public static final Predicate<Entity> NOT_SPECTATING = entity -> !(entity instanceof EntityPlayer) || !((EntityPlayer)entity).isSpectator();


    public static <T extends Entity> Predicate<T> withinRange(final double x, final double y, final double z, double range) {
        final double d0 = range * range;
        return entity -> entity != null && entity.getDistanceSq(x, y, z) <= d0;
    }


    @Deprecated
    public static <T extends Entity> Predicate<T> getTeamCollisionPredicate(final Entity entityIn) {
        final Team team = entityIn.getTeam();
        final Team.CollisionRule team$collisionrule = team == null ? Team.CollisionRule.ALWAYS : team.getCollisionRule();
        Predicate<?> ret = team$collisionrule == Team.CollisionRule.NEVER ? Predicates.alwaysFalse() :
                Predicates.and(NOT_SPECTATING, entity -> {
            if (!entity.canBePushed()) {
                return false;
            }
            else if (!entityIn.world.isRemote || entity instanceof EntityPlayer && ((EntityPlayer)entity).isUser()) {
                Team team1 = entity.getTeam();
                Team.CollisionRule team$collisionrule1 = team1 == null ? Team.CollisionRule.ALWAYS : team1.getCollisionRule();

                if (team$collisionrule1 == Team.CollisionRule.NEVER) {
                    return false;
                } else {
                    boolean flag = team != null && team.isSameTeam(team1);

                    if ((team$collisionrule == Team.CollisionRule.HIDE_FOR_OWN_TEAM || team$collisionrule1 == Team.CollisionRule.HIDE_FOR_OWN_TEAM) && flag) {
                        return false;
                    } else {
                        return team$collisionrule != Team.CollisionRule.HIDE_FOR_OTHER_TEAMS && team$collisionrule1 != Team.CollisionRule.HIDE_FOR_OTHER_TEAMS || flag;
                    }
                }
            } else {
                return false;
            }
        });
        return (Predicate<T>)ret;
    }


    public static Predicate<Entity> notRiding(final Entity src) {
        return new Predicate<Entity>() {
            public boolean apply(@Nullable Entity entity) {
                while (true) {
                    if (entity.isRiding()) {
                        entity = entity.getRidingEntity();
                        if (entity != src) {
                            continue;
                        }
                        return false;
                    }
                    return true;
                }
            }
        };
    }


    public static class ArmoredMob implements Predicate<Entity> {
        private final ItemStack armor;

        public ArmoredMob(ItemStack armor) {
            this.armor = armor;
        }

        public boolean apply(@Nullable Entity input) {
            if (!input.isEntityAlive()) {
                return false;
            }
            else if (!(input instanceof EntityLivingBase)) {
                return false;
            } else {
                EntityLivingBase entity = (EntityLivingBase)input;

                if (!entity.getItemStackFromSlot(EntityLiving.getSlotForItemStack(this.armor)).isEmpty()) {
                    return false;
                } else if (entity instanceof EntityLiving) {
                    return ((EntityLiving)entity).canPickUpLoot();
                } else if (entity instanceof EntityArmorStand) {
                    return true;
                } else {
                    return entity instanceof EntityPlayer;
                }
            }
        }
    }
}