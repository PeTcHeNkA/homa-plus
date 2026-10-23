package plus.te4net.mds.util;

import java.util.HashMap;


public class FrMap<T> {
    private final HashMap<FMather, T> matches = new HashMap<>();
    private final HashMap<String, T> equals = new HashMap<>();

    public T get(String key){
        T val = matches.get(new QwFirst(key));
        if(val==null){
            val = matches.get(new QwEnd(key));
            if(val==null) return equals.get(key);
        }
        return val;
    }

    public void add(String math, T val){
        int len = math.length();
        if(math.charAt(0)=='*'){
            if(len!=8)throw new RuntimeException("Lenght for this reg ["+math+"] is 8!");
            matches.put(new KeyEnd(math.substring(1)), val);
            return;
        }
        if(math.charAt(len-1)=='*'){
            if(len!=5)throw new RuntimeException("Lenght for this reg ["+math+"] is 5!");
            matches.put(new KeyFirst(math.substring(0,len-1)), val);
            return;
        }
        equals.put(math, val);
    }
}


interface FMather{
    String getRealValue();
}


class KeyFirst implements FMather{
    private final String key;
    KeyFirst(String key) {
        this.key = key;
    }

    @Override
    public final int hashCode() {
        return key.hashCode();
    }


    @Override
    public final boolean equals(Object obj) {
        if(obj instanceof KeyFirst){
            KeyFirst k = (KeyFirst) obj;
            return k.key.equals(key);
        }
        return false;
    }


    public String getRealValue(){
        return key;
    }
}


final class QwFirst extends KeyFirst{
    private final String realValue;
    QwFirst(String key) {
        super(key.substring(0, 4));
        realValue = key;
    }
    @Override
    public String getRealValue() {
        return realValue;
    }
}


class KeyEnd implements FMather{
    private final String key;

    KeyEnd(String key) {
        this.key = key;
    }


    @Override
    public final int hashCode() {
        return key.hashCode();
    }


    @Override
    public final boolean equals(Object obj) {
        if(obj instanceof KeyEnd){
            KeyEnd k = (KeyEnd) obj;
            return k.key.equals(key);
        }
        return false;
    }


    @Override
    public String getRealValue() {
        return key;
    }
}


final class QwEnd extends KeyEnd{
    private final String realValue;

    QwEnd(String key) {
        super(toKey(key));
        this.realValue = key;
    }


    private static String toKey(String k){
        int len = k.length();
        if(len>7){
            return k.substring(len-7);
        } else {
            return k;
        }
    }


    @Override
    public String getRealValue() {
        return realValue;
    }
}