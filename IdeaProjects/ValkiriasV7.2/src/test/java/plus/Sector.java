package plus;

import java.util.List;

public interface Sector extends Region {

    Region getRegionOfCords(int x, int y, int z);


    public static Sector buildSector(List<Region1> regions){
        return buildSector(regions.toArray(new Region1[0]));
    }


    public static Sector buildSector(Region1... regions){
        if(regions.length>3){
            return new Sector1(regions);
        } else return new SmallSector(regions);
    }
}


class SmallSector implements Sector{

    private final Region parent;
    private final Region1[] regions;


    public SmallSector(Region1... regions){
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;

        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        int maxZ = Integer.MIN_VALUE;

        for(Region1 r:regions){
            if(r.X_1 > maxX)maxX = r.X_1;
            if(r.Y_1 > maxY)maxY = r.Y_1;
            if(r.Z_1 > maxZ)maxZ = r.Z_1;
            if(r.X_1 < minX)minX = r.X_1;
            if(r.Y_1 < minY)minY = r.Y_1;
            if(r.Z_1 < minZ)minZ = r.Z_1;
        }
        parent = new Region1(minX, maxY, minZ, maxX, maxY, maxZ);
        this.regions = regions;
    }


    @Override
    public boolean containsArea(Region1 r) {
        return parent.containsArea(r);
    }


    @Override
    public boolean containsCords(int x, int y, int z) {
        return parent.containsCords(x, y, z);
    }


    @Override
    public int typeRelationOf(Region1 r) {
        return parent.typeRelationOf(r);
    }


    @Override
    public Region getRegionOfCords(int x, int y, int z) {
        for(Region1 r:regions){
            if(r.containsCords(x, y, z))return r;
        }
        return null;
    }


    @Override
    public String toString(){
        return parent.toString();
    }

}