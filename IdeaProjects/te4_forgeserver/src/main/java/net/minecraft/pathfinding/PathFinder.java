package net.minecraft.pathfinding;

import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;


public class PathFinder {
    private final PathHeap path = new PathHeap();
    //private final Set<PathPoint> closedSet = Sets.newHashSet();
    private final PathPoint[] pathOptions = new PathPoint[32];
    private final NodeProcessor nodeProcessor;

    public PathFinder(NodeProcessor processor) {
        this.nodeProcessor = processor;
    }


    @Nullable
    public Path findPath(IBlockAccess worldIn, EntityLiving entitylivingIn, Entity targetEntity, float maxDistance) {
        return this.findPath(worldIn, entitylivingIn, targetEntity.posX, targetEntity.getEntityBoundingBox().minY, targetEntity.posZ, maxDistance);
    }


    @Nullable
    public Path findPath(IBlockAccess worldIn, EntityLiving entitylivingIn, BlockPos targetPos, float maxDistance) {
        return this.findPath(worldIn, entitylivingIn, 0.5d+targetPos.getX(), 0.5d+targetPos.getY(), 0.5d+targetPos.getZ(), maxDistance);
    }


    @Nullable
    private Path findPath(IBlockAccess worldIn, EntityLiving entitylivingIn, double x, double y, double z, float maxDistance) {
        this.path.clearPath();
        this.nodeProcessor.init(worldIn, entitylivingIn);
        Path path = this.findPath(
                nodeProcessor.getStart(),
                nodeProcessor.getPathPointToCoords(x, y, z),
                maxDistance
        );
        this.nodeProcessor.postProcess();
        return path;
    }


    @Nullable
    private Path findPath(PathPoint from, PathPoint to, float maxDistance) {
        from.totalPathDistance = 0.0F;
        from.distanceToNext = from.distanceManhattan(to);
        from.distanceToTarget = from.distanceToNext;
        PathHeap path;
        (path = this.path).clearPath();
        path.addPoint(from);

        PathPoint pathpoint = from, cur;
        int limit = 0;

        PathPoint[] options = this.pathOptions;

        while (++limit < 200 && !path.isPathEmpty()) {
            if ((cur = path.dequeue()).equals(to)) {
                pathpoint = to;
                break;
            }

            if (cur.distanceManhattan(to) < pathpoint.distanceManhattan(to))
                pathpoint = cur;

            cur.visited = true;

            for (int k = 0, count = this.nodeProcessor.findPathOptions(options, cur, to, maxDistance); k < count; ++k) {
                PathPoint point = options[k];
                float f = cur.distanceManhattan(point);
                point.distanceFromOrigin = cur.distanceFromOrigin + f;
                point.cost = f + point.costMalus;
                float f1 = cur.totalPathDistance + point.cost;

                if (point.distanceFromOrigin < maxDistance && (!point.isAssigned() || f1 < point.totalPathDistance)) {
                    point.previous = cur;
                    point.totalPathDistance = f1;
                    point.distanceToNext = point.distanceManhattan(to) + point.costMalus;

                    if (point.isAssigned())
                        path.changeDistance(point, point.totalPathDistance + point.distanceToNext);
                    else {
                        point.distanceToTarget = point.totalPathDistance + point.distanceToNext;
                        path.addPoint(point);
                    }
                }
            }
        }

        if (pathpoint == from) return null;
        else return this.createPath(pathpoint);
    }


    //private Path createPath(PathPoint start, PathPoint end)
    private Path createPath(PathPoint end) {
        int i = 1;

        for (PathPoint pathpoint = end; pathpoint.previous != null; pathpoint = pathpoint.previous) ++i;

        PathPoint[] points = new PathPoint[i];
        PathPoint pathpoint1 = end;
        --i;

        for (points[i] = end; pathpoint1.previous != null; points[i] = pathpoint1) {
            pathpoint1 = pathpoint1.previous;
            --i;
        }
        return new Path(points);
    }
}