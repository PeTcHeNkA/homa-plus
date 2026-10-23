package plus.fserver.utls;



public final class AABKey {
    private int minX, maxX, comY, minZ, maxZ;

    private AABKey(int n1, int  n2, int  n3, int n4, int  n5){
        minX = n1;maxX = n2;comY = n3;minZ = n4;maxZ = n5;
    }

    public AABKey(){}


    public AABKey(double _minX, double _minY, double _minZ, double _maxX, double _maxY, double _maxZ){
        minX = ((int)_minX*96)>>5;
        maxX = ((int)_maxX*96)>>5;
        comY = ((((int)_minY*192)>>3) & 0xFFFF) | ((((int)_maxY*96)>>4)<<16);
        minZ = ((int)_minZ*96)>>5;
        maxZ = ((int)_maxZ*96)>>5;
    }


    public void init(double _minX, double _minY, double _minZ, double _maxX, double _maxY, double _maxZ){
        minX = ((int)_minX*96)>>5;
        maxX = ((int)_maxX*96)>>5;
        comY = ((((int)_minY*192)>>3) & 0xFFFF) | ((((int)_maxY*96)>>4)<<16);
        minZ = ((int)_minZ*96)>>5;
        maxZ = ((int)_maxZ*96)>>5;
    }


    public void initV2(double _minX, double _minY, double _minZ, double _maxX, double _maxY, double _maxZ){
        minX = ((int)Math.scalb(_minX, 6))>>5;
        maxX = ((int)Math.scalb(_maxX, 6))>>5;
        comY = ((((int)Math.scalb(_minY, 7))>>3) & 0xFFFF) | ((((int)Math.scalb(_maxY, 6))>>4)<<16);
        minZ = ((int)Math.scalb(_minZ, 6))>>5;
        maxZ = ((int)Math.scalb(_maxZ, 6))>>5;
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
