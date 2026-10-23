package net.minecraft.pathfinding;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;


public class PathNavigateClimber extends PathNavigateGround {
    private BlockPos targetPosition;

    public PathNavigateClimber(EntityLiving living, World world) {
        super(living, world);
    }


    @Override
    public Path getPathToPos(BlockPos pos) {
        this.targetPosition = pos;
        return super.getPathToPos(pos);
    }


    @Override
    public Path getPathToEntityLiving(Entity entity) {
        this.targetPosition = new BlockPos(entity);
        return super.getPathToEntityLiving(entity);
    }


    @Override
    public boolean tryMoveToEntityLiving(Entity entity, double speed) {
        Path path = this.getPathToEntityLiving(entity);
        if (path != null) {
            return this.setPath(path, speed);
        } else {
            BlockPos tp = this.targetPosition;
            if(tp != null){
                BlockPos.unsafeEdit(tp,
                        MathHelper.floor(entity.posX),
                        MathHelper.floor(entity.posY),
                        MathHelper.floor(entity.posZ)
                );
            } else {
                this.targetPosition = new BlockPos(entity);
            }
            this.speed = speed;
            return true;
        }
    }


    @Override
    public void onUpdateNavigation() {
        if (!this.noPath()) {
            super.onUpdateNavigation();
        } else {
            BlockPos targetPosition;
            if ((targetPosition = this.targetPosition) != null) {
                EntityLiving entity = this.entity;

                double widthSq = entity.width * entity.width;
                if (!(entity.getDistanceSqToCenter(targetPosition) < widthSq) && (!(entity.posY > (double)targetPosition.getY()) ||
                        !(entity.getDistanceSqToCenter(new BlockPos(targetPosition.getX(), MathHelper.floor(entity.posY), targetPosition.getZ())) < widthSq))) {

                    entity.getMoveHelper().setMoveTo(targetPosition.getX(), targetPosition.getY(), targetPosition.getZ(), this.speed);
                } else {
                    this.targetPosition = null;
                }
            }
        }
    }
}