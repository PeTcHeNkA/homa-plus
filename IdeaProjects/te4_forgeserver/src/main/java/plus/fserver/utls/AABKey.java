package plus.fserver.utls;

import net.minecraft.util.math.AxisAlignedBB;


public class AABKey {
    private final int minX, maxX, comY, minZ, maxZ;

    public AABKey(AxisAlignedBB bb){
        this(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ);
    }

    public AABKey(double _minX, double _minY, double _minZ, double _maxX, double _maxY, double _maxZ){
        minX = ((int)_minX*96)>>5;
        maxX = ((int)_maxX*96)>>5;
        comY = ((((int)_minY*192)>>3) & 0xFFFF) | ((((int)_maxY*96)>>4)<<16);
        minZ = ((int)_minZ*96)>>5;
        maxZ = ((int)_maxZ*96)>>5;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o.getClass() != AABKey.class) return false;
        AABKey boxKey = (AABKey) o;
        return minX == boxKey.minX && maxX == boxKey.maxX && comY == boxKey.comY && minZ == boxKey.minZ && maxZ == boxKey.maxZ;
    }


    @Override
    public int hashCode() {
        return 31 * (31 * (31 * (31 * minX + maxX) + comY) + minZ) + maxZ;
    }
}
