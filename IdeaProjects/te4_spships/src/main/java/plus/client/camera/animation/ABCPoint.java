package plus.client.camera.animation;

import plus.utls.MathUtils;


public abstract class ABCPoint<T extends ABCPoint<T, P>, P> {

    public abstract T range(T p);


    public abstract T copy();


    public abstract T add(T p);


    public abstract T pow(float f);


    public abstract void render(P p);


    public static double rangeInTime(float f1, float f2, float time){
        return f1 + MathUtils.range(f1, f2) * time;
    }
}