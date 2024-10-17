package plus.fserver.utls.collections;

import plus.tson.utl.alloc.ConcurrentCasAllocator;


public class PooledLongQueue {
    //private static final int CACHED_QUEUE_SEGMENTS_COUNT = 4096;
    private static final int QUEUE_SEGMENT_SIZE = 1024;
    private final Pool pool;
    Segment cur, last;
    private int size;
    private volatile boolean empty;
    
    public PooledLongQueue(final Pool pool) {
        this.size = 0;
        this.pool = pool;
    }
    
    public int size() {
        return this.size;
    }


    public boolean isEmpty() {
        return this.empty;
    }


    public synchronized void add(final long val) {
        if (this.cur == null) {
            this.empty = false;
            final Segment segment = this.pool.acquire();
            this.last = segment;
            this.cur = segment;
            segment.index = 1;
            segment.longArray[0] = val;
        } else {
            Segment last;
            if ((last = this.last).index == 1024) {
                final Segment ret = last.next = last.pool.acquire();
                ret.index = 1;
                ret.longArray[0] = val;
                this.last = ret;
            } else {
                last.longArray[last.index++] = val;
            }
        }
        ++this.size;
    }


    public LongQueueIterator iterator() {
        return new LongQueueIterator(this);
    }


    synchronized void clear() {
        Segment next;
        for (Segment segment = this.cur; segment != null; segment = next) {
            next = segment.next;
            segment.release();
        }
        this.size = 0;
        this.cur = null;
        this.last = null;
        this.empty = true;
    }


    public static class LongQueueIterator {
        PooledLongQueue parent;
        Segment cur;
        long[] curArray;
        int index;
        int capacity;

        public LongQueueIterator(){}
        
        LongQueueIterator(final PooledLongQueue parent) {
            final Segment cur = parent.cur;
            this.parent = parent;
            this.cur = cur;
            if (this.cur != null) {
                this.curArray = cur.longArray;
                this.capacity = cur.index;
            }
        }


        public void init(final PooledLongQueue parent){
            final Segment cur = parent.cur;
            this.parent = parent;
            this.index = 0;
            this.cur = cur;
            if (cur != null) {
                this.curArray = cur.longArray;
                this.capacity = cur.index;
            } else {
                this.curArray = null;
                this.capacity = 0;
            }
        }

        
        public boolean hasNext() {
            return this.cur != null;
        }


        public long next() {
            final long ret = this.curArray[this.index++];
            if (this.index == this.capacity) {
                this.index = 0;
                Segment cur = this.cur = this.cur.next;
                if (cur != null) {
                    this.curArray = cur.longArray;
                    this.capacity = cur.index;
                }
            }
            return ret;
        }


        public void finish() {
            this.curArray = null;
            PooledLongQueue parent;
            if((parent = this.parent) != null) {
                parent.clear();
                this.parent = null;
            }
        }
    }


    public static class Pool extends ConcurrentCasAllocator {
        public Pool() {
            super(64, 64);
        }

        Segment acquire() {
            Object segment = tryAllocRaw();
            if (segment == null) {
                return new Segment(this);
            }
            return (Segment) segment;
        }


        void release(final Segment segment) {
            super.freeObj(segment);
        }
    }


    static final class Segment {
        private final long[] longArray;
        private int index;
        private Segment next;
        private final Pool pool;
        
        Segment(final Pool pool) {
            this.longArray = new long[QUEUE_SEGMENT_SIZE];
            this.index = 0;
            this.pool = pool;
        }


        void release() {
            this.index = 0;
            this.next = null;
            this.pool.release(this);
        }
    }
}