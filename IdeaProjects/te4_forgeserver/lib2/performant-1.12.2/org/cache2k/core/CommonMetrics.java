//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.core;

public interface CommonMetrics
{
    long getPutNewEntryCount();
    
    long getPutHitCount();
    
    long getPutNoReadHitCount();
    
    long getHeapHitButNoReadCount();
    
    long getTimerEventCount();
    
    long getLoadCount();
    
    long getReloadCount();
    
    long getRefreshCount();
    
    long getLoadMillis();
    
    long getLoadExceptionCount();
    
    long getSuppressedExceptionCount();
    
    long getExpiredKeptCount();
    
    long getPeekMissCount();
    
    long getPeekHitNotFreshCount();
    
    long getRefreshedHitCount();
    
    long getRefreshFailedCount();
    
    long getGoneSpinCount();
    
    boolean isDisabled();
    
    public static class BlackHole implements Updater
    {
        @Override
        public void putNewEntry() {
        }
        
        @Override
        public void putNewEntry(final long cnt) {
        }
        
        @Override
        public void putHit() {
        }
        
        @Override
        public void putHit(final long cnt) {
        }
        
        @Override
        public void putNoReadHit() {
        }
        
        @Override
        public void putNoReadHit(final long cnt) {
        }
        
        @Override
        public void heapHitButNoRead() {
        }
        
        @Override
        public void heapHitButNoRead(final long cnt) {
        }
        
        @Override
        public void timerEvent() {
        }
        
        @Override
        public void timerEvent(final long cnt) {
        }
        
        @Override
        public void load(final long _millis) {
        }
        
        @Override
        public void load(final long cnt, final long _millis) {
        }
        
        @Override
        public void reload(final long _millis) {
        }
        
        @Override
        public void reload(final long cnt, final long _millis) {
        }
        
        @Override
        public void refresh(final long _millis) {
        }
        
        @Override
        public void refresh(final long cnt, final long _millis) {
        }
        
        @Override
        public void loadException() {
        }
        
        @Override
        public void loadException(final long cnt) {
        }
        
        @Override
        public void suppressedException() {
        }
        
        @Override
        public void suppressedException(final long cnt) {
        }
        
        @Override
        public void expiredKept() {
        }
        
        @Override
        public void expiredKept(final long cnt) {
        }
        
        @Override
        public void peekMiss() {
        }
        
        @Override
        public void peekMiss(final long cnt) {
        }
        
        @Override
        public void peekHitNotFresh() {
        }
        
        @Override
        public void peekHitNotFresh(final long cnt) {
        }
        
        @Override
        public void refreshedHit() {
        }
        
        @Override
        public void refreshedHit(final long cnt) {
        }
        
        @Override
        public void refreshFailed() {
        }
        
        @Override
        public void refreshFailed(final long cnt) {
        }
        
        @Override
        public void goneSpin() {
        }
        
        @Override
        public void goneSpin(final long cnt) {
        }
        
        @Override
        public long getPutNewEntryCount() {
            return 0L;
        }
        
        @Override
        public long getPutHitCount() {
            return 0L;
        }
        
        @Override
        public long getPutNoReadHitCount() {
            return 0L;
        }
        
        @Override
        public long getHeapHitButNoReadCount() {
            return 0L;
        }
        
        @Override
        public long getTimerEventCount() {
            return 0L;
        }
        
        @Override
        public long getLoadCount() {
            return 0L;
        }
        
        @Override
        public long getReloadCount() {
            return 0L;
        }
        
        @Override
        public long getRefreshCount() {
            return 0L;
        }
        
        @Override
        public long getLoadMillis() {
            return 0L;
        }
        
        @Override
        public long getLoadExceptionCount() {
            return 0L;
        }
        
        @Override
        public long getSuppressedExceptionCount() {
            return 0L;
        }
        
        @Override
        public long getExpiredKeptCount() {
            return 0L;
        }
        
        @Override
        public long getPeekMissCount() {
            return 0L;
        }
        
        @Override
        public long getPeekHitNotFreshCount() {
            return 0L;
        }
        
        @Override
        public long getRefreshedHitCount() {
            return 0L;
        }
        
        @Override
        public long getRefreshFailedCount() {
            return 0L;
        }
        
        @Override
        public long getGoneSpinCount() {
            return 0L;
        }
        
        @Override
        public boolean isDisabled() {
            return true;
        }
    }
    
    public interface Updater extends CommonMetrics
    {
        void putNewEntry();
        
        void putNewEntry(final long p0);
        
        void putHit();
        
        void putHit(final long p0);
        
        void putNoReadHit();
        
        void putNoReadHit(final long p0);
        
        void heapHitButNoRead();
        
        void heapHitButNoRead(final long p0);
        
        void timerEvent();
        
        void timerEvent(final long p0);
        
        void load(final long p0);
        
        void load(final long p0, final long p1);
        
        void reload(final long p0);
        
        void reload(final long p0, final long p1);
        
        void refresh(final long p0);
        
        void refresh(final long p0, final long p1);
        
        void loadException();
        
        void loadException(final long p0);
        
        void suppressedException();
        
        void suppressedException(final long p0);
        
        void expiredKept();
        
        void expiredKept(final long p0);
        
        void peekMiss();
        
        void peekMiss(final long p0);
        
        void peekHitNotFresh();
        
        void peekHitNotFresh(final long p0);
        
        void refreshedHit();
        
        void refreshedHit(final long p0);
        
        void refreshFailed();
        
        void refreshFailed(final long p0);
        
        void goneSpin();
        
        void goneSpin(final long p0);
    }
}
