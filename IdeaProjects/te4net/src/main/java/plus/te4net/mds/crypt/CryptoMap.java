package plus.te4net.mds.crypt;

import plus.tson.utl.Te4HashMap;
import java.nio.charset.StandardCharsets;
import java.util.Random;


public abstract class CryptoMap<T extends CryptKey, V> extends Te4HashMap<T, V> {
    private static final int limit = Short.MAX_VALUE*2;

    protected abstract T newKey(long time, short usages);
    protected abstract T encodeKey(byte[] bytes, int delta);
    protected abstract V encodeValue(byte[] data, int delta);


    public T createKey(int time, int usages, V value){
        long curTime = System.currentTimeMillis();
        short curUsages = getOf(usages);
        T key = newKey(curTime+time, curUsages);
        Node<T, V> node = putN(key, value);
        while ( !(node==null || curTime>node.getKey().getTime()) ){
            key = newKey(curTime+time, curUsages);
            node = putN(key, value);
        }
        return key;
    }


    public boolean hasKey(String key){
        return containsKey(new CryptKey(key));
    }


    public T getKey(String key){
        Node<T, V> node = getNode(new CryptKey(key));
        if(node==null)return null;
        return node.getKey();
    }


    public V use(String key){
        Node<T, V> val = useNode(key);
        if(val==null)return null;
        return val.getValue();
    }


    public Node<T,V> getNode(String key){
        if(key==null)return null;
        return getNode(new CryptKey(key));
    }


    public Node<T, V> useNode(String key){
        Node<T, V> node = getNode(key);
        if(node==null)return null;
        T nKey = node.getKey();
        if(!nKey.use()){
            remove(node.getKey());
            return null;
        } else {
            if(!nKey.maybeUse()){
                remove(node.getKey());
            }
        }
        return node;
    }


    private static short getOf(int i){
        if(i>limit) return Short.MAX_VALUE;
        if(i<1)return Short.MIN_VALUE;
        return (short) (Short.MAX_VALUE-i);
    }


    @Override
    public final String toString() {
        return new String(toBytes(), StandardCharsets.UTF_8);
    }


    public final byte[] toBytes(){
        final int aSize = additionalSize();
        final int shift = 22+aSize;
        int size = size()*shift;
        byte[] data = new byte[size];
        int delta = 0;
        for(CryptKey key:keysToArray(new CryptKey[size()])){
            Node<T, V> node = getNode(key);
            node.getKey().save(data, delta);
            byte[] bData = node.getValue().toString().getBytes(StandardCharsets.UTF_8);
            if(bData.length!=aSize)throw new RuntimeException("Value data != "+aSize+"!");
            System.arraycopy(bData, 0, data, delta+22, aSize);
            delta+=shift;
        }
//        for(CryptKey key:values()){
//            key.save(data, delta);
//
//            delta+=shift;
//        }
        return data;
    }


    public final CryptoMap<T, V> fromBytes(byte[] data){
        final long curTime = System.currentTimeMillis();
        final int aSize = additionalSize();
        final int shift = 22+aSize;
        for(int delta=0;delta<data.length;delta+=shift){
            T key = encodeKey(data, delta);
            V val = encodeValue(data, delta+22);
            if(key.getTime()>curTime){
                put(key, val);
            }
        }
        return this;
    }


    protected int additionalSize(){
        return 0;
    }
}


