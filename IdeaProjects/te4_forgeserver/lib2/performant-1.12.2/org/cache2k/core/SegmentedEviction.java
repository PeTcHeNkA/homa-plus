//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.core;

import org.cache2k.core.concurrency.*;

public class SegmentedEviction implements Eviction, EvictionMetrics
{
    private Eviction[] segments;
    
    public SegmentedEviction(final Eviction[] _segments) {
        this.segments = _segments;
    }
    
    public void updateWeight(final Entry e) {
        final int hc = e.hashCode;
        final Eviction[] sgs = this.segments;
        final int _mask = sgs.length - 1;
        final int idx = hc & _mask;
        sgs[idx].updateWeight(e);
    }
    
    public boolean submitWithoutEviction(final Entry e) {
        final int hc = e.hashCode;
        final Eviction[] sgs = this.segments;
        final int _mask = sgs.length - 1;
        final int idx = hc & _mask;
        return sgs[idx].submitWithoutEviction(e);
    }
    
    public void evictEventually(final int _hashCodeHint) {
        final Eviction[] sgs = this.segments;
        final int _mask = sgs.length - 1;
        final int idx = _hashCodeHint & _mask;
        sgs[idx].evictEventually(_hashCodeHint);
    }
    
    public void evictEventually() {
        for (final Eviction ev : this.segments) {
            ev.evictEventually();
        }
    }
    
    public long removeAll() {
        long _count = 0L;
        for (final Eviction ev : this.segments) {
            _count += ev.removeAll();
        }
        return _count;
    }
    
    public void start() {
        for (final Eviction ev : this.segments) {
            ev.start();
        }
    }
    
    public void stop() {
        for (final Eviction ev : this.segments) {
            ev.stop();
        }
    }
    
    public void close() {
        for (final Eviction ev : this.segments) {
            ev.close();
        }
    }
    
    public boolean drain() {
        boolean f = false;
        for (final Eviction ev : this.segments) {
            f |= ev.drain();
        }
        return f;
    }
    
    public <T> T runLocked(final Job<T> j) {
        return this.runLocked(0, j);
    }
    
    private <T> T runLocked(final int idx, final Job<T> j) {
        if (idx == this.segments.length) {
            return (T)j.call();
        }
        return (T)this.segments[idx].runLocked((Job)new Job<T>() {
            public T call() {
                return (T)SegmentedEviction.this.runLocked(idx + 1, (org.cache2k.core.concurrency.Job<Object>)j);
            }
        });
    }
    
    public void checkIntegrity(final IntegrityState _integrityState) {
        for (int i = 0; i < this.segments.length; ++i) {
            _integrityState.group("eviction" + i);
            this.segments[i].checkIntegrity(_integrityState);
        }
    }
    
    public EvictionMetrics getMetrics() {
        return (EvictionMetrics)this;
    }
    
    public String getExtraStatistics() {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < this.segments.length; ++i) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append("eviction").append(i).append('(');
            sb.append(this.segments[i].getMetrics().getExtraStatistics());
            sb.append(')');
        }
        return sb.toString();
    }
    
    public long getHitCount() {
        long sum = 0L;
        for (final Eviction ev : this.segments) {
            sum += ev.getMetrics().getHitCount();
        }
        return sum;
    }
    
    public long getNewEntryCount() {
        long sum = 0L;
        for (final Eviction ev : this.segments) {
            sum += ev.getMetrics().getNewEntryCount();
        }
        return sum;
    }
    
    public long getRemovedCount() {
        long sum = 0L;
        for (final Eviction ev : this.segments) {
            sum += ev.getMetrics().getRemovedCount();
        }
        return sum;
    }
    
    public long getExpiredRemovedCount() {
        long sum = 0L;
        for (final Eviction ev : this.segments) {
            sum += ev.getMetrics().getExpiredRemovedCount();
        }
        return sum;
    }
    
    public long getVirginRemovedCount() {
        long sum = 0L;
        for (final Eviction ev : this.segments) {
            sum += ev.getMetrics().getVirginRemovedCount();
        }
        return sum;
    }
    
    public long getEvictedCount() {
        long sum = 0L;
        for (final Eviction ev : this.segments) {
            sum += ev.getMetrics().getEvictedCount();
        }
        return sum;
    }
    
    public long getSize() {
        long sum = 0L;
        for (final Eviction ev : this.segments) {
            sum += ev.getMetrics().getSize();
        }
        return sum;
    }
    
    public long getMaxSize() {
        long sum = 0L;
        for (final Eviction ev : this.segments) {
            final long l = ev.getMetrics().getMaxSize();
            if (l == Long.MAX_VALUE) {
                return Long.MAX_VALUE;
            }
            sum += l;
        }
        if (sum < 0L) {
            return -1L;
        }
        return sum;
    }
    
    public long getMaxWeight() {
        long sum = 0L;
        for (final Eviction ev : this.segments) {
            final long l = ev.getMetrics().getMaxWeight();
            if (l == Long.MAX_VALUE) {
                return Long.MAX_VALUE;
            }
            sum += l;
        }
        if (sum < 0L) {
            return -1L;
        }
        return sum;
    }
    
    public long getCurrentWeight() {
        long sum = 0L;
        for (final Eviction ev : this.segments) {
            final long l = ev.getMetrics().getCurrentWeight();
            sum += l;
        }
        return sum;
    }
    
    public int getEvictionRunningCount() {
        int sum = 0;
        for (final Eviction ev : this.segments) {
            sum += ev.getMetrics().getEvictionRunningCount();
        }
        return sum;
    }
    
    public boolean isWeigherPresent() {
        return this.segments[0].isWeigherPresent();
    }
}
