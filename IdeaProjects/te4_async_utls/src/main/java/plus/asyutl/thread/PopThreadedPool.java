package plus.asyutl.thread;


public class PopThreadedPool<T extends Runnable> extends AbcPool<T> {
    protected short actived;

    public PopThreadedPool(int count){
        super(count);
    }


    @Override
    AbcPoolThread<T> create(AbcPool<T> pool) {
        return new AbcPoolThread<T>(pool) {
            @Override
            void onEndLoop() {
                synchronized (this) {
                    active = false;
                    --actived;
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        };
    }


    public boolean isActive(){
        return actived>0;
    }


    @Override
    public void put(T obj) {
        super.put(obj);
        if(actived<threads.length){
            for (AbcPoolThread<T> thread : threads) {
                synchronized (thread) {
                    if (!thread.active) {
                        ++actived;
                        thread.notify();
                    }
                }
            }
        }
    }
}