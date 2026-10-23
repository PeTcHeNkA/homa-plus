package plus.invlib.render.animate;

public abstract class ABCPoint<T extends ABCPoint<T, P>, P> {

    public abstract T range(T p);


    public abstract T copy();


    public abstract T add(T p);


    public abstract T pow(float f);


    public abstract void render(P p);


    public static float rangeInTime(float f1, float f2, float time){
        return f1 + range(f1, f2) * time;
    }


    public static float range(float f1, float f2){
        if(f1>0){
            if(f2>0){
                return f1>f2 ? -(f1-f2) : f2-f1;
            } else {
                return f2-f1;
            }
        } else {
            if(f2>0){
                return f2-f1;
            } else {
                return f1<f2 ? -(f1-f2) : f2-f1;
            }
        }
    }
}