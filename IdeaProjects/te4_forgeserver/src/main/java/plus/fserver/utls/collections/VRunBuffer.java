package plus.fserver.utls.collections;


@Deprecated
public class VRunBuffer implements Runnable{
    public final Runnable[] tasks;

    public VRunBuffer(int size) {
        this.tasks = new Runnable[0];
    }

    @Override
    public void run() {
        for (Runnable r:tasks)r.run();
    }
}