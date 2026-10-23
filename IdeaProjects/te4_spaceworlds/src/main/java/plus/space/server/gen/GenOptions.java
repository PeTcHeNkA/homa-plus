package plus.space.server.gen;

import io.netty.util.collection.IntObjectHashMap;
import plus.fserver.utls.math.FastRandom;
import plus.tson.TsonInt;
import plus.tson.TsonList;
import plus.tson.TsonMap;
import plus.tson.TsonObj;
import java.util.Map;
import java.util.Random;


public class GenOptions {
    private final IntObjectHashMap<TsonMap> frames = new IntObjectHashMap<>();

    public GenOptions(TsonMap map){
        frames.put(0, map);
//        for(int i=0;i<0;i++){
//
//        }
    }



    public TsonMap getMutated(int key){
        return mutate(frames.get(key));
    }


    private TsonMap mutate(TsonMap map){
        TsonMap result = map.clone();
        TsonMap mutations = result.remove("mutations").getMap();
        for(Map.Entry<String,TsonObj> etr:mutations.entrySet()){
            TsonUrl url = new TsonUrl(etr.getKey());
            RamdomVal val = randomValOfTson(etr.getValue());
            url.set(result, val.get(FastRandom.INST));
        }
        if(result.getOrDefaultBool("mutate-result",false)){
            result.remove("mutate-result");
            return mutate(result);
        }
        return result;
    }


    private static RamdomVal randomValOfTson(TsonObj obj){
        if(obj.isMap())return new RandomNum(obj.getMap());
        if(obj.isList()){
            if(obj.getList().size()>1) return new RandomObj(obj.getList());
            else obj = obj.getList().get(0);
        }

        TsonObj finalObj = obj;
        return r -> finalObj;
    }
}


class RandomObj implements RamdomVal{
    private final TsonObj[] values;
    RandomObj(TsonList list){
        this.values = list.toArray(new TsonObj[0]);
    }

    @Override
    public TsonObj get(Random r) {
        return values[r.nextInt(values.length)];
    }
}


class RandomNum implements RamdomVal {
    private final int delta, max;
    public RandomNum(TsonMap map){
        int min = map.getOrDefaultInt("min", 0);
        int max = map.getInt("max");
        delta = min;
        this.max = max - min;
    }

    @Override
    public TsonObj get(Random r) {
        return new TsonInt(delta+r.nextInt(max));
    }
}



interface RamdomVal {
    TsonObj get(Random r);
}



class TsonUrl{
    private final TsonUrlNode[] nodes;

    TsonUrl(String patch) {
        String[] patch0 = patch.split(":");
        nodes = new TsonUrlNode[patch0.length];
        for(int i=0;i< patch0.length;i++){
            String cur = patch0[i];
            switch (cur.charAt(0)){
                case '+':
                    nodes[i] = new TsonUrlListAdd(Integer.parseInt(cur.substring(1)));
                    break;
                case '#':
                    nodes[i] = new TsonUrlList(Integer.parseInt(cur.substring(1)));
                    break;
                case '&':
                    nodes[i] = new TsonUrlMap(cur.substring(1));
                    break;
                default:
                    throw new IllegalArgumentException(cur);
            }
        }
    }

    public TsonObj get(TsonObj from){
        for (TsonUrlNode node : nodes) from = node.get(from);
        return from;
    }

    public void set(TsonObj target, TsonObj obj){
        TsonObj prevTarget = null;
        TsonUrlNode last = null;

        int size = nodes.length-1;
        for (int i=0;i<=size;i++) {
            TsonUrlNode node = nodes[i];
            prevTarget = target;
            target = node.get(target);
            last = node;
        }
        last.set(prevTarget, obj);
    }
}



interface TsonUrlNode{
    TsonObj get(TsonObj obj);
    void set(TsonObj target, TsonObj obj);
}


class TsonUrlMap implements TsonUrlNode{
    private final String key;

    TsonUrlMap(String key) {
        this.key = key;
    }

    @Override
    public TsonObj get(TsonObj obj) {
        return obj.getMap().get(key);
    }

    @Override
    public void set(TsonObj target, TsonObj obj) {
        target.getMap().put(key, obj);
    }
}


class TsonUrlList implements TsonUrlNode{
    final int key;
    TsonUrlList(int key) {
        this.key = key;
    }
    @Override
    public TsonObj get(TsonObj obj) {
        return obj.getList().get(key);
    }

    @Override
    public void set(TsonObj target, TsonObj obj) {
        target.getList().set(key, obj);
    }
}


class TsonUrlListAdd extends TsonUrlList{
    TsonUrlListAdd(int key) {
        super(key);
    }
    @Override
    public TsonObj get(TsonObj obj) {
        int size = obj.getList().size();
        if(!(key>=size)) return obj.getList().get(key);
        return null;
    }
    @Override
    public void set(TsonObj target, TsonObj obj) {
        target.getList().add(key, obj);
    }
}