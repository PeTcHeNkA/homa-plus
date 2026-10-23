package plus.lyn;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Arrays;


public class Prefix {
    private static final Int2ObjectOpenHashMap<String> EMPTY = new Int2ObjectOpenHashMap<>();
    private static final Int2ObjectOpenHashMap<String>[] EMPTY_ARR = new Int2ObjectOpenHashMap[]{};
    public final IdxString str;
    private Int2ObjectOpenHashMap<String>[] mergedRoots = EMPTY_ARR;

    public Prefix(IdxString str) {
        this.str = str;
    }

    public String merge(char[] str, int start, int count){
        if(count < 0 || count >= mergedRoots.length)return null;
        return mergedRoots[count].get(WordIndex.inst.werbToIndex6(str, start, start+(count>6?6:count)));
    }


    public void mergeWith(String... roots){
        for (String root:roots) mergeWith(root);
        for (Int2ObjectOpenHashMap<String> map:mergedRoots) map.trim();
    }


    private void mergeWith(String root){
        int size;
        ensureSize(size = root.length());

        Int2ObjectOpenHashMap<String> map = mergedRoots[size-1];
        if(map == EMPTY){
            map = mergedRoots[size-1] = new Int2ObjectOpenHashMap<>();
        }
        String prev = map.put(WordIndex.inst.werbToIndex6(root), str.value + root);
        if(prev != null){
            throw new IllegalArgumentException("Roots ["+prev+"] and ["+root+"] cant be used together!");
        }
    }


    private void ensureSize(int max){
        if(mergedRoots == EMPTY_ARR){
            mergedRoots = new Int2ObjectOpenHashMap[max];
            Arrays.fill(mergedRoots, EMPTY);
        } else {
            if(max > mergedRoots.length){
                Int2ObjectOpenHashMap<String>[] newRoots = new Int2ObjectOpenHashMap[max];
                Arrays.fill(newRoots, EMPTY);
                System.arraycopy(mergedRoots, 0, newRoots, 0, mergedRoots.length);
                mergedRoots = newRoots;
            }
        }
    }
}



