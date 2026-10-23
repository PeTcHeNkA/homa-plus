package plus.lyn.utl;

import plus.lyn.Word;
import plus.lyn.unit.Proposal;
import plus.tson.utl.Te4HashMap;
import java.util.Arrays;


public class PropMap {
    private static final Object[] EMPTY = new Object[]{};
    private final Layer rootWerbLayer = new Layer();

    private static final class Layer extends Te4HashMap<String, Layer>{
        private Object[] items = EMPTY;

        private Layer newLayer(String key){
            Layer result = get(key);
            if(result == null) fput(key, result = new Layer());
            return result;
        }
        private void add(Object obj){
            items = Arrays.copyOf(items, items.length+1);
            items[items.length-1] = obj;
        }
    }


    public static class Result<T> extends FList<T>{
        private int position;
        Word[] words;
        public Result(){};

        public int getStart(){
            return position;
        }

        public Word[] werbs(){
            return words;
        }


        @Override
        public void clear(){
            words = null;
            position = 0;
        }
    }


    public void get(Result<?> result, String key, Proposal.Builder builder){
        get(result, result.words = builder.buildWerbs(key));
    }


    public void get(Result<?> result, Word... werbs){
        Object[] prev = EMPTY;
        Layer layer;
        for (int i = 0, max = 0; i < werbs.length; i++){
            layer = rootWerbLayer;

            for (int cur = i, cMax = 0; cur < werbs.length; cur++) {
                Word werb = werbs[cur];
                if ((layer = layer.get(werb.indexPart())) == null) {
                    break;
                } else {
                    if ((++cMax) > max) {
                        prev = layer.items;
                        max = cMax;
                        result.position = i;
                    }
                }
            }
        }
        result.items = prev;
        result.cursor = prev.length;
    }


    public void put(String key, Proposal.Builder builder, Object value){
        put(value, builder.buildWerbs(key));
    }


    public void put(Object value, Word... werbs){
        Layer layer = rootWerbLayer;
        for (Word werb:werbs) layer = layer.newLayer(werb.indexPart());
        layer.add(value);
    }
}