//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.core;

import org.cache2k.*;
import org.cache2k.core.util.*;

public class ClockProPlusEviction extends AbstractEviction
{
    public static final Tunable TUNABLE_CLOCK_PRO;
    private long hotHits;
    private long coldHits;
    private long ghostHits;
    private long hotRunCnt;
    private long hotScanCnt;
    private long coldRunCnt;
    private long coldScanCnt;
    private int coldSize;
    private int hotSize;
    private Entry handCold;
    private Entry handHot;
    private Ghost[] ghosts;
    private Ghost ghostHead;
    private int ghostSize;
    private static final int GHOST_LOAD_PERCENT = 63;
    
    public ClockProPlusEviction(final HeapCache heapCache, final HeapCacheListener listener, final long maxSize, final Weigher weigher, final long maxWeight, final boolean noChunking) {
        super(heapCache, listener, maxSize, weigher, maxWeight, noChunking);
        this.ghostHead = new Ghost().shortCircuit();
        this.ghostSize = 0;
        this.coldSize = 0;
        this.hotSize = 0;
        this.handCold = null;
        this.handHot = null;
        this.ghosts = new Ghost[4];
    }
    
    private long sumUpListHits(Entry e) {
        if (e == null) {
            return 0L;
        }
        long cnt = 0L;
        final Entry head = e;
        do {
            cnt += e.hitCnt;
            e = e.next;
        } while (e != head);
        return cnt;
    }
    
    public long getHotMax() {
        return this.isWeigherPresent() ? (this.getSize() * ClockProPlusEviction.TUNABLE_CLOCK_PRO.hotMaxPercentage / 100L) : (this.getMaxSize() * ClockProPlusEviction.TUNABLE_CLOCK_PRO.hotMaxPercentage / 100L);
    }
    
    public long getGhostMax() {
        return this.getSize() / 2L + 1L;
    }
    
    public long getHitCount() {
        return this.hotHits + this.coldHits + this.sumUpListHits(this.handCold) + this.sumUpListHits(this.handHot);
    }
    
    public long removeAll() {
        int count = 0;
        Entry e;
        Entry head = e = this.handCold;
        long hits = 0L;
        if (e != null) {
            do {
                hits += e.hitCnt;
                final Entry next = e.prev;
                e.removedFromList();
                ++count;
                e = next;
            } while (e != head);
            this.coldHits += hits;
        }
        this.handCold = null;
        this.coldSize = 0;
        head = (e = this.handHot);
        if (e != null) {
            hits = 0L;
            do {
                hits += e.hitCnt;
                final Entry next = e.prev;
                e.removedFromList();
                ++count;
                e = next;
            } while (e != head);
            this.hotHits += hits;
        }
        this.handHot = null;
        this.hotSize = 0;
        return count;
    }
    
    public void removeFromReplacementListOnEvict(final Entry e) {
        this.insertCopyIntoGhosts(e);
        this.removeFromReplacementList(e);
    }
    
    protected void removeFromReplacementList(final Entry e) {
        if (e.isHot()) {
            this.hotHits += e.hitCnt;
            this.handHot = Entry.removeFromCyclicList(this.handHot, e);
            --this.hotSize;
        }
        else {
            this.coldHits += e.hitCnt;
            this.handCold = Entry.removeFromCyclicList(this.handCold, e);
            --this.coldSize;
        }
    }
    
    private void insertCopyIntoGhosts(final Entry e) {
        final int hc = e.hashCode;
        Ghost g = this.lookupGhost(hc);
        if (g != null) {
            Ghost.moveToFront(this.ghostHead, g);
            return;
        }
        if (this.ghostSize >= this.getGhostMax()) {
            g = this.ghostHead.prev;
            Ghost.removeFromList(g);
            this.removeGhost(g, g.hash);
        }
        else {
            g = new Ghost();
        }
        this.insertGhost(g, g.hash = hc);
        Ghost.insertInList(this.ghostHead, g);
    }
    
    public long getSize() {
        return this.hotSize + this.coldSize;
    }
    
    protected void insertIntoReplacementList(final Entry e) {
        final Ghost g = this.lookupGhost(e.hashCode);
        if (g != null) {
            ++this.ghostHits;
            e.setHot(true);
            ++this.hotSize;
            this.handHot = Entry.insertIntoTailCyclicList(this.handHot, e);
            return;
        }
        ++this.coldSize;
        this.handCold = Entry.insertIntoTailCyclicList(this.handCold, e);
    }
    
    private Entry runHandHot() {
        ++this.hotRunCnt;
        Entry coldCandidate;
        Entry hand = coldCandidate = this.handHot;
        long lowestHits = Long.MAX_VALUE;
        long hotHits = this.hotHits;
        int maxScan;
        final int initialMaxScan = maxScan = this.hotSize >> 3;
        final long decrease = (hand.hitCnt + hand.next.hitCnt >> ClockProPlusEviction.TUNABLE_CLOCK_PRO.hitCounterDecreaseShift) + 1L;
        while (maxScan-- > 0) {
            final long hitCnt = hand.hitCnt;
            if (hitCnt < lowestHits) {
                lowestHits = hitCnt;
                coldCandidate = hand;
                if (hitCnt == 0L) {
                    break;
                }
            }
            if (hitCnt < decrease) {
                hand.hitCnt = 0L;
                hotHits += hitCnt;
            }
            else {
                hand.hitCnt = hitCnt - decrease;
                hotHits += decrease;
            }
            hand = hand.next;
        }
        this.hotHits = hotHits;
        final long scanCount = initialMaxScan - maxScan;
        this.hotScanCnt += scanCount;
        this.handHot = hand;
        return coldCandidate;
    }
    
    protected Entry findEvictionCandidate(final Entry previous) {
        Entry hand = this.handCold;
        if (this.hotSize > this.getHotMax() || hand == null) {
            return this.runHandHot();
        }
        ++this.coldRunCnt;
        int scanCnt = 1;
        if (hand.hitCnt > 0L) {
            Entry evictFromHot = null;
            do {
                if (this.hotSize >= this.getHotMax() && this.handHot != null) {
                    evictFromHot = this.runHandHot();
                }
                this.coldHits += hand.hitCnt;
                hand.hitCnt = 0L;
                final Entry e = hand;
                hand = Entry.removeFromCyclicList(e);
                --this.coldSize;
                e.setHot(true);
                ++this.hotSize;
                this.handHot = Entry.insertIntoTailCyclicList(this.handHot, e);
                if (evictFromHot != null) {
                    this.coldScanCnt += scanCnt;
                    this.handCold = hand;
                    return evictFromHot;
                }
                ++scanCnt;
            } while (hand != null && hand.hitCnt > 0L);
        }
        this.coldScanCnt += scanCnt;
        if (hand == null) {
            this.handCold = null;
            return this.runHandHot();
        }
        this.handCold = hand.next;
        return hand;
    }
    
    public void checkIntegrity(final IntegrityState is) {
        is.checkEquals("ghostSize == countGhostsInHash()", this.ghostSize, this.countGhostsInHash()).check("isWeigherPresent() || hotMax <= size", this.isWeigherPresent() || this.getHotMax() <= this.getMaxSize()).check("checkCyclicListIntegrity(handHot)", Entry.checkCyclicListIntegrity(this.handHot)).check("checkCyclicListIntegrity(handCold)", Entry.checkCyclicListIntegrity(this.handCold)).checkEquals("getCyclicListEntryCount(handHot) == hotSize", Entry.getCyclicListEntryCount(this.handHot), this.hotSize).checkEquals("getCyclicListEntryCount(handCold) == coldSize", Entry.getCyclicListEntryCount(this.handCold), this.coldSize).checkEquals("Ghost.listSize(ghostHead) == ghostSize", Ghost.listSize(this.ghostHead), this.ghostSize);
    }
    
    public String getExtraStatistics() {
        return super.getExtraStatistics() + ", coldSize=" + this.coldSize + ", hotSize=" + this.hotSize + ", hotMaxSize=" + this.getHotMax() + ", ghostSize=" + this.ghostSize + ", coldHits=" + (this.coldHits + this.sumUpListHits(this.handCold)) + ", hotHits=" + (this.hotHits + this.sumUpListHits(this.handHot)) + ", ghostHits=" + this.ghostHits + ", coldRunCnt=" + this.coldRunCnt + ", coldScanCnt=" + this.coldScanCnt + ", hotRunCnt=" + this.hotRunCnt + ", hotScanCnt=" + this.hotScanCnt;
    }
    
    private Ghost lookupGhost(final int hash) {
        final Ghost[] tab = this.ghosts;
        final int n = tab.length;
        final int mask = n - 1;
        final int idx = hash & mask;
        for (Ghost e = tab[idx]; e != null; e = e.another) {
            if (e.hash == hash) {
                return e;
            }
        }
        return null;
    }
    
    private void insertGhost(final Ghost e2, final int hash) {
        final Ghost[] tab = this.ghosts;
        final int n = tab.length;
        final int mask = n - 1;
        final int idx = hash & mask;
        e2.another = tab[idx];
        tab[idx] = e2;
        ++this.ghostSize;
        final int maxFill = n * 63 / 100;
        if (this.ghostSize > maxFill) {
            this.expand();
        }
    }
    
    private void expand() {
        final Ghost[] tab = this.ghosts;
        final int n = tab.length;
        final Ghost[] newTab = new Ghost[n * 2];
        final int mask = newTab.length - 1;
        for (Ghost g : tab) {
            while (g != null) {
                final int idx = g.hash & mask;
                final Ghost next = g.another;
                g.another = newTab[idx];
                newTab[idx] = g;
                g = next;
            }
        }
        this.ghosts = newTab;
    }
    
    private boolean removeGhost(final Ghost g, final int hash) {
        final Ghost[] tab = this.ghosts;
        final int n = tab.length;
        final int mask = n - 1;
        final int idx = hash & mask;
        Ghost e = tab[idx];
        if (e == g) {
            tab[idx] = e.another;
            --this.ghostSize;
            return true;
        }
        while (e != null) {
            final Ghost another = e.another;
            if (another == g) {
                e.another = another.another;
                --this.ghostSize;
                return true;
            }
            e = another;
        }
        return false;
    }
    
    private int countGhostsInHash() {
        int entryCount = 0;
        for (Ghost e : this.ghosts) {
            while (e != null) {
                ++entryCount;
                e = e.another;
            }
        }
        return entryCount;
    }
    
    static {
        TUNABLE_CLOCK_PRO = TunableFactory.get(Tunable.class);
    }
    
    public static class Tunable extends TunableConstants
    {
        int hotMaxPercentage;
        int hitCounterDecreaseShift;
        
        public Tunable() {
            this.hotMaxPercentage = 97;
            this.hitCounterDecreaseShift = 6;
        }
    }
    
    private static class Ghost
    {
        int hash;
        Ghost another;
        Ghost next;
        Ghost prev;
        
        Ghost shortCircuit() {
            this.prev = this;
            return this.next = this;
        }
        
        static void removeFromList(final Ghost e) {
            e.prev.next = e.next;
            e.next.prev = e.prev;
            final Ghost ghost = null;
            e.prev = ghost;
            e.next = ghost;
        }
        
        static void insertInList(final Ghost head, final Ghost e) {
            e.prev = head;
            e.next = head.next;
            e.next.prev = e;
            head.next = e;
        }
        
        static void moveToFront(final Ghost head, final Ghost e) {
            removeFromList(e);
            insertInList(head, e);
        }
        
        static int listSize(final Ghost head) {
            int count = 0;
            Ghost e = head;
            while ((e = e.next) != head) {
                ++count;
            }
            return count;
        }
    }
}
