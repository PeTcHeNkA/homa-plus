package plus.te4net.mds.crypt;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


public abstract class CryptoLibBase<T extends CryptKey,V> extends CryptoMap<T,V> implements Runnable {
    private final ScheduledExecutorService service;
    private ScheduledFuture<?> future;
    private final int delay;
    private int apiUsages = 0;

    protected CryptoLibBase(ScheduledExecutorService service, int delay) {
        this.service = service;
        this.delay = delay;
    }


    private void useApi(){
        apiUsages++;
        if(future == null) future = service.scheduleAtFixedRate(this, delay, delay, TimeUnit.MILLISECONDS);
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
    public final void run() {
        if(apiUsages == 0){
            future.cancel(true);
            future = null;
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