package net.minecraft.pathfinding;


public class PathHeap {
    private PathPoint[] pathPoints = new PathPoint[64];
    private int count;

    public PathPoint addPoint(PathPoint point) {
        addPointV(point);
        return point;
    }


    public final void addPointV(PathPoint point) {
        if (point.index >= 0) {
            throw new IllegalStateException("OW KNOWS!");
        } else {
            PathPoint[] pathPoints = this.pathPoints;
            if (this.count == pathPoints.length) {
                PathPoint[] newPoints = new PathPoint[this.count << 1];
                System.arraycopy(pathPoints, 0, newPoints, 0, this.count);
                this.pathPoints = pathPoints = newPoints;
            }

            pathPoints[point.index = this.count] = point;
            sortBack(pathPoints, this.count++);
        }
    }


    public void clearPath() {
        this.count = 0;
    }


    public PathPoint dequeue() {
        PathPoint[] pathPoints;
        PathPoint pathpoint = (pathPoints = this.pathPoints)[0];
        pathPoints[0] = pathPoints[--this.count];
        pathPoints[this.count] = null;

        if (this.count > 0) {
            sortForward(pathPoints, 0, this.count);
        }
        pathpoint.index = -1;
        return pathpoint;
    }


    public void changeDistance(PathPoint point, float distance) {
        float f = point.distanceToTarget;
        point.distanceToTarget = distance;

        if (distance < f) {
            sortBack(this.pathPoints, point.index);
        } else {
            sortForward(this.pathPoints, point.index, count);
        }
    }


    private static void sortBack(final PathPoint[] pathPoints, int index) {
        PathPoint pathpoint = pathPoints[index], current;
        int i;
        for (float f = pathpoint.distanceToTarget; index > 0; index = i) {
            if (f >= (current = pathPoints[i = index - 1 >> 1]).distanceToTarget) break;
            pathPoints[index] = current;
            current.index = index;
        }
        pathPoints[index] = pathpoint;
        pathpoint.index = index;
    }


    private static void sortForward(final PathPoint[] pathPoints, int index, int count) {
        PathPoint point1 = pathPoints[index], point2, point3;
        int num1, num2;
        float f = point1.distanceToTarget, f1, f2;
        while (true) {
            if ((num1 = 1 + (index << 1)) >= count) break;
            f1 = (point2 = pathPoints[num1]).distanceToTarget;

            if ((num2 = num1 + 1) >= count) {
                point3 = null;
                f2 = Float.POSITIVE_INFINITY;
            } else {
                point3 = pathPoints[num2];
                f2 = point3.distanceToTarget;
            }

            if (f1 < f2) {
                if (f1 >= f) break;

                pathPoints[index] = point2;
                point2.index = index;
                index = num1;
            } else {
                if (f2 >= f) break;

                pathPoints[index] = point3;
                point3.index = index;
                index = num2;
            }
        }
        pathPoints[index] = point1;
        point1.index = index;
    }


    public boolean isPathEmpty() {
        return this.count == 0;
    }
}