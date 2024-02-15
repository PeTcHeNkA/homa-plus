package plus.fserver.utls.collections;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;


public class PooledLongQueue {
    private static final int CACHED_QUEUE_SEGMENTS_COUNT = 4096;
    private static final int QUEUE_SEGMENT_SIZE = 1024;
    private final Pool pool;
    private Segment cur;
    private Segment last;
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
        }
        if (this.last.index == 1024) {
            final Segment ret = this.last.next = this.last.pool.acquire();
            ret.longArray[ret.index++] = val;
            this.last = ret;
        } else {
            this.last.longArray[this.last.index++] = val;
        }
        ++this.size;
    }


    public LongQueueIterator iterator() {
        return new LongQueueIterator(this.cur);
    }


    private synchronized void clear() {
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


    public class LongQueueIterator {
        private Segment cur;
        private long[] curArray;
        private int index;
        private int capacity;
        
        private LongQueueIterator(final Segment cur) {
            this.cur = cur;
            if (this.cur != null) {
                this.curArray = cur.longArray;
                this.capacity = cur.index;
            }
        }

        
        public boolean hasNext() {
            return this.cur != null;
        }


        public long next() {
            final long ret = this.curArray[this.index++];
            if (this.index == this.capacity) {
                this.index = 0;
                this.cur = this.cur.next;
                if (this.cur != null) {
                    this.curArray = this.cur.longArray;
                    this.capacity = this.cur.index;
                }
            }
            return ret;
        }


        public void finish() {
            PooledLongQueue.this.clear();
        }
    }


    public static class Pool {
        private final Deque<Segment> segmentPool;
        
        public Pool() {
            this.segmentPool = new ConcurrentLinkedDeque<>();
            //this.segmentPool = new ArrayDeque<>();
        }


        private Segment acquire() {
            if (this.segmentPool.isEmpty()) {
                return new Segment(this);
            }
            return this.segmentPool.pop();
        }


        private void release(final Segment segment) {
            if (this.segmentPool.size() < 4096) {
                this.segmentPool.push(segment);
            }
        }
    }


    private static class Segment {
        private final long[] longArray;
        private int index;
        private Segment next;
        private final Pool pool;
        
        private Segment(final Pool pool) {
            this.longArray = new long[1024];
            this.index = 0;
            this.pool = pool;
        }
        
        private void release() {
            this.index = 0;
            this.next = null;
            this.pool.release(this);
        }
    }
}