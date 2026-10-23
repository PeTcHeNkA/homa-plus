package net.minecraft.entity.ai;

import net.minecraft.world.*;
import net.minecraft.entity.*;
import net.minecraft.util.math.*;
import net.minecraft.entity.player.*;
import net.minecraft.pathfinding.*;
import net.minecraft.util.*;


public class EntityAIAttackMelee extends EntityAIBase {
    World world;
    protected EntityCreature attacker;
    protected int attackTick;
    double speedTowardsTarget;
    boolean longMemory;
    Path path;
    private int delayCounter;
    private double targetX;
    private double targetY;
    private double targetZ;
    protected final int attackInterval = 20;
    private int failedPathFindingPenalty;
    private boolean canPenalize;

    public EntityAIAttackMelee(final EntityCreature creature, final double speedIn, final boolean useLongMemory) {
        this.failedPathFindingPenalty = 0;
        this.canPenalize = false;
        this.attacker = creature;
        this.world = creature.world;
        this.speedTowardsTarget = speedIn;
        this.longMemory = useLongMemory;
        this.setMutexBits(3);
    }


    @Override
    public boolean shouldExecute() {
        EntityCreature attacker;
        final EntityLivingBase target = (attacker = this.attacker).getAttackTarget();
        if (target == null) {
            return false;
        }
        if (!target.isEntityAlive()) {
            return false;
        }
        if (!this.canPenalize) {
            this.path = attacker.getNavigator().getPathToEntityLiving(target);
            return this.path != null || this.getAttackReachSqr(target) >= attacker.getDistanceSq(target.posX, target.getEntityBoundingBox().minY, target.posZ);
        }
        if (--this.delayCounter <= 0) {
            this.path = attacker.getNavigator().getPathToEntityLiving(target);
            this.delayCounter = 4 + attacker.getRNG().nextInt(7);
            return this.path != null;
        }
        return true;
    }


    @Override
    public boolean shouldContinueExecuting() {
        EntityCreature attacker;
        final EntityLivingBase entitylivingbase = (attacker = this.attacker).getAttackTarget();
        if (entitylivingbase == null) {
            return false;
        }
        if (!entitylivingbase.isEntityAlive()) {
            return false;
        }
        if (!this.longMemory) {
            return !attacker.getNavigator().noPath();
        }
        return attacker.isWithinHomeDistanceFromPosition(new BlockPos(entitylivingbase)) && (!(entitylivingbase instanceof EntityPlayer) || (!((EntityPlayer)entitylivingbase).isSpectator() && !((EntityPlayer)entitylivingbase).isCreative()));
    }


    @Override
    public void startExecuting() {
        this.attacker.getNavigator().setPath(this.path, this.speedTowardsTarget);
        this.delayCounter = 0;
    }


    @Override
    public void resetTask() {
        EntityCreature attacker;
        final EntityLivingBase entitylivingbase = (attacker = this.attacker).getAttackTarget();
        if (entitylivingbase instanceof EntityPlayer && (((EntityPlayer)entitylivingbase).isSpectator() || ((EntityPlayer)entitylivingbase).isCreative())) {
            attacker.setAttackTarget(null);
        }
        attacker.getNavigator().clearPath();
    }


    @Override
    public void updateTask() {
        EntityCreature attacker;
        final EntityLivingBase entitylivingbase = (attacker = this.attacker).getAttackTarget();
        attacker.getLookHelper().setLookPositionWithEntity(entitylivingbase, 30.0f, 30.0f);
        final double d0 = attacker.getDistanceSq(entitylivingbase.posX, entitylivingbase.getEntityBoundingBox().minY, entitylivingbase.posZ);
        --this.delayCounter;
        if ((this.longMemory || attacker.getEntitySenses().canSee(entitylivingbase)) && this.delayCounter <= 0 && ((this.targetX == 0.0 && this.targetY == 0.0 && this.targetZ == 0.0) || entitylivingbase.getDistanceSq(this.targetX, this.targetY, this.targetZ) >= 1.0 || attacker.getRNG().nextFloat() < 0.05f)) {
            this.targetX = entitylivingbase.posX;
            this.targetY = entitylivingbase.getEntityBoundingBox().minY;
            this.targetZ = entitylivingbase.posZ;
            this.delayCounter = 4 + attacker.getRNG().nextInt(7);
            if (this.canPenalize) {
                this.delayCounter += this.failedPathFindingPenalty;
                if (attacker.getNavigator().getPath() != null) {
                    final PathPoint finalPathPoint = attacker.getNavigator().getPath().getFinalPathPoint();
                    if (finalPathPoint != null && entitylivingbase.getDistanceSq(finalPathPoint.x, finalPathPoint.y, finalPathPoint.z) < 1.0) {
                        this.failedPathFindingPenalty = 0;
                    }
                    else {
                        this.failedPathFindingPenalty += 10;
                    }
                }
                else {
                    this.failedPathFindingPenalty += 10;
                }
            }
            if (d0 > 1024.0) {
                this.delayCounter += 10;
            }
            else if (d0 > 256.0) {
                this.delayCounter += 5;
            }
            if (!attacker.getNavigator().tryMoveToEntityLiving(entitylivingbase, this.speedTowardsTarget)) {
                this.delayCounter += 15;
            }
        }
        this.attackTick = Math.max(this.attackTick - 1, 0);
        this.checkAndPerformAttack(entitylivingbase, d0);
    }


    protected void checkAndPerformAttack(final EntityLivingBase enemy, final double distToEnemySqr) {
        final double d0 = this.getAttackReachSqr(enemy);
        if (distToEnemySqr <= d0 && this.attackTick <= 0) {
            this.attackTick = 20;
            EntityCreature attacker;
            (attacker = this.attacker).swingArm(EnumHand.MAIN_HAND);
            attacker.attackEntityAsMob(enemy);
        }
    }


    protected double getAttackReachSqr(final EntityLivingBase attackTarget) {
        EntityCreature attacker;
        return (attacker = this.attacker).width * 2.0f * attacker.width * 2.0f + attackTarget.width;
    }
}