package plus.asyutl.shoulder;


public interface ITaskConsumer {
    void addTask(IBasicTask task);
    void addTask(int after, Runnable r);
}