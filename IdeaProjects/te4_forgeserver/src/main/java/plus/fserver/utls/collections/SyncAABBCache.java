package plus.fserver.utls.collections;

import net.minecraft.util.math.AxisAlignedBB;
import plus.fserver.utls.AABKey;
import java.util.List;


public class SyncAABBCache<V> extends SyncTe4Cache<AABKey, ArrayListE6<V>>{
    private final int clearTime;//500ms = 10 ticks
    private int timer = 0;
    private AABKey swap = new AABKey(0,0,0,0,0,0);

    public SyncAABBCache(int clearTime) {
        this.clearTime = clearTime;
    }


    public void tick(){
        removeByStamp(prev());
        ++timer;
        if(timer > clearTime){
            //System.out.println("N:"+(c3-c2)+"+"+c2+",C:"+c);
            timer = 0;
        }
    }


    public int prev(){
        if(timer==0){
            return clearTime;
        }
        return timer-1;
    }


    //int c = 0, c2 = 0, c3 = 0;
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