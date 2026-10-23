package plus.util.map;

import java.util.HashSet;


public class SHashSet<T> extends HashSet<T> {

    public SHashSet(){}

    public SHashSet(String rawData, Encoder<T> encoder){
        String[] strings = rawData.split(",");
        for(String str:strings){
            T r = encoder.encode(str);
            if(r != null) super.add(encoder.encode(str));
        }
    }
}