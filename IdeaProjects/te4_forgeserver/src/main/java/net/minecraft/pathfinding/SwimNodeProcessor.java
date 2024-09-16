package net.minecraft.pathfinding;

import javax.annotation.Nullable;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import plus.fserver.api.IBlockEAccess;


public class SwimNodeProcessor extends NodeProcessor {
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
        for (int i = x; i < x + this.entitySizeX; ++i) {
            for (int j = y; j < y + this.entitySizeY; ++j)
                for (int k = z; k < z + this.entitySizeZ; ++k)
                    if (blockaccess.getBlockState(pooledPos.setPos(i, j, k))
                        .getMaterial() != Material.WATER) return PathNodeType.BLOCKED;
        }
        return PathNodeType.WATER;
    }
}