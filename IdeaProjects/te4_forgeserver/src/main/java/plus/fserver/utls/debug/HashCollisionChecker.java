package plus.fserver.utls.debug;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;


public class HashCollisionChecker {
    private final IntOpenHashSet set = new IntOpenHashSet();
    private long count = 0;
    private long overlaps = 0;

    public void feed(Object obj){
        ++count;
        int h = obj.hashCode();
        h = h ^ h >> 16;
        if(!set.add(h)) ++overlaps;
    }

    public void feed(int hash){
        ++count;
        int h = hash;
        h = h ^ h >> 16;
        if(!set.add(h)) ++overlaps;
    }


    @Override
    public String toString() {
        return "Completion: "+(int)((count*100d)/Integer.MAX_VALUE)
                +"% Overlaps chance: "+((int)(((overlaps*100d)/count)))+"%";
    }
}