//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.core;

import org.cache2k.*;
import org.cache2k.core.storageApi.*;
import org.cache2k.core.operation.*;
import java.util.concurrent.atomic.*;
import org.cache2k.core.util.*;
import org.cache2k.integration.*;

public class Entry<K, V> extends CompactEntry<K, V> implements CacheEntry<K, V>, StorageEntry, ExaminationEntry<K, V>
{
    public static final int EXPIRY_TIME_MIN = 32;
    static final int DATA_VALID = 16;
    static final int VIRGIN = 0;
    static final int READ_NON_VALID = 1;
    static final int REMOVE_PENDING = 2;
    static final int ABORTED = 3;
    static final int EXPIRED = 4;
    static final int EXPIRED_REFRESH_PENDING = 5;
    static final int EXPIRED_REFRESHED = 6;
    static final int GONE = 8;
    static final int GONE_OTHER = 15;
    private Object misc;
    private volatile long refreshTimeAndState;
    private static final AtomicLongFieldUpdater<Entry> STATE_UPDATER;
    public Entry next;
    public Entry prev;
    private int hotAndWeight;
    private static final int MODIFICATION_TIME_BITS = 44;
    private static final long MODIFICATION_TIME_BASE = 0L;
    private static final int MODIFICATION_TIME_SHIFT = 1;
    private static final long MODIFICATION_TIME_MASK = 17592186044415L;
    private static final int PS_BITS = 5;
    private static final int PS_MASK = 31;
    private static final int PS_POS = 44;
    private static final Entry LIST_REMOVED_MARKER;
    
    public Entry(final K _key, final int _hashCode) {
        super(_key, _hashCode);
    }
    
    public Entry() {
        this(null, 0);
    }
    
    public void setRefreshTime(final long t) {
        this.refreshTimeAndState = ((this.refreshTimeAndState & 0xFFFFF00000000000L) | (t - 0L << 1 & 0xFFFFFFFFFFFL));
    }
    
    public long getRefreshTime() {
        return (this.refreshTimeAndState & 0xFFFFFFFFFFFL) >> 1;
    }
    
    @Deprecated
    public long getLastModification() {
        throw new UnsupportedOperationException();
    }
    
    public static String num2processingStateText(final int ps) {
        switch (ps) {
            case 0: {
                return "DONE";
            }
            case 1: {
                return "READ";
            }
            case 2: {
                return "READ_COMPLETE";
            }
            case 3: {
                return "MUTATE";
            }
            case 4: {
                return "LOAD";
            }
            case 5: {
                return "LOAD_COMPLETE";
            }
            case 6: {
                return "COMPUTE";
            }
            case 7: {
                return "REFRESH";
            }
            case 8: {
                return "EXPIRY";
            }
            case 9: {
                return "EXPIRY_COMPLETE";
            }
            case 10: {
                return "WRITE";
            }
            case 11: {
                return "WRITE_COMPLETE";
            }
            case 12: {
                return "STORE";
            }
            case 13: {
                return "STORE_COMPLETE";
            }
            case 14: {
                return "NOTIFY";
            }
            case 15: {
                return "PINNED";
            }
            case 16: {
                return "EVICT";
            }
            case 17: {
                return "LOAD_ASYNC";
            }
            case 18: {
                return "WRITE_ASYNC";
            }
            case 19: {
                return "LAST";
            }
            default: {
                return "UNKNOWN";
            }
        }
    }
    
    public int getProcessingState() {
        return (int)(this.refreshTimeAndState >> 44 & 0x1FL);
    }
    
    private void setProcessingState(final int v) {
        this.refreshTimeAndState = this.withState(this.refreshTimeAndState, v);
    }
    
    private long withState(final long _refreshTimeAndState, final long _state) {
        return (_refreshTimeAndState & 0xFFFE0FFFFFFFFFFFL) | _state << 44;
    }
    
    public boolean checkAndSwitchProcessingState(final int ps0, final int ps) {
        final long rt = this.refreshTimeAndState;
        final long _expect = this.withState(rt, ps0);
        final long _update = this.withState(rt, ps);
        return Entry.STATE_UPDATER.compareAndSet(this, _expect, _update);
    }
    
    public void startProcessing(final int ps) {
        this.setProcessingState(ps);
    }
    
    public void nextProcessingStep(final int ps) {
        this.setProcessingState(ps);
    }
    
    public void processingDone() {
        this.notifyAll();
        this.setProcessingState(0);
    }
    
    public long getNextRefreshTime() {
        return this.nextRefreshTime;
    }
    
    public void setNextRefreshTime(final long _nextRefreshTime) {
        this.nextRefreshTime = _nextRefreshTime;
    }
    
    public void ensureAbort(final boolean _finished) {
        if (_finished) {
            return;
        }
        synchronized (this) {
            if (this.isVirgin()) {
                this.nextRefreshTime = 3L;
            }
            if (this.isProcessing()) {
                this.processingDone();
            }
        }
    }
    
    public boolean isProcessing() {
        return this.getProcessingState() != 0;
    }
    
    public void waitForProcessing() {
        if (!this.isProcessing()) {
            return;
        }
        boolean _interrupt = false;
        do {
            try {
                this.wait();
            }
            catch (InterruptedException ignore) {
                _interrupt = true;
            }
        } while (this.isProcessing());
        if (_interrupt) {
            Thread.currentThread().interrupt();
        }
    }
    
    public boolean isGettingRefresh() {
        return this.getProcessingState() == 7;
    }
    
    public final void removedFromList() {
        this.next = Entry.LIST_REMOVED_MARKER;
        this.prev = null;
    }
    
    public boolean isRemovedFromReplacementList() {
        return this.next == Entry.LIST_REMOVED_MARKER;
    }
    
    public boolean isNotYetInsertedInReplacementList() {
        return this.next == null;
    }
    
    public Entry shortCircuit() {
        this.prev = this;
        return this.next = this;
    }
    
    public final boolean isVirgin() {
        return this.nextRefreshTime == 0L;
    }
    
    public final boolean isDataValid() {
        return this.nextRefreshTime >= 16L || this.nextRefreshTime < 0L;
    }
    
    public final boolean hasFreshData(final InternalClock t) {
        return this.nextRefreshTime >= 16L || (this.needsTimeCheck() && t.millis() < -this.nextRefreshTime);
    }
    
    public void setExpiredState() {
        this.nextRefreshTime = 4L;
    }
    
    public boolean isExpired() {
        return this.nextRefreshTime == 4L;
    }
    
    public void setGone() {
        final long nrt = this.nextRefreshTime;
        if (nrt >= 0L && nrt < 8L) {
            this.nextRefreshTime = 8L + nrt;
            return;
        }
        this.nextRefreshTime = 15L;
    }
    
    public boolean isGone() {
        final long nrt = this.nextRefreshTime;
        return nrt >= 8L && nrt <= 15L;
    }
    
    public boolean needsTimeCheck() {
        return this.nextRefreshTime < 0L;
    }
    
    public boolean isHot() {
        return this.hotAndWeight < 0;
    }
    
    public void setHot(final boolean f) {
        if (f) {
            this.hotAndWeight |= Integer.MIN_VALUE;
        }
        else {
            this.hotAndWeight &= Integer.MAX_VALUE;
        }
    }
    
    public void setCompressedWeight(final int v) {
        this.hotAndWeight = ((this.hotAndWeight & Integer.MIN_VALUE) | v);
    }
    
    public int getCompressedWeight() {
        return this.hotAndWeight & Integer.MAX_VALUE;
    }
    
    public long getValueExpiryTime() {
        if (this.nextRefreshTime < 0L) {
            return -this.nextRefreshTime;
        }
        if (this.nextRefreshTime > 32L) {
            return this.nextRefreshTime;
        }
        return 0L;
    }
    
    public long getCreatedOrUpdated() {
        return this.getRefreshTime();
    }
    
    @Deprecated
    public long getEntryExpiryTime() {
        return 0L;
    }
    
    public String toString(final HeapCache c) {
        final StringBuilder sb = new StringBuilder();
        sb.append("Entry{");
        sb.append("id=").append(System.identityHashCode(this));
        sb.append(", lock=").append(num2processingStateText(this.getProcessingState()));
        sb.append(", key=");
        final Object _key = this.getKeyObj();
        if (_key == null) {
            sb.append(this.hashCode);
        }
        else {
            sb.append(_key);
            if (c != null && HeapCache.modifiedHash(_key.hashCode()) != this.hashCode) {
                sb.append(", keyMutation=true");
            }
        }
        final Object _valueOrException = this.getValueOrException();
        if (_valueOrException instanceof ExceptionWrapper) {
            sb.append(", exception=").append(((ExceptionWrapper)_valueOrException).getException().getClass().getSimpleName());
        }
        else if (_valueOrException != null) {
            sb.append(", valueId=").append(System.identityHashCode(_valueOrException));
        }
        else {
            sb.append(", value=null");
        }
        final long t = this.getRefreshTime();
        if (t > 0L) {
            sb.append(", refresh=").append(Util.formatMillis(t));
        }
        final long nrt = this.nextRefreshTime;
        if (nrt < 0L) {
            sb.append(", nextRefreshTime(sharp)=").append(Util.formatMillis(-nrt));
        }
        else if (nrt == Long.MAX_VALUE) {
            sb.append(", nextRefreshTime=ETERNAL");
        }
        else if (nrt >= 32L) {
            sb.append(", nextRefreshTime(timer)=").append(Util.formatMillis(nrt));
        }
        else {
            sb.append(", state=").append(nrt);
        }
        if (Thread.holdsLock(this)) {
            if (this.getTask() != null) {
                sb.append(", timerState=").append(this.getTask());
            }
        }
        else {
            sb.append(", timerState=skipped/notLocked");
        }
        sb.append("}");
        return sb.toString();
    }
    
    public String toString() {
        return this.toString(null);
    }
    
    public SimpleTimerTask getTask() {
        if (this.misc instanceof SimpleTimerTask) {
            return (SimpleTimerTask)this.misc;
        }
        final TaskPiggyBack pb = this.getPiggyBack(TaskPiggyBack.class);
        if (pb != null) {
            return pb.task;
        }
        return null;
    }
    
    public <X> X getPiggyBack(final Class<X> _class) {
        final Object obj = this.misc;
        if (!(obj instanceof PiggyBack)) {
            return null;
        }
        PiggyBack pb = (PiggyBack)obj;
        while (pb.getClass() != _class) {
            pb = pb.next;
            if (pb == null) {
                return null;
            }
        }
        return (X)pb;
    }
    
    public void setTask(final SimpleTimerTask v) {
        if (this.misc == null || this.misc instanceof SimpleTimerTask) {
            this.misc = v;
            return;
        }
        final TaskPiggyBack pb = this.getPiggyBack(TaskPiggyBack.class);
        if (pb != null) {
            pb.task = v;
            return;
        }
        this.misc = new TaskPiggyBack(v, (PiggyBack)this.misc);
    }
    
    private PiggyBack existingPiggyBackForInserting() {
        final Object _misc = this.misc;
        if (_misc instanceof SimpleTimerTask) {
            return new TaskPiggyBack((SimpleTimerTask)_misc, null);
        }
        return (PiggyBack)_misc;
    }
    
    public void setSuppressedLoadExceptionInformation(final ExceptionInformation w) {
        final LoadExceptionPiggyBack inf = this.getPiggyBack(LoadExceptionPiggyBack.class);
        if (inf != null) {
            inf.info = w;
            return;
        }
        this.misc = new LoadExceptionPiggyBack(w, this.existingPiggyBackForInserting());
    }
    
    public void resetSuppressedLoadExceptionInformation() {
        final LoadExceptionPiggyBack inf = this.getPiggyBack(LoadExceptionPiggyBack.class);
        if (inf != null) {
            inf.info = null;
        }
    }
    
    public ExceptionInformation getSuppressedLoadExceptionInformation() {
        final LoadExceptionPiggyBack inf = this.getPiggyBack(LoadExceptionPiggyBack.class);
        return (inf != null) ? inf.info : null;
    }
    
    public void setRefreshProbationNextRefreshTime(final long nrt) {
        final RefreshProbationPiggyBack inf = this.getPiggyBack(RefreshProbationPiggyBack.class);
        if (inf != null) {
            inf.nextRefreshTime = nrt;
            return;
        }
        this.misc = new RefreshProbationPiggyBack(nrt, this.existingPiggyBackForInserting());
    }
    
    public long getRefreshProbationNextRefreshTime() {
        final RefreshProbationPiggyBack inf = this.getPiggyBack(RefreshProbationPiggyBack.class);
        return (inf != null) ? inf.nextRefreshTime : 0L;
    }
    
    public static void removeFromList(final Entry e) {
        e.prev.next = e.next;
        e.next.prev = e.prev;
        e.removedFromList();
    }
    
    public static void insertInList(final Entry _head, final Entry e) {
        e.prev = _head;
        e.next = _head.next;
        e.next.prev = e;
        _head.next = e;
    }
    
    public static <E extends Entry> E insertIntoTailCyclicList(final E _head, final E e) {
        if (_head == null) {
            return (E)e.shortCircuit();
        }
        e.next = _head;
        e.prev = _head.prev;
        _head.prev = e;
        e.prev.next = e;
        return _head;
    }
    
    public static <E extends Entry> E removeFromCyclicList(final E _head, final E e) {
        if (e.next == e) {
            e.removedFromList();
            return null;
        }
        final Entry _eNext = e.next;
        e.prev.next = _eNext;
        e.next.prev = e.prev;
        e.removedFromList();
        return (E)((e == _head) ? _eNext : _head);
    }
    
    public static Entry removeFromCyclicList(final Entry e) {
        final Entry _eNext = e.next;
        e.prev.next = _eNext;
        e.next.prev = e.prev;
        e.removedFromList();
        return (_eNext == e) ? null : _eNext;
    }
    
    public static int getCyclicListEntryCount(Entry e) {
        if (e == null) {
            return 0;
        }
        final Entry _head = e;
        int cnt = 0;
        do {
            ++cnt;
            e = e.next;
            if (e == null) {
                return -cnt;
            }
        } while (e != _head);
        return cnt;
    }
    
    public static boolean checkCyclicListIntegrity(Entry e) {
        if (e == null) {
            return true;
        }
        final Entry _head = e;
        while (e.next != null) {
            if (e.next.prev != e) {
                return false;
            }
            e = e.next;
            if (e == _head) {
                return true;
            }
        }
        return false;
    }
    
    static {
        STATE_UPDATER = AtomicLongFieldUpdater.newUpdater(Entry.class, "refreshTimeAndState");
        LIST_REMOVED_MARKER = new Entry();
    }
    
    public static class ProcessingState
    {
        public static final int DONE = 0;
        public static final int READ = 1;
        public static final int READ_COMPLETE = 2;
        public static final int MUTATE = 3;
        public static final int LOAD = 4;
        public static final int LOAD_COMPLETE = 5;
        public static final int COMPUTE = 6;
        public static final int REFRESH = 7;
        public static final int EXPIRY = 8;
        public static final int EXPIRY_COMPLETE = 9;
        public static final int WRITE = 10;
        public static final int WRITE_COMPLETE = 11;
        public static final int STORE = 12;
        public static final int STORE_COMPLETE = 13;
        public static final int NOTIFY = 14;
        public static final int PINNED = 15;
        public static final int EVICT = 16;
        public static final int LOAD_ASYNC = 17;
        public static final int WRITE_ASYNC = 18;
        public static final int LAST = 19;
    }
    
    static class PiggyBack
    {
        final PiggyBack next;
        
        public PiggyBack(final PiggyBack _next) {
            this.next = _next;
        }
    }
    
    static class TaskPiggyBack extends PiggyBack
    {
        SimpleTimerTask task;
        
        public TaskPiggyBack(final SimpleTimerTask _task, final PiggyBack _next) {
            super(_next);
            this.task = _task;
        }
    }
    
    static class LoadExceptionPiggyBack extends PiggyBack
    {
        ExceptionInformation info;
        
        public LoadExceptionPiggyBack(final ExceptionInformation _info, final PiggyBack _next) {
            super(_next);
            this.info = _info;
        }
    }
    
    static class RefreshProbationPiggyBack extends PiggyBack
    {
        long nextRefreshTime;
        
        public RefreshProbationPiggyBack(final long _nextRefreshTime, final PiggyBack _next) {
            super(_next);
            this.nextRefreshTime = _nextRefreshTime;
        }
    }
}
