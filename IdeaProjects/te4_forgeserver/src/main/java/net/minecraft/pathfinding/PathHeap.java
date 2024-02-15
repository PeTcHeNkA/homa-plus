package net.minecraft.pathfinding;


public class PathHeap {
    private PathPoint[] pathPoints = new PathPoint[128];
    private int count;

    public PathPoint addPoint(PathPoint point) {
        if (point.index >= 0) {
            throw new IllegalStateException("OW KNOWS!");
        } else {
            if (this.count == this.pathPoints.length) {
                PathPoint[] apathpoint = new PathPoint[this.count << 1];
                System.arraycopy(this.pathPoints, 0, apathpoint, 0, this.count);
                this.pathPoints = apathpoint;
            }

            this.pathPoints[this.count] = point;
            point.index = this.count;
            this.sortBack(this.count++);
            return point;
        }
    }


    public void clearPath() {
        this.count = 0;
    }


    public PathPoint dequeue() {
        PathPoint pathpoint = this.pathPoints[0];
        this.pathPoints[0] = this.pathPoints[--this.count];
        this.pathPoints[this.count] = null;

        if (this.count > 0) {
            this.sortForward(0);
        }
        pathpoint.index = -1;
        return pathpoint;
    }


    public void changeDistance(PathPoint point, float distance) {
        float f = point.distanceToTarget;
        point.distanceToTarget = distance;

        if (distance < f) {
            this.sortBack(point.index);
        } else {
            this.sortForward(point.index);
        }
    }


    private void sortBack(int index) {
        PathPoint pathpoint = this.pathPoints[index], current;
        int i;
        for (float f = pathpoint.distanceToTarget; index > 0; index = i) {
            if (f >= (current = this.pathPoints[i = index - 1 >> 1]).distanceToTarget) break;
            this.pathPoints[index] = current;
            current.index = index;
        }
        this.pathPoints[index] = pathpoint;
        pathpoint.index = index;
    }


    private void sortForward(int index) {
        PathPoint point1 = this.pathPoints[index], point2, point3;
        int num1, num2;
        float f = point1.distanceToTarget, f1, f2;
        while (true) {
            if ((num1 = 1 + (index << 1)) >= this.count) break;
            f1 = (point2 = this.pathPoints[num1]).distanceToTarget;

            if ((num2 = num1 + 1) >= this.count) {
                point3 = null;
                f2 = Float.POSITIVE_INFINITY;
            } else {
                point3 = this.pathPoints[num2];
                f2 = point3.distanceToTarget;
            }

            if (f1 < f2) {
                if (f1 >= f) break;

                this.pathPoints[index] = point2;
                point2.index = index;
                index = num1;
            } else {
                if (f2 >= f) break;

                this.pathPoints[index] = point3;
                point3.index = index;
                index = num2;
            }
        }
        this.pathPoints[index] = point1;
        point1.index = index;
    }


    public boolean isPathEmpty() {
        return this.count == 0;
    }
}