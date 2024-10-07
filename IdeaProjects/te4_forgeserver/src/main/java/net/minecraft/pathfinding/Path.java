package net.minecraft.pathfinding;

import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


public class Path {
    public static final Path WAITING = new Path(new PathPoint[0]);

    private final PathPoint[] points;
    private PathPoint[] openSet = new PathPoint[0];
    private PathPoint[] closedSet = new PathPoint[0];
    @SideOnly(Side.CLIENT)
    private PathPoint target;
    private int currentPathIndex;
    private int pathLength;

    public Path(PathPoint[] points) {
        this.points = points;
        this.pathLength = points.length;
    }

    public void incrementPathIndex() {
        ++this.currentPathIndex;
    }


    public boolean isFinished() {
        return this.currentPathIndex >= this.pathLength;
    }


    @Nullable
    public PathPoint getFinalPathPoint() {
        return this.pathLength > 0 ? this.points[this.pathLength - 1] : null;
    }


    public PathPoint getPathPointFromIndex(int i) {
        return this.points[i];
    }


    public void setPoint(int i, PathPoint point) {
        this.points[i] = point;
    }


    public int getCurrentPathLength() {
        return this.pathLength;
    }


    public void setCurrentPathLength(int length) {
        this.pathLength = length;
    }


    public int getCurrentPathIndex() {
        return this.currentPathIndex;
    }


    public void setCurrentPathIndex(int index) {
        this.currentPathIndex = index;
    }


    public Vec3d getVectorFromIndex(Entity entity, int i) {
        final PathPoint point = this.points[i];
        return new Vec3d(
                (double)point.x + (double)((int)(entity.width + 1.0F)) * 0.5,
                point.y,
                (double)point.z + (double)((int)(entity.width + 1.0F)) * 0.5
        );
    }


    public Vec3d getPosition(Entity entity) {
        return this.getVectorFromIndex(entity, this.currentPathIndex);
    }


    public Vec3d getCurrentPos() {
        PathPoint point = this.points[this.currentPathIndex];
        return new Vec3d(point.x, point.y, point.z);
    }


    public PathPoint getCurrentPoint(){
        return this.points[this.currentPathIndex];
    }


    public boolean isSamePath(Path path) {
        if (path == null) return false;
        PathPoint[] pathPoints, selfPoints;
        if ((pathPoints = path.points).length != (selfPoints = this.points).length) {
            return false;
        } else {
            for(int i = 0, s = pathPoints.length; i < s; ++i) {
                PathPoint point = pathPoints[i], sPoint = selfPoints[i];
                if (point.x != sPoint.x || point.y != sPoint.y || point.z != sPoint.z) return false;
            }
            return true;
        }
    }


    @SideOnly(Side.CLIENT)
    public PathPoint[] getOpenSet() {
        return this.openSet;
    }


    @SideOnly(Side.CLIENT)
    public PathPoint[] getClosedSet() {
        return this.closedSet;
    }


    @SideOnly(Side.CLIENT)
    public PathPoint getTarget() {
        return this.target;
    }


    @SideOnly(Side.CLIENT)
    public static Path read(PacketBuffer buf) {
        int index = buf.readInt();
        PathPoint point = PathPoint.createFromBuffer(buf);
        PathPoint[] pathPoints = new PathPoint[buf.readInt()];

        for(int i = 0; i < pathPoints.length; ++i) {
            pathPoints[i] = PathPoint.createFromBuffer(buf);
        }

        PathPoint[] pathPoints1 = new PathPoint[buf.readInt()];

        for(int i = 0; i < pathPoints1.length; ++i) {
            pathPoints1[i] = PathPoint.createFromBuffer(buf);
        }

        PathPoint[] pathPoints2 = new PathPoint[buf.readInt()];

        for(int i = 0; i < pathPoints2.length; ++i) {
            pathPoints2[i] = PathPoint.createFromBuffer(buf);
        }

        Path path = new Path(pathPoints);
        path.openSet = pathPoints1;
        path.closedSet = pathPoints2;
        path.target = point;
        path.currentPathIndex = index;
        return path;
    }
}