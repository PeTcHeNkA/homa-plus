package plus.fserver.utls.collections;

import net.minecraft.network.NetworkManager;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.*;


public class NetMenegerChunkedSyncList {
    private final Chunk[] chunks;
    private int size = 0;

    public NetMenegerChunkedSyncList(int countChunks){
        this.chunks = new Chunk[countChunks];
        for (int i = 0; i < chunks.length; i++){
            chunks[i] = new Chunk();
        }
    }


    public interface Task{
        boolean updateManager(NetworkManager manager);
    }


    private class Chunk{
        private NetworkManager[] managers = new NetworkManager[4];
        int size = 0;

        synchronized void add(NetworkManager manager){
            if(managers.length == size){
                NetworkManager[] newManagers = new NetworkManager[managers.length + 4];
                System.arraycopy(managers, 0, newManagers, 0, managers.length);
                managers = newManagers;
            }
            managers[size++] = manager;
        }


        private void removeAt(int index){
            if(size == 1){
                if(index != 0)throw new IndexOutOfBoundsException();
                managers[0] = null;
            }
            NetworkManager last = managers[--size];
            managers[index] = last;
            managers[size] = null;
        }


        public synchronized int size(){
            return size;
        }


        public synchronized void forEach(Task task){
            for (int i = 0; i < size; i++){
                NetworkManager manager = managers[i];
                if(manager != null){
                    if(!task.updateManager(manager)) {
                        removeAt(i);
                        --i;
                        inc();
                    }
                }
            }
        }
    }


    private synchronized int addAndGet(){
        return ++size;
    }


    private synchronized void inc(){
        --size;
    }


    public synchronized void add(NetworkManager manager){
        addAndGet();
        final Chunk[] chunks = this.chunks;
        int min = 0;
        for (int i = 0; i < chunks.length; i++){
            min = Math.min(min, chunks[i].size);
        }
        chunks[min].add(manager);
    }


    public synchronized void forEach(Task task){
        for (Chunk chunk:chunks)chunk.forEach(task);
    }


    public synchronized AsyncUpdateTask makeTask(Task task){
        return new AsyncUpdateTask(task);
    }


    public class AsyncUpdateTask implements ScheduledFuture<Object>{
        boolean isCanceled = false;
        private final Task task;
        private ScheduledFuture<?>[] futures;

        private AsyncUpdateTask(Task task) {
            this.task = task;
        }

        public synchronized AsyncUpdateTask scheduleAtFixedRate(ScheduledExecutorService service, long tickRate, TimeUnit unit){
            futures = new ScheduledFuture[chunks.length];
            for (int i = 0; i < chunks.length; i++) {
                futures[i] = service.scheduleAtFixedRate(
                        new ChunkTask(task, chunks[i]), 5, tickRate, unit
                );
            }
            return this;
        }


        public synchronized void cancel(){
            for (ScheduledFuture<?> future:futures){
                future.cancel(true);
            }
        }


        @Override
        public long getDelay(@NotNull TimeUnit unit) {
            return futures[0].getDelay(unit);
        }


        @Override
        public int compareTo(@NotNull Delayed o) {
            return futures[0].compareTo(o);
        }


        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            boolean res = false;
            for (ScheduledFuture<?> future:futures){
                res = res || future.cancel(mayInterruptIfRunning);
            }
            isCanceled = true;
            futures = null;
            return res;
        }


        @Override
        public boolean isCancelled() {
            return isCanceled;
        }


        @Override
        public boolean isDone() {
            return !isCanceled;
        }


        @Override
        public Object get() throws InterruptedException, ExecutionException {
            throw new UnsupportedOperationException();
        }


        @Override
        public Object get(long timeout, @NotNull TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            throw new UnsupportedOperationException();
        }
    }


    private static class ChunkTask implements Runnable{
        private final Task task;
        private final Chunk chunk;

        private ChunkTask(Task task, Chunk chunk) {
            this.task = task;
            this.chunk = chunk;
        }

        @Override
        public void run() {
            chunk.forEach(task);
        }
    }
}
