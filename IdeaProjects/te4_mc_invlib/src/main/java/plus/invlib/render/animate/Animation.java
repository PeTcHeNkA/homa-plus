package plus.invlib.render.animate;

import plus.invlib.render.Updateable;


public class Animation<T extends ABCPoint<T, P>, P> implements Updateable {
    public T current;
    public T target;
    private T w;

    private long time = 0;
    public float speed = 250;


    public Animation(float speed, T target){
        this.speed = speed;
        update();
        this.target = target;
        //setTarget(target);
    }


    public void stop(){
        time+=speed;
    }


    public Animation(){
        update();
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
        return lTime>1?1f:lTime;
    }


    public T get(){;
        float lTime = calcCurTime();
        if(lTime>1){
            return target;
        } else {
            return current.add(w.pow(lTime));
        }
    }
}