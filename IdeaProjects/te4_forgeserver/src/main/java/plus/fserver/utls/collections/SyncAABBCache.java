package plus.fserver.utls.collections;

import plus.fserver.utls.AABKey;


public class SyncAABBCache<T> extends SyncTe4Cache<AABKey,T>{
    private final int clearTime;//500ms = 10 ticks
    private int timer = 0;

    public SyncAABBCache(int clearTime) {
        this.clearTime = clearTime;
    }


    public void tick(){
        removeByStamp(prev());
        if(++timer>=clearTime)timer = 0;
    }



    public void add(AABKey key, T value) {
        put(key, value, timer);
    }


    public int prev(){
        if(timer==0)return clearTime;
        return timer-1;
    }
}