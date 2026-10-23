package plus.util.map;

import java.util.HashMap;


public class SHashMap<K, V> extends HashMap<K, V> {

    public SHashMap(){}

    public SHashMap(String rawData, Encoder<K> eKey, Encoder<V> eValue){
        if(rawData==null || rawData.isEmpty())return;
        for(String s:rawData.split(",")){
            String[] s2 = s.split(":");
            super.put(eKey.encode(s2[0]), eValue.encode(s2[1]));
        }
    }


    @Override
    public String toString(){
        StringBuilder s = new StringBuilder();
        for(K key: super.keySet()){
            s.append(key.toString()).append(":").append(super.get(key).toString()).append(",");
        }
        return s.toString();
    }
}