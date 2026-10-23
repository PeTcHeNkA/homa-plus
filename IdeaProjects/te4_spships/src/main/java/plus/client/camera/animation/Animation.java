package plus.client.camera.animation;


import plus.tson.utl.Tuple;

public class Animation<T extends ABCPoint<T, P>, P>{
    public T current;
    public T target;
    private T w;

    private long time = 0;
    public float speed = 250;


    public Animation(float speed, T target){
        this.speed = speed;
        this.target = target;
    }


    public void stop(){
        time = 0;
    }


    public void resume(){
        time = System.currentTimeMillis();
    }


    public void preStart(T point){
        current = point;
        target = point;
    }


    public void setTarget(T point){
        current = get();
        target = point;
        time = System.currentTimeMillis();
        w = current.range(target);
    }


    private float calcCurTime(){
        return ((System.currentTimeMillis() - time)) / speed;
    }


    public float curTime(){
        float lTime = calcCurTime();
        if(lTime<0)return 0;
        return lTime>1?1f:lTime;
    }


    public Tuple<Boolean, T> getCurPoint(){
        float lTime = calcCurTime();
        if(lTime>1){
            return new Tuple<>(false, target);
        } else {
            return new Tuple<>(true, current.add(w.pow(lTime)));
        }
    }


    public T get(){;
        float lTime = calcCurTime();
        if(lTime>=1){
            return target;
        } else {
            return current.add(w.pow(lTime));
        }
    }
}