package plus.util.map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class RangableMap<Value> {

    private final HashMap<Integer, Value> map = new HashMap<>();
    private final List<Range> ranges = new ArrayList<>();

    public void put(Value v, int... range){
        if(range.length<1 || v == null)return;
        Range r = new Range(range[0], range.length>1?range[1]:range[0]);
        put(v, r);
    }


    public void put(Value v, Range r){
        ranges.add(r);
        map.put(ranges.size()-1, v);
    }


    public Value getOrFixNull(double value){
        if(ranges.get(0).lower(value))return map.get(0);
        int i = ranges.size()-1;
        if(ranges.get(i).lower(value))return map.get(i);
        i = 0;
        for(Range r:ranges){
            if(r.equals(value))return map.get(i);
            ++i;
        }
        return null;
    }
//    public Value get(int value){
//        int i = 0;
//        for(Range r:ranges){
//            if(r.equals(value))return map.get(i);
//            ++i;
//        }
//        return null;
//    }


    public Value get(double value){
        int i = 0;
        for(Range r:ranges){
            if(r.equals(value))return map.get(i);
            ++i;
        }
        return null;
    }


//    public Value getOrDefault(int value, Value def){
//        Value result = get(value);
//        return result==null?def:result;
//    }


    public Value getOrDefault(double value, Value def){
        Value result = get(value);
        return result==null?def:result;
    }
}