package plus.fserver.utls.collections;


public final class Int1LinkedList {
    private Node root, cur, prev;

    public void add(int n){
        root = new Node(root, n);
    }

    public int get(int cursor){
        Node cur = root;
        while (--cursor>0){
            cur = root.parent;
        }
        return cur.value;
    }


    public Node getListAndRem(){
        Node result = root;
        root = null;
        return result;
    }


    public void resetIter(){
        prev = null;
        cur = root;
    }


    public boolean isEmpty(){
        return root == null;
    }


    public int getCur(){
        return cur.value;
    }


    public void next(){
        prev = cur;
        cur = cur.parent;
    }


    public void removeCur(){
        if(prev != null) cur = prev.parent = cur.parent;
        else cur = cur.parent;
    }


    public boolean hasCur(){
        return cur != null;
    }


    public boolean hasNext(){
        return cur.parent != null;
    }


    public void removeVal(int value){
        Node prev = null;
        Node cur = root;
        while (cur != null && cur.value != value) {
            prev = cur;
            cur = cur.parent;
        }
        if(cur != null){
            if(prev != null)prev.parent = cur.parent;
            else root = root.parent;
        }
    }


    public void addAll(Int1LinkedList list){
        list.resetIter();
        while (list.hasCur()){
            add(list.getCur());
            list.next();
        }
        list.resetIter();
    }


    public static final class Node{
        private final int value;
        private Node parent;

        public Node(Node parent, int value) {
            this.parent = parent;
            this.value = value;
        }


        public int getValue() {
            return value;
        }


        public Node getParent() {
            return parent;
        }


        @Override
        public String toString() {
            return "{value=" + value + ", parent=" + parent + '}';
        }
    }


    @Override
    public String toString() {
        return "{" + "root=" + root + '}';
    }
}
