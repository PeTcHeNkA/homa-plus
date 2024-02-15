//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.core;

import java.util.concurrent.atomic.*;

public class StandardCommonMetrics implements CommonMetrics.Updater
{
    static final AtomicLongFieldUpdater<StandardCommonMetrics> PUT_NEW_ENTRY_UPDATER;
    private volatile long putNewEntry;
    static final AtomicLongFieldUpdater<StandardCommonMetrics> putHitUpdater;
    private volatile long putHit;
    static final AtomicLongFieldUpdater<StandardCommonMetrics> putNoReadHitUpdater;
    private volatile long putNoReadHit;
    static final AtomicLongFieldUpdater<StandardCommonMetrics> heapHitButNoReadUpdater;
    private volatile long heapHitButNoRead;
    static final AtomicLongFieldUpdater<StandardCommonMetrics> timerEventUpdater;
    private volatile long timerEvent;
    static final AtomicLongFieldUpdater<StandardCommonMetrics> loadMillisUpdater;
    private volatile long loadMillis;
    static final AtomicLongFieldUpdater<StandardCommonMetrics> refreshUpdater;
    private volatile long refresh;
    static final AtomicLongFieldUpdater<StandardCommonMetrics> loadUpdater;
    private volatile long load;
    static final AtomicLongFieldUpdater<StandardCommonMetrics> reloadUpdater;
    private volatile long reload;
    static final AtomicLongFieldUpdater<StandardCommonMetrics> loadExceptionUpdater;
    private volatile long loadException;
    static final AtomicLongFieldUpdater<StandardCommonMetrics> suppressedExceptionUpdater;
    private volatile long suppressedException;
    static final AtomicLongFieldUpdater<StandardCommonMetrics> expiredKeptUpdater;
    private volatile long expiredKept;
    static final AtomicLongFieldUpdater<StandardCommonMetrics> peekMissUpdater;
    private volatile long peekMiss;
    static final AtomicLongFieldUpdater<StandardCommonMetrics> peekHitNotFreshUpdater;
    private volatile long peekHitNotFresh;
    static final AtomicLongFieldUpdater<StandardCommonMetrics> refreshHitUpdater;
    private volatile long refreshHit;
    static final AtomicLongFieldUpdater<StandardCommonMetrics> refreshSubmitFailedUpdater;
    private volatile long refreshSubmitFailed;
    static final AtomicLongFieldUpdater<StandardCommonMetrics> goneSpinUpdater;
    private volatile long goneSpin;
    
    public void putNewEntry() {
        StandardCommonMetrics.PUT_NEW_ENTRY_UPDATER.incrementAndGet(this);
    }
    
    public long getPutNewEntryCount() {
        return StandardCommonMetrics.PUT_NEW_ENTRY_UPDATER.get(this);
    }
    
    public void putNewEntry(final long cnt) {
        StandardCommonMetrics.PUT_NEW_ENTRY_UPDATER.addAndGet(this, cnt);
    }
    
    public void putHit() {
        StandardCommonMetrics.putHitUpdater.incrementAndGet(this);
    }
    
    public long getPutHitCount() {
        return StandardCommonMetrics.putHitUpdater.get(this);
    }
    
    public void putHit(final long cnt) {
        StandardCommonMetrics.putHitUpdater.addAndGet(this, cnt);
    }
    
    public void putNoReadHit() {
        StandardCommonMetrics.putNoReadHitUpdater.incrementAndGet(this);
    }
    
    public long getPutNoReadHitCount() {
        return StandardCommonMetrics.putNoReadHitUpdater.get(this);
    }
    
    public void putNoReadHit(final long cnt) {
        StandardCommonMetrics.putNoReadHitUpdater.addAndGet(this, cnt);
    }
    
    public void heapHitButNoRead() {
        StandardCommonMetrics.heapHitButNoReadUpdater.incrementAndGet(this);
    }
    
    public long getHeapHitButNoReadCount() {
        return StandardCommonMetrics.heapHitButNoReadUpdater.get(this);
    }
    
    public void heapHitButNoRead(final long cnt) {
        StandardCommonMetrics.heapHitButNoReadUpdater.addAndGet(this, cnt);
    }
    
    public void timerEvent() {
        StandardCommonMetrics.timerEventUpdater.incrementAndGet(this);
    }
    
    public long getTimerEventCount() {
        return StandardCommonMetrics.timerEventUpdater.get(this);
    }
    
    public void timerEvent(final long cnt) {
        StandardCommonMetrics.timerEventUpdater.addAndGet(this, cnt);
    }
    
    public long getLoadMillis() {
        return StandardCommonMetrics.loadMillisUpdater.get(this);
    }
    
    public long getRefreshCount() {
        return StandardCommonMetrics.refreshUpdater.get(this);
    }
    
    public void refresh(final long _millis) {
        StandardCommonMetrics.refreshUpdater.incrementAndGet(this);
        StandardCommonMetrics.loadMillisUpdater.addAndGet(this, _millis);
    }
    
    public void refresh(final long cnt, final long _millis) {
        StandardCommonMetrics.refreshUpdater.addAndGet(this, cnt);
        StandardCommonMetrics.loadMillisUpdater.addAndGet(this, _millis);
    }
    
    public long getLoadCount() {
        return StandardCommonMetrics.loadUpdater.get(this);
    }
    
    public void load(final long _millis) {
        StandardCommonMetrics.loadUpdater.incrementAndGet(this);
        StandardCommonMetrics.loadMillisUpdater.addAndGet(this, _millis);
    }
    
    public void load(final long cnt, final long _millis) {
        StandardCommonMetrics.loadUpdater.addAndGet(this, cnt);
        StandardCommonMetrics.loadMillisUpdater.addAndGet(this, _millis);
    }
    
    public long getReloadCount() {
        return StandardCommonMetrics.reloadUpdater.get(this);
    }
    
    public void reload(final long _millis) {
        StandardCommonMetrics.reloadUpdater.incrementAndGet(this);
        StandardCommonMetrics.loadMillisUpdater.addAndGet(this, _millis);
    }
    
    public void reload(final long cnt, final long _millis) {
        StandardCommonMetrics.reloadUpdater.addAndGet(this, cnt);
        StandardCommonMetrics.loadMillisUpdater.addAndGet(this, _millis);
    }
    
    public long getLoadExceptionCount() {
        return StandardCommonMetrics.loadExceptionUpdater.get(this);
    }
    
    public void loadException() {
        StandardCommonMetrics.loadExceptionUpdater.incrementAndGet(this);
    }
    
    public void loadException(final long cnt) {
        StandardCommonMetrics.loadExceptionUpdater.addAndGet(this, cnt);
    }
    
    public long getSuppressedExceptionCount() {
        return StandardCommonMetrics.suppressedExceptionUpdater.get(this);
    }
    
    public void suppressedException() {
        StandardCommonMetrics.suppressedExceptionUpdater.incrementAndGet(this);
    }
    
    public void suppressedException(final long cnt) {
        StandardCommonMetrics.suppressedExceptionUpdater.addAndGet(this, cnt);
    }
    
    public long getExpiredKeptCount() {
        return StandardCommonMetrics.expiredKeptUpdater.get(this);
    }
    
    public void expiredKept() {
        StandardCommonMetrics.expiredKeptUpdater.incrementAndGet(this);
    }
    
    public void expiredKept(final long cnt) {
        StandardCommonMetrics.expiredKeptUpdater.addAndGet(this, cnt);
    }
    
    public long getPeekMissCount() {
        return StandardCommonMetrics.peekMissUpdater.get(this);
    }
    
    public void peekMiss() {
        StandardCommonMetrics.peekMissUpdater.incrementAndGet(this);
    }
    
    public void peekMiss(final long cnt) {
        StandardCommonMetrics.peekMissUpdater.addAndGet(this, cnt);
    }
    
    public long getPeekHitNotFreshCount() {
        return StandardCommonMetrics.peekHitNotFreshUpdater.get(this);
    }
    
    public void peekHitNotFresh() {
        StandardCommonMetrics.peekHitNotFreshUpdater.incrementAndGet(this);
    }
    
    public void peekHitNotFresh(final long cnt) {
        StandardCommonMetrics.peekHitNotFreshUpdater.addAndGet(this, cnt);
    }
    
    public long getRefreshedHitCount() {
        return StandardCommonMetrics.refreshHitUpdater.get(this);
    }
    
    public void refreshedHit() {
        StandardCommonMetrics.refreshHitUpdater.incrementAndGet(this);
    }
    
    public void refreshedHit(final long cnt) {
        StandardCommonMetrics.refreshHitUpdater.addAndGet(this, cnt);
    }
    
    public long getRefreshFailedCount() {
        return StandardCommonMetrics.refreshSubmitFailedUpdater.get(this);
    }
    
    public void refreshFailed() {
        StandardCommonMetrics.refreshSubmitFailedUpdater.incrementAndGet(this);
    }
    
    public void refreshFailed(final long cnt) {
        StandardCommonMetrics.refreshSubmitFailedUpdater.addAndGet(this, cnt);
    }
    
    public boolean isDisabled() {
        return false;
    }
    
    public long getGoneSpinCount() {
        return StandardCommonMetrics.goneSpinUpdater.get(this);
    }
    
    public void goneSpin() {
        StandardCommonMetrics.goneSpinUpdater.incrementAndGet(this);
    }
    
    public void goneSpin(final long cnt) {
        StandardCommonMetrics.goneSpinUpdater.addAndGet(this, cnt);
    }
    
    static {
        PUT_NEW_ENTRY_UPDATER = AtomicLongFieldUpdater.newUpdater(StandardCommonMetrics.class, "putNewEntry");
        putHitUpdater = AtomicLongFieldUpdater.newUpdater(StandardCommonMetrics.class, "putHit");
        putNoReadHitUpdater = AtomicLongFieldUpdater.newUpdater(StandardCommonMetrics.class, "putNoReadHit");
        heapHitButNoReadUpdater = AtomicLongFieldUpdater.newUpdater(StandardCommonMetrics.class, "heapHitButNoRead");
        timerEventUpdater = AtomicLongFieldUpdater.newUpdater(StandardCommonMetrics.class, "timerEvent");
        loadMillisUpdater = AtomicLongFieldUpdater.newUpdater(StandardCommonMetrics.class, "loadMillis");
        refreshUpdater = AtomicLongFieldUpdater.newUpdater(StandardCommonMetrics.class, "refresh");
        loadUpdater = AtomicLongFieldUpdater.newUpdater(StandardCommonMetrics.class, "load");
        reloadUpdater = AtomicLongFieldUpdater.newUpdater(StandardCommonMetrics.class, "reload");
        loadExceptionUpdater = AtomicLongFieldUpdater.newUpdater(StandardCommonMetrics.class, "loadException");
        suppressedExceptionUpdater = AtomicLongFieldUpdater.newUpdater(StandardCommonMetrics.class, "suppressedException");
        expiredKeptUpdater = AtomicLongFieldUpdater.newUpdater(StandardCommonMetrics.class, "expiredKept");
        peekMissUpdater = AtomicLongFieldUpdater.newUpdater(StandardCommonMetrics.class, "peekMiss");
        peekHitNotFreshUpdater = AtomicLongFieldUpdater.newUpdater(StandardCommonMetrics.class, "peekHitNotFresh");
        refreshHitUpdater = AtomicLongFieldUpdater.newUpdater(StandardCommonMetrics.class, "refreshHit");
        refreshSubmitFailedUpdater = AtomicLongFieldUpdater.newUpdater(StandardCommonMetrics.class, "refreshSubmitFailed");
        goneSpinUpdater = AtomicLongFieldUpdater.newUpdater(StandardCommonMetrics.class, "goneSpin");
    }
}
