package plus.fserver.utls.collections;

import net.minecraft.util.math.AxisAlignedBB;
import plus.fserver.utls.AABKey;


public class SyncListCache<V> extends SyncTe4Cache<AABKey, ArrayListE6<V>>{
    private final int clearTime, clearTime2;//500ms = 10 ticks
    private int timer = 0, timer2, timer3, maxV, maxN;
    private AABKey swap = new AABKey(0,0,0,0,0,0);

    public SyncListCache(int clearTime, int clearTime2) {
        this.clearTime = clearTime;
        this.clearTime2 = clearTime2;
    }

    static class BoxCache<V> extends SyncListCache<V>{
        public BoxCache(int clearTime, int clearTime2) {
            super(clearTime, clearTime2);
        }

        @Override
        protected void cleanRed1(){
            for (int i=0;i<vals.length;i++){
                ArrayListE6 list = (ArrayListE6) vals[i];
                if(list == null) return;
                if(list.data.length > 6){
                    list.data = ArrayListE6.EMPTY_ARR;
                    list.size = 0;
                }
                else {
                    Object[] arr = list.data;
                    for(int j=3;j<arr.length;j++) arr[j] = null;
                }
            }
        }
    }


    public void tick(){
        removeByStamp(prev());
        ++timer;

        maxV = vCursor < maxV ? maxV : vCursor;
        maxN = nCursor<maxN?maxN:nCursor;

        if(timer > clearTime){
            timer = 0;
            if(++timer2 >= clearTime2){
                timer2 = 0;

                if(++timer3 >= 5){
                    cleanRed2();
                    timer3 = 0;
                } else {

                    cleanRed1();
                }
            }
        }
    }


    protected void cleanRed2(){
        l1: if(maxV > vCursor){
            int usages = (maxV - vCursor) * 20;

            //Object[] vals = this.vals;
            maxV = Math.min(maxV, vals.length);

            if(usages >= maxN)break l1;

            Object[] newVals = new Object[usages];
            System.arraycopy(vals, 0, newVals, 0, usages);
            vals = newVals;
//            for(int cursor = usages; cursor < maxV; ++cursor){
//                vals[cursor] = null;
//            }
            vCursor = usages-1;
            //System.out.println("FREE "+(maxV-usages)+" values");
            maxV = 0;
        }

        l2: if(maxN > nCursor){
            int usages = (maxN - nCursor) * 20;

            //Object[] vals = this.emptyNodes;
            maxN = Math.min(maxN, vals.length);

            if(usages >= maxN)break l2;

            Node[] nodes = new Node[usages];
            System.arraycopy(emptyNodes, 0, nodes, 0, usages);
            emptyNodes = nodes;

//            for(int cursor = usages; cursor < maxN; ++cursor){
//                vals[cursor] = null;
//            }
            nCursor = usages-1;
            //System.out.println("FREE "+(maxN-usages)+" nodes");
            maxN = 0;
        }
    }


    protected void cleanRed1(){
        for (int i=0;i<vals.length;i++){
            ArrayListE6 list = (ArrayListE6) vals[i];
            if(list == null) return;
            if(list.data.length > 6){
                list.data = ArrayListE6.EMPTY_ARR;
                list.size = 0;
            }
            else list.clearAll();
        }
    }


    public int prev(){
        if(timer==0){
            return clearTime;
        }
        return timer-1;
    }


   // int c = 0, c2 = 0, c3 = 0;
    public ArrayListE6<V> allocArr(){
        ArrayListE6<V> list = tryAllocVal();
        if(list != null){
            //++c;
            list.fclear();
            return list;
        }
        //++c3;
        //System.out.println("NEN1");
        return new ArrayListE6<>();
    }


    public void freeArr(ArrayListE6<V> list){
        super.freeVal(list);
    }


    final Node<AABKey,ArrayListE6<V>> alloc0(int hash, AABKey key, ArrayListE6<V> value, int stamp){
        if(nCursor > 0){
            Node<AABKey,ArrayListE6<V>> node = emptyNodes[nCursor--];
            swap = node.key;
            node.init(hash, key, value, stamp);
            return node;
        }
        return new Node<>(hash, key.copy(), value, null, stamp);
    }


    public AABKey key(AxisAlignedBB bb){
        swap.init(bb);
        return swap;
    }


    public void add(AABKey key, ArrayListE6<V> v){
        putValN1(key.hashCode(), key, v, timer);
    }


    private void putValN1(int hash, AABKey key, ArrayListE6<V> value, int stamp) {
        Node<AABKey,ArrayListE6<V>>[] tab = table; Node<AABKey,ArrayListE6<V>> node; int i;
        if ((node = tab[i = (tab.length - 1) & hash]) == null) {
            tab[i] = alloc0(hash, key, value, stamp);
        } else {
            Node<AABKey,ArrayListE6<V>> e; AABKey k;
            if (node.hash == hash && ((k = node.key) == key || (key != null && key.equals(k))))
                e = node;
            else if (node instanceof TreeNode)
                e = ((TreeNode<AABKey,ArrayListE6<V>>)node).putTreeVal(tab, hash, key, value, stamp);
            else {
                for (i = 0; ; ++i) {
                    if ((e = node.next) == null) {
                        node.next = alloc0(hash, key, value, stamp);
                        if (i >= 7) // -1 for 1st
                            treeifyBin(tab, hash);
                        break;
                    }
                    if (e.hash == hash &&
                            ((k = e.key) == key || (key != null && key.equals(k))))
                        break;
                    node = e;
                }
            }
            if (e != null) { // existing mapping for key
                freeVal(e.value);
                e.value = value;
                //return e;
            }
        }
        ++modCount;
        if (++size > threshold) resize();
        //return null;
    }
}