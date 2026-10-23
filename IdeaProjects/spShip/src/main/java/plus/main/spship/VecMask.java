package plus.main.spship;

import net.minecraft.world.phys.Vec3D;


public abstract class VecMask {
    public abstract Vec3D apply(Vec3D vec);
    public static final VecMask EMPTY = new VecMask() {
        @Override
        public Vec3D apply(Vec3D vec) {
            return vec;
        }
    };
    public static VecMask build(float x, float y, float z){
        if(x==0 && y==0 && z==0){
            return EMPTY;
        } else {
            return new NormalVecMask(x, y, z);
        }
    }
}


class NormalVecMask extends VecMask{
    final float mX;
    final float mY;
    final float mZ;

    public NormalVecMask(float x, float y, float z){
        mX = x;
        mY = y;
        mZ = z;
    }
    @Override
    public Vec3D apply(Vec3D vec){
        return new Vec3D(vec.b+mX, vec.c+mY, vec.d+mZ);
    }
}