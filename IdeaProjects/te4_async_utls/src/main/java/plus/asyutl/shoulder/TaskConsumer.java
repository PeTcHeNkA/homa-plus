package plus.asyutl.shoulder;

import java.util.concurrent.ConcurrentLinkedQueue;


public class TaskConsumer implements ITaskConsumer{
    protected final ConcurrentLinkedQueue<IBasicTask> tasks = new ConcurrentLinkedQueue<>();

    protected boolean enabled = false;


    protected void tick(){
        if(enabled) {
            enabled = false;
            for (IBasicTask task : tasks) {
                enabled = true;
                if (task.needRun()) {
                    task.run();
                    tasks.remove(task);
                }
            }
        }
    }


    public boolean isEnabled(){
        return enabled;
    }


    @Override
    public void addTask(IBasicTask task){
        tasks.add(task);
        enabled = true;
    }


    @Override
    public void addTask(int after, Runnable r){
        tasks.add(new ITask(after, r));
        enabled = true;
    }


    private static class ITask extends Task {
        private final Runnable r;

        public ITask(int after, Runnable r) {
            super(after);
            this.r = r;
        }


        @Override
        public void run() {
            r.run();
        }
    }


    public static abstract class Task implements IBasicTask {
        private int runAfter;

        public Task(int after){
            runAfter = after;
        }


        public boolean needRun(){
            return --runAfter<1;
        }
    }
}
