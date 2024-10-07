package net.minecraft.pathfinding;

import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import plus.fserver.entity.NavChunkLSECache;
import java.util.function.Consumer;


public class PathFinder {
    private PathHeap path;
    private PathPoint[] pathOptions;
    private final NodeProcessor nodeProcessor;
    private volatile boolean calculating = false;

    public PathFinder(NodeProcessor processor) {
        this.nodeProcessor = processor;
    }


    @Nullable
    public Path findPath(NavChunkLSECache cache, EntityLiving entity, Entity target, float maxDistance) {
        return this.findPath(cache, entity, target.posX, target.getEntityBoundingBox().minY, target.posZ, maxDistance);
    }


    public void findPath(EntityLiving entity, Entity target, float maxDistance, Consumer<Path> callBack) {
        if(calculating)return;
        ((WorldServer)entity.world).addPathGenTask(new PathGenTask(
                entity, target.posX, target.getEntityBoundingBox().minY, target.posZ, maxDistance, callBack
        ));
    }


    @Nullable
    public Path findPath(NavChunkLSECache cache, EntityLiving entitylivingIn, BlockPos targetPos, float maxDistance) {
        return this.findPath(cache, entitylivingIn, 0.5d+targetPos.getX(), 0.5d+targetPos.getY(), 0.5d+targetPos.getZ(), maxDistance);
    }


    public void findPath(EntityLiving entity, BlockPos target, float maxDistance, Consumer<Path> callBack) {
        if(calculating)return;
        ((WorldServer)entity.world).addPathGenTask(new PathGenTask(
                entity,0.5d+target.getX(), 0.5d+target.getY(), 0.5d+target.getZ(), maxDistance, callBack
        ));
    }


    @Nullable
    private Path findPath(NavChunkLSECache access, EntityLiving entity, double x, double y, double z, float maxDistance) {
        if(calculating)return null;
        Path path = null;
        try {
            calculating = true;
            NodeProcessor proc;
            access.init(entity);

            (proc = nodeProcessor).init(entity);
            path = this.findPath(
                    access,
                    proc.getStart(access),
                    proc.getPathPointToCoords(access, x, y, z),
                    maxDistance
            );
            proc.postProcess();
        } finally {
            calculating = false;
        }
        return path;
    }


    public class PathGenTask{
        final EntityLiving entitylivingIn;
        final double x, y, z;
        final float maxDistance;
        final Consumer<Path> callBack;

        public PathGenTask(EntityLiving entitylivingIn, double x, double y, double z, float maxDistance, Consumer<Path> callBack) {
            this.entitylivingIn = entitylivingIn;
            this.x = x;
            this.y = y;
            this.z = z;
            this.maxDistance = maxDistance;
            this.callBack = callBack;
        }


        public void complete(NavChunkLSECache access){
            Path path = findPath(access, entitylivingIn, x, y, z, maxDistance);
            callBack.accept(path);
        }
    }


    @Nullable
    private Path findPath(NavChunkLSECache access, PathPoint from, PathPoint to, float maxDistance) {
        from.totalPathDistance = 0.0F;
        from.distanceToNext = from.distanceManhattan(to);
        from.distanceToTarget = from.distanceToNext;
        PathHeap path;
        if((path = this.path) != null)path.clearPath();
        else path = this.path = new PathHeap();
        path.addPointV(from);

        PathPoint pathpoint = from, cur;
        int limit = 0;

        PathPoint[] options;
        if((options = this.pathOptions) == null)
            options = this.pathOptions = new PathPoint[32];

        final NodeProcessor proc = this.nodeProcessor;

        while (++limit < 200 && !path.isPathEmpty()) {
            if ((cur = path.dequeue()).equals(to)) {
                pathpoint = to;
                break;
            }

            if (cur.distanceManhattanInt(to) < pathpoint.distanceManhattanInt(to))
                pathpoint = cur;

            cur.visited = true;

            for (int k = 0, count = proc.findPathOptions(access, options, cur, to, maxDistance); k < count; ++k) {
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
                        path.addPointV(point);
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