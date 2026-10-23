package plus.asyutl.thread;

import plus.asyutl.buf.Buffer;


public abstract class AbcPool<T extends Runnable> extends Buffer<T> {
    protected final AbcPoolThread<T>[] threads;

    public AbcPool(int count) {
        threads = new AbcPoolThread[count];
        for(int i=0;i<threads.length;i++){
            AbcPoolThread<T> thr = create(this);
            thr.setName("Thread Pool #"+i);
            threads[i] = thr;
        }
    }

    abstract AbcPoolThread<T> create(AbcPool<T> pool);


    protected static abstract class AbcPoolThread<T extends Runnable> extends Thread{
        protected final AbcPool<T> parent;
        protected final Field<T> field = new Field<>();
        protected boolean active = true;

        AbcPoolThread(AbcPool<T> parent) {
            this.parent = parent;
            setDaemon(true);
            start();
        }

        public void run(){
            while (true) {
                active = true;
                while (parent.flush(field)) {
                    try {
                        field.value.run();
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
                onEndLoop();
            }
        }
        abstract void onEndLoop();
    }
}