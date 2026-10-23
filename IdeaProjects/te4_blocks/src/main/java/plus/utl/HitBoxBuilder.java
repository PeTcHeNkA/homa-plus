package plus.utl;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;


public class HitBoxBuilder {

    protected double minX;
    protected double maxX;

    protected double minY;
    protected double maxY;

    protected double minZ;
    protected double maxZ;


    public HitBoxBuilder(double x1, double y1, double z1, double x2, double y2, double z2){
        minX = x1;
        minY = y1;
        minZ = z1;

        maxX = x2;
        maxY = y2;
        maxZ = z2;

        sortX();
        sortY();
        sortZ();
    }


    public HitBoxBuilder(Vec3d start, Vec3d end){
        minX = start.x;
        minY = start.y;
        minZ = start.z;

        maxX = end.x;
        maxY = end.y;
        maxZ = end.z;
        sortX();
        sortY();
        sortZ();
    }


    public HitBoxBuilder(AxisAlignedBB h){
        minX = h.minX;
        minY = h.minY;
        maxX = h.maxX;
        maxY = h.maxY;
        minZ = h.minZ;
        maxZ = h.maxZ;
    }


    public AxisAlignedBB toHitbox(){
        return new AxisAlignedBB(minX, minY,minZ, maxX, maxY, maxZ);
    }


    protected void swapXZ(){
        double buf = minX;
        minX = minZ;
        minZ = buf;
        buf = maxX;
        maxX = maxZ;
        maxZ = buf;
        sortX();
        sortZ();
    }


    protected void sortX(){
        if(minX>maxX){
            double buf = maxX;
            maxX = minX;
            minX = buf;
        }
    }


    protected void sortY(){
        if(minY>maxY){
            double buf = maxY;
            maxY = minY;
            minY = buf;
        }
    }


    protected void sortZ(){
        if(minZ>maxZ){
            double buf = maxZ;
            maxZ = minZ;
            minZ = buf;
        }
    }


    public HitBoxBuilder invertX(){
        maxX = 1-maxX;
        minX = 1-minX;
        sortX();
        return this;
    }


    public HitBoxBuilder invertY(){
        maxY = 1-maxY;
        minY = 1-minY;
        sortY();
        return this;
    }


    public HitBoxBuilder invertZ(){
        maxZ = 1-maxZ;
        minZ = 1-minZ;
        sortZ();
        return this;
    }


    public HitBoxBuilder rotate180(){
        invertX();
        return invertZ();
    }


    public HitBoxBuilder rotate90(){
        swapXZ();
        return invertX();
    }


    public HitBoxBuilder rotate270(){
        swapXZ();
        return invertZ();
    }


    @Override
    public String toString(){
        return String.format(
                "%s,%s,%s:%s,%s,%s"
                ,minX,minY,minZ,maxX,maxY,maxZ
        );
    }


    public static String convertMcHitboxToString(AxisAlignedBB b){
        return String.format("%.2f,%.2f,%.2f:%.2f,%.2f,%.2f",
                b.minX,b.minY,b.minZ,b.maxX,b.maxY,b.maxZ
        );
    }
}