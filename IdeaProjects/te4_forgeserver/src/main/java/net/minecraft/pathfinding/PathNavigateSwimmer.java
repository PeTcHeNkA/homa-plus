package net.minecraft.pathfinding;

import net.minecraft.entity.EntityLiving;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.world.World;


public class PathNavigateSwimmer extends PathNavigate {
    public PathNavigateSwimmer(EntityLiving living, World world) {
        super(living, world);
    }


    @Override
    protected PathFinder getPathFinder() {
        return new PathFinder(new SwimNodeProcessor(world));
    }


    @Override
    protected boolean canNavigate() {
        return this.isInLiquid();
    }


    @Override
    protected Vec3d getEntityPosition() {
        return new Vec3d(
                this.entity.posX,
                this.entity.posY + (double)this.entity.height * 0.5,
                this.entity.posZ
        );
    }


    @Override
    protected void updateEntityPos() {
        lastPosX = this.entity.posX;
        lastPosY = this.entity.posY + (double)this.entity.height * 0.5;
        lastPosZ = this.entity.posZ;
    }


    @Override
    protected void pathFollow() {
        Vec3d pos = this.getEntityPosition();
        float lvt_2_1_ = this.entity.width * this.entity.width;
        if (pos.squareDistanceTo(this.currentPath.getVectorFromIndex(this.entity, this.currentPath.getCurrentPathIndex())) < (double)lvt_2_1_) {
            this.currentPath.incrementPathIndex();
        }

        for(int i = Math.min(this.currentPath.getCurrentPathIndex() + 6, this.currentPath.getCurrentPathLength() - 1); i > this.currentPath.getCurrentPathIndex(); --i) {
            Vec3d vec = this.currentPath.getVectorFromIndex(this.entity, i);
            if (!(vec.squareDistanceTo(pos) > 36.0) && this.isDirectPathBetweenPoints(pos, vec, 0, 0, 0)) {
                this.currentPath.setCurrentPathIndex(i);
                break;
            }
        }
        this.checkForStuck(pos);
    }


    @Override
    protected boolean isDirectPathBetweenPoints(Vec3d vec1, Vec3d vec2, int x, int y, int z) {
        RayTraceResult result = this.world.rayTraceBlocks(vec1, new Vec3d(vec2.x, vec2.y + (double)this.entity.height * 0.5, vec2.z), false, true, false);
        return result == null || result.typeOfHit == Type.MISS;
    }


    @Override
    public boolean canEntityStandOnPos(BlockPos pos) {
        return !this.world.access().getBlockState(pos).isFullBlock();
    }
}