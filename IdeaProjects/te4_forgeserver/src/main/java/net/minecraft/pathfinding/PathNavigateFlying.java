package net.minecraft.pathfinding;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;


public class PathNavigateFlying extends PathNavigate {
    public PathNavigateFlying(EntityLiving living, World world) {
        super(living, world);
    }

    @Override
    protected PathFinder getPathFinder() {
        this.nodeProcessor = new FlyingNodeProcessor();
        this.nodeProcessor.setCanEnterDoors(true);
        return new PathFinder(this.nodeProcessor);
    }


    @Override
    protected boolean canNavigate() {
        return this.canFloat() && this.isInLiquid() || !this.entity.isRiding();
    }


    @Override
    protected Vec3d getEntityPosition() {
        return new Vec3d(this.entity.posX, this.entity.posY, this.entity.posZ);
    }


    @Override
    protected void updateEntityPos() {
        lastPosX = this.entity.posX;
        lastPosY = this.entity.posY;
        lastPosZ = this.entity.posZ;
    }


    public Path getPathToEntityLiving(Entity p_75494_1_) {
        return this.getPathToPos(new BlockPos(p_75494_1_));
    }


    public void onUpdateNavigation() {
        ++this.totalTicks;
        if (this.tryUpdatePath) {
            this.updatePath();
        }

        if (!this.noPath()) {
            Vec3d lvt_1_2_;
            if (this.canNavigate()) {
                this.pathFollow();
            } else if (this.currentPath != null && this.currentPath.getCurrentPathIndex() < this.currentPath.getCurrentPathLength()) {
                lvt_1_2_ = this.currentPath.getVectorFromIndex(this.entity, this.currentPath.getCurrentPathIndex());
                if (MathHelper.floor(this.entity.posX) == MathHelper.floor(lvt_1_2_.x) && MathHelper.floor(this.entity.posY) == MathHelper.floor(lvt_1_2_.y) && MathHelper.floor(this.entity.posZ) == MathHelper.floor(lvt_1_2_.z)) {
                    this.currentPath.setCurrentPathIndex(this.currentPath.getCurrentPathIndex() + 1);
                }
            }

            this.debugPathFinding();
            if (!this.noPath()) {
                lvt_1_2_ = this.currentPath.getPosition(this.entity);
                this.entity.getMoveHelper().setMoveTo(lvt_1_2_.x, lvt_1_2_.y, lvt_1_2_.z, this.speed);
            }
        }
    }


    protected boolean isDirectPathBetweenPoints(Vec3d p_75493_1_, Vec3d p_75493_2_, int p_75493_3_, int p_75493_4_, int p_75493_5_) {
        int lvt_6_1_ = MathHelper.floor(p_75493_1_.x);
        int lvt_7_1_ = MathHelper.floor(p_75493_1_.y);
        int lvt_8_1_ = MathHelper.floor(p_75493_1_.z);
        double lvt_9_1_ = p_75493_2_.x - p_75493_1_.x;
        double lvt_11_1_ = p_75493_2_.y - p_75493_1_.y;
        double lvt_13_1_ = p_75493_2_.z - p_75493_1_.z;
        double lvt_15_1_ = lvt_9_1_ * lvt_9_1_ + lvt_11_1_ * lvt_11_1_ + lvt_13_1_ * lvt_13_1_;
        if (lvt_15_1_ < 1.0E-8) {
            return false;
        } else {
            double lvt_17_1_ = 1.0 / Math.sqrt(lvt_15_1_);
            lvt_9_1_ *= lvt_17_1_;
            lvt_11_1_ *= lvt_17_1_;
            lvt_13_1_ *= lvt_17_1_;
            double lvt_19_1_ = 1.0 / Math.abs(lvt_9_1_);
            double lvt_21_1_ = 1.0 / Math.abs(lvt_11_1_);
            double lvt_23_1_ = 1.0 / Math.abs(lvt_13_1_);
            double lvt_25_1_ = (double)lvt_6_1_ - p_75493_1_.x;
            double lvt_27_1_ = (double)lvt_7_1_ - p_75493_1_.y;
            double lvt_29_1_ = (double)lvt_8_1_ - p_75493_1_.z;
            if (lvt_9_1_ >= 0.0) {
                ++lvt_25_1_;
            }

            if (lvt_11_1_ >= 0.0) {
                ++lvt_27_1_;
            }

            if (lvt_13_1_ >= 0.0) {
                ++lvt_29_1_;
            }

            lvt_25_1_ /= lvt_9_1_;
            lvt_27_1_ /= lvt_11_1_;
            lvt_29_1_ /= lvt_13_1_;
            int lvt_31_1_ = lvt_9_1_ < 0.0 ? -1 : 1;
            int lvt_32_1_ = lvt_11_1_ < 0.0 ? -1 : 1;
            int lvt_33_1_ = lvt_13_1_ < 0.0 ? -1 : 1;
            int lvt_34_1_ = MathHelper.floor(p_75493_2_.x);
            int lvt_35_1_ = MathHelper.floor(p_75493_2_.y);
            int lvt_36_1_ = MathHelper.floor(p_75493_2_.z);
            int lvt_37_1_ = lvt_34_1_ - lvt_6_1_;
            int lvt_38_1_ = lvt_35_1_ - lvt_7_1_;
            int lvt_39_1_ = lvt_36_1_ - lvt_8_1_;

            while(true) {
                while(lvt_37_1_ * lvt_31_1_ > 0 || lvt_38_1_ * lvt_32_1_ > 0 || lvt_39_1_ * lvt_33_1_ > 0) {
                    if (lvt_25_1_ < lvt_29_1_ && lvt_25_1_ <= lvt_27_1_) {
                        lvt_25_1_ += lvt_19_1_;
                        lvt_6_1_ += lvt_31_1_;
                        lvt_37_1_ = lvt_34_1_ - lvt_6_1_;
                    } else if (lvt_27_1_ < lvt_25_1_ && lvt_27_1_ <= lvt_29_1_) {
                        lvt_27_1_ += lvt_21_1_;
                        lvt_7_1_ += lvt_32_1_;
                        lvt_38_1_ = lvt_35_1_ - lvt_7_1_;
                    } else {
                        lvt_29_1_ += lvt_23_1_;
                        lvt_8_1_ += lvt_33_1_;
                        lvt_39_1_ = lvt_36_1_ - lvt_8_1_;
                    }
                }

                return true;
            }
        }
    }


    public void setCanOpenDoors(boolean p_192879_1_) {
        this.nodeProcessor.setCanOpenDoors(p_192879_1_);
    }


    public void setCanEnterDoors(boolean p_192878_1_) {
        this.nodeProcessor.setCanEnterDoors(p_192878_1_);
    }


    public void setCanFloat(boolean p_192877_1_) {
        this.nodeProcessor.setCanSwim(p_192877_1_);
    }


    public boolean canFloat() {
        return this.nodeProcessor.getCanSwim();
    }


    public boolean canEntityStandOnPos(BlockPos pos) {
        return entity.world.getBlockState(pos).isTopSolid();
    }
}