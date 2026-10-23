package plus.bw.utl.collect;

import java.util.concurrent.Executor;
import java.util.function.Consumer;


public class AsyncTasksGroup<T> {
    private final ATask[] tasks;
    private int completeStatus = 0;
    private final int completeNum;
    private final Consumer<T> onComplete;
    private byte mark = 0;
    private final T bus;

    public AsyncTasksGroup(Consumer<T> onComplete, T bus, Consumer<T>... tasks){
        if(onComplete == null || tasks.length == 0 || tasks.length>30)throw new IllegalArgumentException();
        this.bus = bus;
        this.onComplete = onComplete;
        this.tasks = new ATask[tasks.length];
        for (int i = 0; i< this.tasks.length; i++){
            this.tasks[i] = new Task(tasks[i], i);
        }
        completeNum = 1 << (tasks.length)-1;
    }


    private synchronized void onComplete(int id){
        completeStatus |= (1<<id);
        if(completeStatus == completeNum){
            if(mark == 1){
                mark = 2;
                onComplete.accept(bus);
            }
        }
    }


    public boolean isActive(){
        return mark == 1;
    }


    public boolean isCompleted(){
        return mark == 2;
    }


    public void runAt(Executor executor){
        mark = 1;
        for (ATask task : tasks) executor.execute(task);
    }


    private abstract static class ATask implements Runnable{
        final int id;
        private ATask(int id) {
            this.id = id;
        }
    }


    private final class Task extends ATask{
        private final Consumer<T> parent;

        private Task(Consumer<T> parent, int id) {
            super(id);
            this.parent = parent;
        }

        @Override
        public void run() {
            try {
                parent.accept(bus);
            } catch (Exception e){
                e.printStackTrace();
            } finally {
                onComplete(id);
            }
        }
    }
}