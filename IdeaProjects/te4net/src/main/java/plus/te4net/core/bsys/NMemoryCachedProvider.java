package plus.te4net.core.bsys;

import plus.asyutl.shoulder.IBasicTask;
import plus.te4net.core.api.IDataProvider;
import plus.te4net.core.node.HEWorker;
import plus.tson.utl.Te4HashMap;
import java.util.Arrays;


public class NMemoryCachedProvider extends Te4HashMap<NMemoryCachedProvider.Key,byte[]> implements IDataProvider, IBasicTask {
    private final IDataProvider parent;
    private final int timer;
    private int time = 0;
    private String[] items;

    public NMemoryCachedProvider(IDataProvider parent, int time) {
        this.timer = time;
        this.parent = parent;
        HEWorker.shoulder.addTask(this);
    }


    @Override
    public boolean needRun() {
        if(time++>timer){
            time=0;
            items = null;
            for (Key key : keysToArray(keySet().toArray(new Key[0]))) {
                if (!key.used()) remove(key);
            }
        }
        return false;
    }


    @Override
    public void run() {}


    @Override
    public byte[] getRaw(String... path) {
        Key key = new Key(path);
        Node<Key, byte[]> node = getNode(key);
        if(node!=null){
            node.getKey().mark = 3;
            return node.getValue();
        }
        byte[] result = parent.getRaw(path);
        put(key, result);
        return result;
    }


    @Override
    public void saveRaw(byte[] data, String... path) {
        put(new Key(path), data);
        items = null;
        HEWorker.pool.put(() -> parent.saveRaw(data, path));
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