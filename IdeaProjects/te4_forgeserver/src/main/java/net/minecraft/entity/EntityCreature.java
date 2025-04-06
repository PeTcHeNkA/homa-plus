package net.minecraft.entity;

import java.util.UUID;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;


public abstract class EntityCreature extends EntityLiving {
    public static final UUID FLEEING_SPEED_MODIFIER_UUID = UUID.fromString("E199AD21-BA8A-4C53-8D13-6182D5C69D3A");
    public static final AttributeModifier FLEEING_SPEED_MODIFIER;
    private BlockPos homePosition;
    private float maximumHomeDistance;
    //private final float restoreWaterCost;

    public EntityCreature(World world) {
        super(world);
        this.homePosition = BlockPos.ORIGIN;
        this.maximumHomeDistance = -1.0F;
        //this.restoreWaterCost = PathNodeType.WATER.getPriority();
    }


    public float getBlockPathWeight(BlockPos pos) {
        return 0.0F;
    }


    @Override
    public boolean getCanSpawnHere() {
        return super.getCanSpawnHere() && this.getBlockPathWeight(new BlockPos(this.posX, this.getEntityBoundingBox().minY, this.posZ)) >= 0.0F;
    }


    public boolean hasPath() {
        return !this.navigator.noPath();
    }


    public boolean isWithinHomeDistanceCurrentPosition() {
        return this.isWithinHomeDistanceFromPosition(posX, posY, posZ);
    }


    public boolean isWithinHomeDistanceFromPosition(BlockPos pos) {
        if (this.maximumHomeDistance == -1.0F) {
            return true;
        } else {
            return this.homePosition.distanceSq(pos) < (double)(this.maximumHomeDistance * this.maximumHomeDistance);
        }
    }


    public boolean isWithinHomeDistanceFromPosition(double x, double y, double z) {
        if (this.maximumHomeDistance == -1.0F) {
            return true;
        } else {
            return this.homePosition.distanceSq(x, y, z) < (double)(this.maximumHomeDistance * this.maximumHomeDistance);
        }
    }


    public void setHomePosAndDistance(BlockPos pos, int maxDistance) {
        this.homePosition = pos;
        this.maximumHomeDistance = (float)maxDistance;
    }


    public BlockPos getHomePosition() {
        return this.homePosition;
    }


    public float getMaximumHomeDistance() {
        return this.maximumHomeDistance;
    }


    public void detachHome() {
        this.maximumHomeDistance = -1.0F;
    }


    public boolean hasHome() {
        return this.maximumHomeDistance != -1.0F;
    }


    public boolean isTeamable(){
        return false;
    }


    @Override
    protected void updateLeashedState() {
        super.updateLeashedState();
        if (this.getLeashed() && this.getLeashHolder() != null && this.getLeashHolder().world == this.world) {
            Entity lvt_1_1_ = this.getLeashHolder();
            this.setHomePosAndDistance(new BlockPos((int)lvt_1_1_.posX, (int)lvt_1_1_.posY, (int)lvt_1_1_.posZ), 5);
            float lvt_2_1_ = this.getDistance(lvt_1_1_);
            if (this instanceof EntityTameable && ((EntityTameable)this).isSitting()) {
                if (lvt_2_1_ > 10.0F) {
                    this.clearLeashed(true, true);
                }

                return;
            }

            this.onLeashDistance(lvt_2_1_);
            if (lvt_2_1_ > 10.0F) {
                this.clearLeashed(true, true);
                this.tasks.disableControlFlag(1);
            } else if (lvt_2_1_ > 6.0F) {
                double lvt_3_1_ = (lvt_1_1_.posX - this.posX) / (double)lvt_2_1_;
                double lvt_5_1_ = (lvt_1_1_.posY - this.posY) / (double)lvt_2_1_;
                double lvt_7_1_ = (lvt_1_1_.posZ - this.posZ) / (double)lvt_2_1_;
                this.motionX += lvt_3_1_ * Math.abs(lvt_3_1_) * 0.4;
                this.motionY += lvt_5_1_ * Math.abs(lvt_5_1_) * 0.4;
                this.motionZ += lvt_7_1_ * Math.abs(lvt_7_1_) * 0.4;
            } else {
                this.tasks.enableControlFlag(1);
                float lvt_3_2_ = 2.0F;
                Vec3d lvt_4_1_ = (new Vec3d(lvt_1_1_.posX - this.posX, lvt_1_1_.posY - this.posY, lvt_1_1_.posZ - this.posZ)).normalize().scale((double)Math.max(lvt_2_1_ - 2.0F, 0.0F));
                this.getNavigator().tryMoveToXYZ(this.posX + lvt_4_1_.x, this.posY + lvt_4_1_.y, this.posZ + lvt_4_1_.z, this.followLeashSpeed());
            }
        }

    }


    protected double followLeashSpeed() {
        return 1.0;
    }


    protected void onLeashDistance(float f) {}


    static {
        FLEEING_SPEED_MODIFIER = (new AttributeModifier(FLEEING_SPEED_MODIFIER_UUID, "Fleeing speed bonus", 2.0, 2)).setSaved(false);
    }
}