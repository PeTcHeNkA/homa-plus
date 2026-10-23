package plus.bw.world.gen;

import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import plus.bw.utl.RandomCtx;
import plus.bw.world.Matrix;
import java.util.function.Consumer;


public class RiverGen implements Consumer<Matrix> {
    private final int min, maxDelta, minRiverSize, maxRiverSizeDelta;
    private final Int2IntFunction function;
    private RandomCtx ctx;

    public RiverGen(int min, int max, int minSize, int maxSize, Int2IntFunction function) {
        this.function = function;
        this.min = min;
        this.maxDelta = max - min;
        this.minRiverSize = minSize;
        this.maxRiverSizeDelta = maxSize - minSize;
    }


    public void setCtx(RandomCtx ctx) {
        this.ctx = ctx;
    }


    @Override
    public void accept(Matrix matrix) {
        ctx.reset();
        PointCache points = new PointCache();
        int def = ctx.defSeed();
        for (int count = ctx.next(maxDelta) + min;count > 0;--count){
            makeRiverAt(points, ctx.next(matrix.height()), ctx.next(matrix.width()), matrix);
        }
        ctx.reset(def);
    }


    private static class PointCache extends Int2ObjectOpenHashMap<IntOpenHashSet> {
        public boolean addPoint(int x, int y){
            IntOpenHashSet set = get(x);
            if(set == null){
                put(x, new IntOpenHashSet());
                return true;
            }
            return set.add(y);
        }


        @Override
        public void clear(){
            for (IntOpenHashSet set:values())set.clear();
        }
    }


    public final class SingleCreator{
        private final PointCache cache = new PointCache();


        public void makeRiverAt(int x, int y, Matrix matrix){
            RiverGen.this.makeRiverAt(cache, x, y, matrix);
        }
    }


    public SingleCreator creator(){
        return new SingleCreator();
    }


    private void makeRiverAt(PointCache cache, int x, int y, Matrix matrix){
        int pd = ctx.next2(3);

        for (int size = ctx.next(maxRiverSizeDelta) + minRiverSize, lim = size; size>0; --size){
            int temp = ctx.next2(7);

            if(temp>3) temp = pd;
            switch (temp) {
                case 0: {
                    x += 1;
                    break;
                }
                case 1: {
                    x -= 1;
                    break;
                }
                case 2: {
                    y += 1;
                    break;
                }
                case 3: {
                    y -= 1;
                    break;
                }
            }

            pd = temp;
            if(matrix.legalPos(x, y)) {
                matrix.apply(x, y, function);

                if(!cache.addPoint(x, y)){
                    if(--lim > 0) ++size;
                    else return;
                }
            } else {
                if(--lim < 0) return;
            }
        }
    }
}


