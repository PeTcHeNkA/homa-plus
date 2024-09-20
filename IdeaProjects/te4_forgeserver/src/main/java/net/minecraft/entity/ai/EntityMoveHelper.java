package net.minecraft.entity.ai;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.pathfinding.NodeProcessor;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.math.MathHelper;


public class EntityMoveHelper {
    protected final EntityLiving entity;
    protected double posX;
    protected double posY;
    protected double posZ;
    protected double speed;
    protected float moveForward;
    protected float moveStrafe;
    public Action action;

    public EntityMoveHelper(EntityLiving living) {
        this.action = EntityMoveHelper.Action.WAIT;
        this.entity = living;
    }

    public boolean isUpdating() {
        return this.action == EntityMoveHelper.Action.MOVE_TO;
    }

    public double getSpeed() {
        return this.speed;
    }


    public void setMoveTo(double x, double y, double z, double speed) {
        this.posX = x;
        this.posY = y;
        this.posZ = z;
        this.speed = speed;
        this.action = EntityMoveHelper.Action.MOVE_TO;
    }


    public void strafe(float forward, float strafe) {
        this.action = EntityMoveHelper.Action.STRAFE;
        this.moveForward = forward;
        this.moveStrafe = strafe;
        this.speed = 0.25;
    }


    public void read(EntityMoveHelper helper) {
        this.action = helper.action;
        this.posX = helper.posX;
        this.posY = helper.posY;
        this.posZ = helper.posZ;
        this.speed = Math.max(helper.speed, 1.0);
        this.moveForward = helper.moveForward;
        this.moveStrafe = helper.moveStrafe;
    }


    public void onUpdateMoveHelper() {
        float deg0;
        EntityLiving entity = this.entity;
        if (this.action == EntityMoveHelper.Action.STRAFE) {
            float moveSpeed = (float)entity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue();
            float curSpeed = (float)this.speed * moveSpeed;
            float forward = this.moveForward;
            float strafe = this.moveStrafe;

            float dis = MathHelper.sqrt(forward * forward + strafe * strafe);
            if (dis < 1.0F) {
                dis = 1.0F;
            }

            dis = curSpeed / dis;
            forward *= dis;
            strafe *= dis;
            float lvt_6_1_ = MathHelper.sin(entity.rotationYaw * 0.017453292F);
            float lvt_7_1_ = MathHelper.cos(entity.rotationYaw * 0.017453292F);
            float lvt_8_1_ = forward * lvt_7_1_ - strafe * lvt_6_1_;
            deg0 = strafe * lvt_7_1_ + forward * lvt_6_1_;
            PathNavigate navigator = entity.getNavigator();
            if (navigator != null) {
                NodeProcessor processor = navigator.getNodeProcessor();
                if (processor != null && processor.getPathNodeType(entity.world.access(), MathHelper.floor(entity.posX + (double)lvt_8_1_), MathHelper.floor(entity.posY), MathHelper.floor(entity.posZ + (double)deg0)) != PathNodeType.WALKABLE) {
                    this.moveForward = 1.0F;
                    this.moveStrafe = 0.0F;
                    curSpeed = moveSpeed;
                }
            }

            entity.setAIMoveSpeed(curSpeed);
            entity.setMoveForward(this.moveForward);
            entity.setMoveStrafing(this.moveStrafe);
            this.action = EntityMoveHelper.Action.WAIT;
        } else if (this.action == EntityMoveHelper.Action.MOVE_TO) {
            this.action = EntityMoveHelper.Action.WAIT;
            double dx = this.posX - entity.posX;
            double dz = this.posZ - entity.posZ;
            double dy = this.posY - entity.posY;
            double dxyzSq = dx * dx + dy * dy + dz * dz;

            if (dxyzSq < 2.500000277905201E-7) {
                entity.setMoveForward(0.0F);
                return;
            }

            deg0 = (float)(MathHelper.atan2(dz, dx) * 57.2957763671875) - 90.0F;
            entity.rotationYaw = this.limitAngle(entity.rotationYaw, deg0, 90.0F);
            entity.setAIMoveSpeed((float)(this.speed * entity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue()));
            if (dy > (double)entity.stepHeight && dx * dx + dz * dz < (double)Math.max(1.0F, entity.width)) {
                entity.getJumpHelper().setJumping();
                this.action = EntityMoveHelper.Action.JUMPING;
            }
        } else if (this.action == EntityMoveHelper.Action.JUMPING) {
            entity.setAIMoveSpeed((float)(this.speed * entity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue()));
            if (entity.onGround) {
                this.action = EntityMoveHelper.Action.WAIT;
            }
        } else {
            entity.setMoveForward(0.0F);
        }
    }


    protected float limitAngle(float yaw, float p_75639_2_, float deg) {
        float lvt_4_1_ = MathHelper.wrapDegrees(p_75639_2_ - yaw);
        if (lvt_4_1_ > deg) {
            lvt_4_1_ = deg;
        }

        if (lvt_4_1_ < -deg) {
            lvt_4_1_ = -deg;
        }

        float lvt_5_1_ = yaw + lvt_4_1_;
        if (lvt_5_1_ < 0.0F) {
            lvt_5_1_ += 360.0F;
        } else if (lvt_5_1_ > 360.0F) {
            lvt_5_1_ -= 360.0F;
        }
        return lvt_5_1_;
    }


    public double getX() {
        return this.posX;
    }


    public double getY() {
        return this.posY;
    }


    public double getZ() {
        return this.posZ;
    }


    public enum Action {
        WAIT,
        MOVE_TO,
        STRAFE,
        JUMPING;
    }
}