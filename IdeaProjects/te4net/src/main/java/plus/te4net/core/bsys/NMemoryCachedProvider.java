package plus.te4net.core.bsys;

import plus.te4net.core.api.IDataProvider;
import plus.tson.utl.Te4HashMap;
import java.util.Arrays;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


public class NMemoryCachedProvider extends Te4HashMap<NMemoryCachedProvider.Key,byte[]> implements IDataProvider, Runnable {
    private final ScheduledExecutorService service;
    private final IDataProvider parent;
    private final int delay;
    private String[] items;
    private ScheduledFuture<?> future;

    public NMemoryCachedProvider(ScheduledExecutorService service, IDataProvider parent, int delay) {
        this.service = service;
        this.delay = delay;
        this.parent = parent;
    }


    @Override
    public void run() {
        items = null;
        for (Key key : keysToArray(keySet().toArray(new Key[0]))) {
            if (!key.used()) remove(key);
        }
        if(isEmpty()) {
            future.cancel(false);
            future = null;
        }
    }


    private void restartTimerIfNeed(){
        if(future == null) future = service.scheduleAtFixedRate(this, delay, delay, TimeUnit.MILLISECONDS);
    }


    @Override
    public byte[] getRaw(String... path) {
        Key key = new Key(path);
        Node<Key, byte[]> node = getNode(key);
        if(node != null){
            node.getKey().mark = 3;
            return node.getValue();
        }
        byte[] result = parent.getRaw(path);
        put(key, result);
        restartTimerIfNeed();
        return result;
    }


    @Override
    public void saveRaw(byte[] data, String... path) {
        put(new Key(path), data);
        restartTimerIfNeed();
        items = null;
        service.execute(() -> parent.saveRaw(data, path));
    }


    @Override
    public String[] getItems(String... path) {
        if(items!=null)return items;
        return items = parent.getItems(path);
    }


    static final class Key{
        private int mark = 3;
        private final String[] key;

        Key(String[] key) {
            this.key = key;
        }


        private boolean used(){
            return --mark>0;
        }


        @Override
        public boolean equals(Object o) {
            return Arrays.equals(key, ((Key) o).key);
        }


        @Override
        public int hashCode() {
            return Arrays.hashCode(key);
        }
    }
}