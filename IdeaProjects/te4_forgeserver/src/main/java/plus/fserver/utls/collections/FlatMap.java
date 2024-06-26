package plus.fserver.utls.collections;


public class FlatMap<T> {
    private Node[] nodes = new Node[256];
    private int mask = 255;
    private int count = 0;

    //31 > 43 > 37 > 21 > 17 > 13
    final int hash(int posX, int posZ){
        return (posX * 31 + posZ) & mask;
    }


    private static class Node{
        private int x, z;
        private Object chunk;
        private Node next;
    }


    public void put(int x, int z, T chunk){
        final int hash;
        final Node node = nodes[hash = hash(x, z)];
        if(node != null){
            Node temp = node;
            while(temp != null){
                if(temp.chunk == chunk)return;
                if(temp.chunk == null){
                    temp.x = x;
                    temp.z = z;
                    temp.chunk = chunk;
                    onInsert();
                    return;
                }
                temp = temp.next;
            }
        }
        final Node cur;
        (nodes[hash] = cur = new Node()).chunk = chunk;
        cur.x = x;
        cur.z = z;
        cur.next = node;
        onInsert();
    }


    public T remove(int x, int z){
        final int hash;
        Node node = nodes[hash = hash(x, z)], prev = null;
        while (node != null){
            if(node.x == x && node.z == z){
                if(prev == null) {
                    if (node.next != null)
                        nodes[hash] = node.next;
                    else
                        node.chunk = null;
                } else
                    prev.next   = node.next;

                --count;
                return (T) node.chunk;
            }
            node = (prev = node).next;
        }
        return null;
    }


    private void onInsert(){
        if((++count) * 100 < mask * 75)return;
        //rehash
        final Node[] oldNodes = nodes;
        count = 0;
        nodes = new Node[(mask = (mask << 1) | 1)+1];

        for(int i = 0, s = oldNodes.length; i < s; ++i){
            Node node = oldNodes[i];
            while(node != null){
                put(node.x, node.z, (T) node.chunk);
                node = node.next;
            }
        }
    }


    public void clear(){
        Node[] nodes = this.nodes;
        for (int i = 0; i < nodes.length; i++) {
            Node node = nodes[i];
            if(node != null) {
                node.chunk = null;
                node.next = null;
            }
        }
        count = 0;
    }


    public int size(){
        return count;
    }


    public T get(int x, int z){
        Node node = nodes[hash(x, z)];
        while (node != null){
            if(node.x == x && node.z == z)return (T) node.chunk;
            node = node.next;
        }
        return null;
    }


    public boolean contains(int x, int z){
        Node node = nodes[hash(x, z)];
        while (node != null){
            if(node.x == x && node.z == z)return true;
            node = node.next;
        }
        return false;
    }
}