package plus.asyutl.thread;


public class ThreadedPool<T extends Runnable> extends AbcPool<T> {
    protected short completed = 0;
    protected Thread currentThread;

    public ThreadedPool(int count){
        super(count);
    }


    @Override
    AbcPoolThread<T> create(AbcPool<T> pool) {
        return new AbcPoolThread<T>(pool) {
            @Override
            void onEndLoop() {
                synchronized (parent) {
                    ++completed;
                    if(completed == parent.threads.length){
                        if(currentThread != null) {
                            synchronized (currentThread) {
                                currentThread.notify();
                                currentThread = null;
                            }
                        }
                    }
                }
                synchronized (this) {
                    try {
                        active = false;
                        wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        };
    }


    public void asyncFlushAll(){
        if(currentThread!=null)return;
        completed = 0;
        currentThread = Thread.currentThread();
        for (AbcPoolThread<T> thread:threads){
            if(!thread.active){
                synchronized (thread) {
                    thread.notify();
                }
            }
        }
        synchronized (currentThread) {
            try {
                currentThread.wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}