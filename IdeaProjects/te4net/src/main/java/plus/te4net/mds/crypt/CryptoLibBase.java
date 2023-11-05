package plus.te4net.mds.crypt;

import plus.asyutl.shoulder.IBasicTask;
import static plus.te4net.core.node.HEWorker.shoulder;


public abstract class CryptoLibBase<T extends CryptKey,V> extends CryptoMap<T,V> implements IBasicTask {
    private final short skip;
    private short timer = 0;
    private int apiUsages = 0;
    boolean remove = false;
    boolean disabled = false;

    protected CryptoLibBase(short skip) {
        this.skip = skip;
    }


    private void useApi(){
        apiUsages++;
        if(disabled){
            disabled = false;
            shoulder.addTask(this);
        }
    }


    @Override
    public V use(String key) {
        useApi();
        return super.use(key);
    }


    @Override
    public Node<T, V> useNode(String key) {
        useApi();
        return super.useNode(key);
    }


    public Node<T, V> getNode(String key){
        useApi();
        return super.getNode(key);
    }


    @Override
    public T createKey(int time, int usages, V val) {
        useApi();
        return super.createKey(time, usages, val);
    }


    @Override
    public final boolean needRun() {
        if(timer++>skip){
            timer = 0;
            if(remove){
                disabled = true;
                return true;
            }
            run();
        }
        return false;
    }


    @Override
    public final void run() {
        if(apiUsages==0){
            remove = true;
            return;
        }
        else apiUsages = 0;
        onRun();
    }


    protected void onRun(){
        long cur = System.currentTimeMillis();
        for(CryptKey key:keysToArray(new CryptKey[size()])){
            if(key.getTime()>cur){
                int hash = hash(key);
                removeNode(hash, key, null, false, true);
            }
        }
    }
}