package plus.fserver.utls.collections;

import java.util.Iterator;

public class NotNullArrayIter<K> implements Iterator<K> {
    private final K[] values;
    private int cursor = 0;
    private K next;

    public NotNullArrayIter(K[] byId) {
        this.values = byId;
    }

    @Override
    public boolean hasNext() {
        return next != null;
    }

    @Override
    public K next() {
        K prev = next;
        int i = cursor;
        for (K[] byId = this.values; i < byId.length; i++) {
            K newNext = byId[i];
            if (newNext != null) {
                next = newNext;
                break;
            }
        }
        cursor = i;
        return prev;
    }
}
