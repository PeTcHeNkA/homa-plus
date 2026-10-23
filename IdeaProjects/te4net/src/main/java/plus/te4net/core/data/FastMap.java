package plus.te4net.core.data;


@Deprecated
public class FastMap<T> {
    private Pair<T>[] nodes = new Pair[0];
    private Pair<T> first;

    public void put(String key, T val) {
        int high = nodes.length - 1;
        int keyHs = key.hashCode();
        if(high==0){
            Pair<T> pair = new Pair<>(keyHs, val);
            Pair<T>[] newNodes = new Pair[2];
            if(keyHs>nodes[0].hs) {
                newNodes[1] = pair;
                newNodes[0] = nodes[0];
            } else {
                newNodes[0] = pair;
                newNodes[1] = nodes[0];
            }
            nodes = newNodes;
            return;
        }
        if(high==-1){
            nodes = new Pair[1];
            nodes[0] = new Pair<>(keyHs, val);
            return;
        }
        int low = 0;
        int mid = 0;
        while (low <= high) {
            mid = (low + high) >>> 1;
            int midVal = nodes[mid].hs;
            if (midVal < keyHs)
                low = mid + 1;
            else if (midVal > keyHs)
                high = mid - 1;
            else {
                break;
            }
        }
        Pair<T>[] newNodes = new Pair[nodes.length+1];
        if(mid+1 == nodes.length){
            System.arraycopy(nodes, 0, newNodes, 0, nodes.length);
            newNodes[nodes.length] = new Pair<>(keyHs, val);

        } else {
            System.arraycopy(nodes, 0, newNodes, 0, mid);
            newNodes[mid] = new Pair<>(keyHs, val);
            System.arraycopy(nodes, mid, newNodes, mid+1, nodes.length-mid);
        }
        nodes = newNodes;
        compile();
    }


    public T get(String key0){
        int keyHs = key0.hashCode();
        Pair<T> pr = first;
        while (pr != null){
            if(pr.hs==keyHs)return pr.val;
            if(pr.hs<keyHs){
                pr = pr.next;
            } else {
                pr = pr.prev;
            }
        }
        return null;
    }


    private void compile(){
        first = nodes[nodes.length >>> 1];
        for(int i=0;i< nodes.length;i++){
            nodes[i] = nodes[i].cleared();
        }
        compile(0, nodes.length);
    }


    private void compile(int low, int high){
        if(high<=low)return;
        int mid = (low + high) >>> 1;
        int midLow = ((low+mid-1) >>> 1);
        int midHigh = ((high+mid) >>> 1)+1;
        if(mid != midLow && midLow < nodes.length)
            nodes[mid] = nodes[mid].setPrev(nodes[midLow]);
        if(mid != midHigh && midHigh < nodes.length)
            nodes[mid] = nodes[mid].setNext(nodes[midHigh]);
        compile(low, mid);
        compile(mid+1, high);
    }



    private static final class Pair<T>{
        private final int hs;
        private final T val;
        private final Pair<T> next;
        private final Pair<T> prev;
        private Pair(int hs, T val) {
            this.hs = hs;
            this.val = val;
            next = null;
            prev = null;
        }
        private Pair(int hs, T val, Pair<T> next, Pair<T> prev){
            this.hs = hs;
            this.val = val;
            this.next = next;
            this.prev = prev;
        }
        private Pair<T> setPrev(Pair<T> prev){
            return new Pair<>(hs, val, next, prev);
        }
        private Pair<T> setNext(Pair<T> next){
            return new Pair<>(hs, val, next, prev);
        }
        private Pair<T> cleared(){
            return new Pair<>(hs, val, null, next);
        }
    }
}