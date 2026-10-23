package plus.fserver.server.elca;

import net.minecraft.util.math.AxisAlignedBB;


public final class AABKey {
    private int minX, maxX, comY, minZ, maxZ;

    private AABKey(int n1, int  n2, int  n3, int n4, int  n5){
        minX = n1;maxX = n2;comY = n3;minZ = n4;maxZ = n5;
    }

    public AABKey(AxisAlignedBB bb){
        this(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ);
    }

    public AABKey(double _minX, double _minY, double _minZ, double _maxX, double _maxY, double _maxZ){
        minX = ((int)_minX*96)>>4;
        maxX = ((int)_maxX*96)>>4;
        comY = ((((int)_minY*192)>>3) & 0xFFFF) | ((((int)_maxY*96)>>4)<<16);
        minZ = ((int)_minZ*96)>>4;
        maxZ = ((int)_maxZ*96)>>4;
    }


    public void init(AxisAlignedBB bb){
        init(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ);
    }


    public void init(double _minX, double _minY, double _minZ, double _maxX, double _maxY, double _maxZ){
        minX = ((int)_minX*96)>>4;
        maxX = ((int)_maxX*96)>>4;
        comY = ((((int)_minY*96)>>2) & 0xFFFF) | ((((int)_maxY*96)>>2)<<16);
        minZ = ((int)_minZ*96)>>4;
        maxZ = ((int)_maxZ*96)>>4;
    }


    @Override
    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o.getClass() != AABKey.class) return false;
        AABKey boxKey = (AABKey) o;
        return minX == boxKey.minX && maxX == boxKey.maxX && comY == boxKey.comY && minZ == boxKey.minZ && maxZ == boxKey.maxZ;
    }


    @Override
    public int hashCode() {
        return 31 * (31 * (31 * (31 * minX + maxX) + comY) + minZ) + maxZ;
    }


    public AABKey copy(){
        return new AABKey(minX, maxX, comY, minZ, maxZ);
    }
}
