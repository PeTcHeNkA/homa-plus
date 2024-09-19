package net.minecraft.pathfinding;

import javax.annotation.Nullable;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import plus.fserver.api.IBlockEAccess;


public class SwimNodeProcessor extends NodeProcessor {
    public SwimNodeProcessor(){}
    public SwimNodeProcessor(World world){
        super(world);
    }

    @Override
    public PathPoint getStart() {
        return this.openPoint(MathHelper.floor(this.entity.getEntityBoundingBox().minX), MathHelper.floor(this.entity.getEntityBoundingBox().minY + 0.5D), MathHelper.floor(this.entity.getEntityBoundingBox().minZ));
    }


    @Override
    public PathPoint getPathPointToCoords(double x, double y, double z) {
        return this.openPoint(MathHelper.floor(x - (double)(this.entity.width / 2.0F)), MathHelper.floor(y + 0.5D), MathHelper.floor(z - (double)(this.entity.width / 2.0F)));
    }


    @Override
    public int findPathOptions(PathPoint[] pathOptions, PathPoint currentPoint, PathPoint targetPoint, float maxDistance) {
        int i = 0;
        PathPoint point;
        float maxDistanceSq = maxDistance * maxDistance;
        for (EnumFacing enumfacing : EnumFacing.VALUES)
            if ((point = this.getWaterNode(
                    currentPoint.x + enumfacing.getXOffset(), currentPoint.y + enumfacing.getYOffset(), currentPoint.z + enumfacing.getZOffset()
            )) != null && !point.visited && point.distanceToSquaredInt(targetPoint) < maxDistanceSq) {

                pathOptions[i++] = point;
            }
        return i;
    }


    @Override
    public PathNodeType getPathNodeType(IBlockEAccess access, int x, int y, int z, EntityLiving entity, int xSize, int ySize, int zSize, boolean canBreakDoorsIn, boolean canEnterDoorsIn) {
        return PathNodeType.WATER;
    }


    @Override
    public PathNodeType getPathNodeType(IBlockEAccess access, int x, int y, int z) {
        return PathNodeType.WATER;
    }


    @Nullable
    private PathPoint getWaterNode(int x, int y, int z) {
        PathNodeType pathnodetype = this.isFree(x, y, z);
        return pathnodetype == PathNodeType.WATER ? this.openPoint(x, y, z) : null;
    }


    private PathNodeType isFree(int x, int y, int z) {
        final int mx = (x + this.entitySizeX) & 255;
        final int my = (y + this.entitySizeY) & 255;
        final int mz = (z + this.entitySizeZ) & 255;
        IBlockEAccess access = blockaccess;
        for (int cx = x; cx < mx; ++cx) {
            for (int cy = y; cy < my; ++cy)
                for (int cz = z; cz < mz; ++cz)
                    if (access.getBlockState(cx, cy, cz)
                        .getMaterial() != Material.WATER) return PathNodeType.BLOCKED;
        }
        return PathNodeType.WATER;
    }
}