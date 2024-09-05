package plus.fserver.utls.collections;


import java.util.function.Consumer;

public class FlatMap<T> {
    private Node[] nodes;
    private int mask;
    private int count = 0;

    public FlatMap(){
        this(256);
    }

    public FlatMap(int initSize){
        nodes = new Node[initSize];
        mask = initSize - 1;
    }


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
                if(temp.chunk == null){
                    temp.x = x;
                    temp.z = z;
                    temp.chunk = chunk;
                    onInsert();
                    return;
                }
                if(temp.x == x && temp.z == z){
                    temp.chunk = chunk;
                    if(chunk == null)count -= 1;
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

                count -= 1;
                return (T) node.chunk;
            }
            node = (prev = node).next;
        }
        return null;
    }


    private void onInsert(){
        if((count += 1) * 100 < mask * 75)return;
        //rehash
        final Node[] oldNodes = nodes;
        count = 0;
        final Node[] curNodes = nodes = new Node[(mask = (mask << 1) | 1)+1];

        int index = 0;
        Node first = oldNodes[0];
        while(first != null){
            put(first.x, first.z, (T) first.chunk);
            first = first.next;
        }

        for(int i = 1, s = oldNodes.length, cs = curNodes.length; i < s; ++i){
            while (index < cs){
                if(curNodes[index] == null) {
                    Node node = oldNodes[i - 1];
                    if(node != null) {
                        curNodes[index] = node;
                        ++index;

                        Node add = node.next;
                        node.next = null;
                        node.chunk = null;

                        while (add != null && index < cs) {
                            if (curNodes[index] == null) {
                                curNodes[index] = add;
                                Node rNext = add.next;
                                add.next = null;
                                add.chunk = null;
                                add = rNext;
                            }
                            ++index;
                        }
                        break;
                    } else {
                        ++index;
                    }
                } else {
                    ++index;
                }
            }

            Node node = oldNodes[i];
            while(node != null){
                T chunk = (T) node.chunk;
                if(chunk != null) put(node.x, node.z, chunk);
                node = node.next;
            }
        }
    }


    public void clear(){
        Node[] nodes = this.nodes;
        for (int i = 0, s = nodes.length; i < s; i++) {
            Node node = nodes[i];
            if(node != null) {
                node.chunk = null;
                node.next = null;
            }
        }
        count = 0;
    }


    public void forEach(Consumer<T> consumer){
        Node[] nodes = this.nodes;
        for (int i = 0, s = nodes.length; i < s; i++) {
            Node node = nodes[i];
            while (node != null) {
                Object chunk = node.chunk;
                if(chunk != null)consumer.accept((T) chunk);
                node = node.next;
            }
        }
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


    public T[] toArray(T[] src){
        if(src.length < count){
            src = (T[]) java.lang.reflect.Array.newInstance(src.getClass().getComponentType(), count);
        }
        Node[] nodes = this.nodes;
        int index = 0;
        for(int i = 0, s = nodes.length; i < s; ++i){
            Node node = nodes[i];
            while(node != null){
                T chunk = (T) node.chunk;
                if(chunk != null){
                    src[index++] = chunk;
                }
                node = node.next;
            }
        }
        return src;
    }
}