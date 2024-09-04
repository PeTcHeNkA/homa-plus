package net.minecraft.entity.ai;

import javax.annotation.Nullable;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.math.MathHelper;


public abstract class EntityAITarget extends EntityAIBase {
    protected final EntityCreature taskOwner;
    protected boolean shouldCheckSight;
    private final boolean nearbyOnly;
    private byte targetSearchStatus;
    private byte targetSearchDelay;
    private short targetUnseenTicks;
    protected EntityLivingBase target;
    protected int unseenMemoryTicks;

    public EntityAITarget(EntityCreature creature, boolean skipCheck) {
        this(creature, skipCheck, false);
    }

    public EntityAITarget(EntityCreature creature, boolean skipCheck, boolean nearOnly) {
        this.unseenMemoryTicks = 60;
        this.taskOwner = creature;
        this.shouldCheckSight = skipCheck;
        this.nearbyOnly = nearOnly;
    }


    @Override
    public boolean shouldContinueExecuting() {
        EntityLivingBase curTarget = this.taskOwner.getAttackTarget();
        if (curTarget == null) {
            curTarget = this.target;
        }

        if (curTarget == null) {
            return false;
        } else if (!curTarget.isEntityAlive()) {
            return false;
        } else {
            Team team = this.taskOwner.getTeam();
            Team targetTeam = curTarget.getTeam();
            if (team != null && targetTeam == team) {
                return false;
            } else {
                double distance = this.getTargetDistance();
                if (this.taskOwner.getDistanceSq(curTarget) > distance * distance) {
                    return false;
                } else {
                    if (this.shouldCheckSight) {
                        if (this.taskOwner.getEntitySenses().canSee(curTarget)) {
                            this.targetUnseenTicks = 0;
                        } else if (++this.targetUnseenTicks > this.unseenMemoryTicks) {
                            return false;
                        }
                    }

                    if (curTarget.isPlayer() && ((EntityPlayer)curTarget).capabilities.disableDamage) {
                        return false;
                    } else {
                        this.taskOwner.setAttackTarget(curTarget);
                        return true;
                    }
                }
            }
        }
    }


    protected double getTargetDistance() {
        IAttributeInstance inst = this.taskOwner.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE);
        return inst == null ? 16.0 : inst.getAttributeValue();
    }


    @Override
    public void startExecuting() {
        this.targetSearchStatus = 0;
        this.targetSearchDelay = 0;
        this.targetUnseenTicks = 0;
    }


    @Override
    public void resetTask() {
        this.taskOwner.setAttackTarget(null);
        this.target = null;
    }


    public static boolean isSuitableTarget(EntityLiving owner, @Nullable EntityLivingBase target, boolean flag, boolean check) {
        if (target == null) {
            return false;
        } else if (target == owner) {
            return false;
        } else if (!target.isEntityAlive()) {
            return false;
        } else if (!owner.canAttackClass(target.getClass())) {
            return false;
        } else if (owner.isOnSameTeam(target)) {
            return false;
        } else {
            if (owner instanceof IEntityOwnable && ((IEntityOwnable)owner).getOwnerId() != null) {
                if (target instanceof IEntityOwnable && ((IEntityOwnable)owner).getOwnerId().equals(((IEntityOwnable)target).getOwnerId())) {
                    return false;
                }

                if (target == ((IEntityOwnable)owner).getOwner()) {
                    return false;
                }
            } else if (target.isPlayer() && !flag && ((EntityPlayer)target).capabilities.disableDamage) {
                return false;
            }

            return !check || owner.getEntitySenses().canSee(target);
        }
    }


    protected boolean isSuitableTarget(@Nullable EntityLivingBase entity, boolean flag) {
        if (!isSuitableTarget(this.taskOwner, entity, flag, this.shouldCheckSight)) {
            return false;
        } else if (!this.taskOwner.isWithinHomeDistanceFromPosition(entity.posX, entity.posY, entity.posZ)) {
            return false;
        } else {
            if (this.nearbyOnly) {
                if (--this.targetSearchDelay <= 0) {
                    this.targetSearchStatus = 0;
                }
                if (this.targetSearchStatus == 0) {
                    this.targetSearchStatus = (byte) (this.canEasilyReach(entity) ? 1 : 2);
                }
                return this.targetSearchStatus != 2;
            }
            return true;
        }
    }


    private boolean canEasilyReach(EntityLivingBase entity) {
        this.targetSearchDelay = (byte) (10 + this.taskOwner.getRNG().nextInt(5));
        Path path = this.taskOwner.getNavigator().getPathToEntityLiving(entity);
        if (path == null) {
            return false;
        } else {
            PathPoint point = path.getFinalPathPoint();
            if (point == null) {
                return false;
            } else {
                int dx = point.x - MathHelper.floor(entity.posX);
                int dz = point.z - MathHelper.floor(entity.posZ);
                return (double)(dx * dx + dz * dz) <= 2.25;
            }
        }
    }


    public EntityAITarget setUnseenMemoryTicks(int ticks) {
        this.unseenMemoryTicks = ticks;
        return this;
    }
}
