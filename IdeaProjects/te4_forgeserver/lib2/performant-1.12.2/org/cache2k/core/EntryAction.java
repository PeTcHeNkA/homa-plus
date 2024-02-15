//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.core;

import org.cache2k.core.experimentalApi.*;
import org.cache2k.event.*;
import org.cache2k.core.operation.*;
import org.cache2k.integration.*;
import org.cache2k.*;
import org.cache2k.integration.AsyncCacheLoader;


public abstract class EntryAction<K, V, R> implements AsyncCacheLoader.Context<K, V>, AsyncCacheLoader.Callback<K, V>, AsyncCacheWriter.Callback, Progress<K, V, R> {
    static final Entry NON_FRESH_DUMMY;
    InternalCache<K, V> userCache;
    HeapCache<K, V> heapCache;
    K key;
    Semantic<K, V, R> operation;
    Entry<K, V> entry;
    V newValueOrException;
    V oldValueOrException;
    R result;
    long currentTime;
    long lastRefreshTime;
    long loadStartedTime;
    RuntimeException exceptionToPropagate;
    boolean remove;
    boolean expiredImmediately;
    long expiry;
    boolean entryLocked;
    boolean heapDataValid;
    boolean storageDataValid;
    boolean needsFinish;
    boolean storageRead;
    boolean storageMiss;
    boolean heapMiss;
    boolean wantData;
    boolean countMiss;
    boolean heapHit;
    boolean doNotCountAccess;
    boolean loadAndMutate;
    boolean load;
    boolean refresh;
    boolean successfulLoad;
    boolean suppressException;
    Thread syncThread;
    ActionCompletedCallback completedCallback;
    
    public EntryAction(final HeapCache<K, V> _heapCache, final InternalCache<K, V> _userCache, final Semantic<K, V, R> op, final K k, final Entry<K, V> e, final ActionCompletedCallback cb) {
        this.expiry = 0L;
        this.entryLocked = false;
        this.heapDataValid = false;
        this.storageDataValid = false;
        this.needsFinish = true;
        this.storageRead = false;
        this.storageMiss = false;
        this.heapMiss = false;
        this.wantData = false;
        this.countMiss = false;
        this.heapHit = false;
        this.doNotCountAccess = false;
        this.loadAndMutate = false;
        this.load = false;
        this.refresh = false;
        this.successfulLoad = false;
        this.suppressException = false;
        this.heapCache = _heapCache;
        this.userCache = _userCache;
        this.operation = op;
        this.key = k;
        if (e != null) {
            this.entry = e;
        }
        else {
            this.entry = (org.cache2k.core.Entry<K, V>)EntryAction.NON_FRESH_DUMMY;
        }
        if (cb == null) {
            this.syncThread = Thread.currentThread();
        }
        else {
            this.completedCallback = cb;
        }
    }
    
    public EntryAction(final HeapCache<K, V> _heapCache, final InternalCache<K, V> _userCache, final Semantic<K, V, R> op, final K k, final Entry<K, V> e) {
        this(_heapCache, _userCache, op, k, e, null);
    }
    
    protected AdvancedCacheLoader<K, V> loader() {
        return this.heapCache.loader;
    }
    
    protected AsyncCacheLoader<K, V> asyncLoader() {
        return null;
    }
    
    protected CommonMetrics.Updater metrics() {
        return this.heapCache.metrics;
    }
    
    protected CacheWriter<K, V> writer() {
        return null;
    }
    
    protected boolean mightHaveListeners() {
        return false;
    }
    
    protected CacheEntryCreatedListener<K, V>[] entryCreatedListeners() {
        return null;
    }
    
    protected CacheEntryUpdatedListener<K, V>[] entryUpdatedListeners() {
        return null;
    }
    
    protected CacheEntryRemovedListener<K, V>[] entryRemovedListeners() {
        return null;
    }
    
    protected CacheEntryExpiredListener<K, V>[] entryExpiredListeners() {
        return null;
    }
    
    protected abstract TimingHandler<K, V> timing();
    
    @Override
    public K getKey() {
        return (K)this.entry.getKey();
    }
    
    @Override
    public long getCurrentTime() {
        if (this.currentTime > 0L) {
            return this.currentTime;
        }
        return this.currentTime = this.millis();
    }
    
    @Override
    public V getCachedValue() {
        final V v = (V)this.entry.getValueOrException();
        if (v instanceof ExceptionWrapper || this.entry.isVirgin()) {
            return null;
        }
        return v;
    }
    
    @Override
    public Throwable getCachedException() {
        final V v = (V)this.entry.getValueOrException();
        if (v instanceof ExceptionWrapper) {
            return ((ExceptionWrapper)v).getException();
        }
        return null;
    }
    
    @Override
    public boolean isPresent() {
        this.doNotCountAccess = true;
        return this.successfulLoad || this.entry.hasFreshData(this.heapCache.getClock());
    }
    
    @Override
    public boolean isPresentOrInRefreshProbation() {
        this.doNotCountAccess = true;
        return this.successfulLoad || this.entry.getNextRefreshTime() == 6L || this.entry.hasFreshData(this.heapCache.getClock());
    }
    
    @Override
    public boolean isPresentOrMiss() {
        if (this.successfulLoad || this.entry.hasFreshData(this.heapCache.getClock())) {
            return true;
        }
        this.countMiss = true;
        return false;
    }
    
    @Override
    public void wantData() {
        this.wantData = true;
        this.retrieveDataFromHeap();
    }
    
    public void retrieveDataFromHeap() {
        Entry<K, V> e = this.entry;
        if (e == EntryAction.NON_FRESH_DUMMY) {
            e = this.heapCache.lookupEntry(this.key);
            if (e == null) {
                this.heapMiss();
                return;
            }
        }
        this.heapHit(e);
    }
    
    private long millis() {
        return this.heapCache.getClock().millis();
    }
    
    public void heapMiss() {
        this.heapMiss = true;
        this.examine();
    }
    
    public void heapHit(final Entry<K, V> e) {
        this.heapHit = true;
        this.entry = e;
        this.examine();
    }
    
    public void examine() {
        this.operation.examine(this, (ExaminationEntry<K, V>)this.entry);
        if (this.needsFinish) {
            this.finish();
        }
    }
    
    @Override
    public void wantMutation() {
        if (!this.entryLocked && this.wantData) {
            this.lockFor(3);
            this.countMiss = false;
            this.operation.examine(this, (ExaminationEntry<K, V>)this.entry);
            if (this.needsFinish) {
                this.finish();
            }
            return;
        }
        this.operation.update(this, (ExaminationEntry<K, V>)this.entry);
        if (this.needsFinish) {
            this.finish();
        }
    }
    
    public void finish() {
        this.needsFinish = false;
        this.noMutationRequested();
    }
    
    @Override
    public void loadAndMutation() {
        this.loadAndMutate = true;
        this.load();
    }
    
    @Override
    public void refresh() {
        this.refresh = true;
        this.load();
    }
    
    @Override
    public void load() {
        this.lockFor(4);
        this.needsFinish = false;
        this.load = true;
        final Entry<K, V> e = this.entry;
        long currentTime;
        if (this.heapCache.isUpdateTimeNeeded()) {
            final long n = currentTime = this.getCurrentTime();
            this.loadStartedTime = n;
            this.lastRefreshTime = n;
        }
        else {
            currentTime = 0L;
        }
        final long t0 = currentTime;
        if (e.getNextRefreshTime() == 6L) {
            final long nrt = e.getRefreshProbationNextRefreshTime();
            if (nrt > t0) {
                this.reviveRefreshedEntry(nrt);
                return;
            }
        }
        final AsyncCacheLoader<K, V> _asyncLoader;
        if ((_asyncLoader = this.asyncLoader()) != null) {
            this.lockFor(17);
            try {
                _asyncLoader.load(this.key, this, this);
            }
            catch (Exception ex) {
                this.onLoadFailure(ex);
                this.exceptionToPropagate = (RuntimeException)new CacheLoaderException(ex);
                return;
            }
            this.asyncOperationStarted();
            return;
        }
        final AdvancedCacheLoader<K, V> _loader = this.loader();
        if (_loader == null) {
            this.mutationAbort(null);
            return;
        }
        V v;
        try {
            if (e.isVirgin()) {
                v = _loader.load(this.key, t0, null);
            }
            else {
                v = _loader.load(this.key, t0, (org.cache2k.CacheEntry<K, V>)e);
            }
        }
        catch (Throwable _ouch) {
            this.onLoadFailureIntern(_ouch);
            return;
        }
        this.onLoadSuccessIntern(v);
    }
    
    public void reviveRefreshedEntry(final long nrt) {
        this.metrics().refreshedHit();
        final Entry<K, V> e = this.entry;
        this.newValueOrException = (V)e.getValueOrException();
        this.lastRefreshTime = e.getRefreshTime();
        this.expiry = nrt;
        this.expiryCalculated();
    }
    
    void lockFor(final int ps) {
        if (this.entryLocked) {
            this.entry.nextProcessingStep(ps);
            return;
        }
        Entry<K, V> e = this.entry;
        if (e == EntryAction.NON_FRESH_DUMMY) {
            e = this.heapCache.lookupOrNewEntry(this.key);
        }
        while (true) {
            synchronized (e) {
                e.waitForProcessing();
                if (!e.isGone()) {
                    e.startProcessing(ps);
                    this.entryLocked = true;
                    this.heapDataValid = e.isDataValid();
                    this.heapHit = !e.isVirgin();
                    this.entry = e;
                    return;
                }
            }
            e = this.heapCache.lookupOrNewEntry(this.key);
        }
    }
    
    void lockForNoHit(final int ps) {
        if (this.entryLocked) {
            this.entry.nextProcessingStep(ps);
            return;
        }
        Entry<K, V> e = this.entry;
        if (e == EntryAction.NON_FRESH_DUMMY) {
            e = this.heapCache.lookupOrNewEntryNoHitRecord(this.key);
        }
        while (true) {
            synchronized (e) {
                e.waitForProcessing();
                if (!e.isGone()) {
                    e.startProcessing(ps);
                    this.entryLocked = true;
                    this.heapDataValid = e.isDataValid();
                    this.heapHit = !e.isVirgin();
                    this.entry = e;
                    return;
                }
            }
            e = this.heapCache.lookupOrNewEntryNoHitRecord(this.key);
        }
    }
    
    @Override
    public void onLoadSuccess(final V v) {
        this.checkEntryStateOnLoadCallback();
        this.onLoadSuccessIntern(v);
    }
    
    @Override
    public void onLoadFailure(final Throwable t) {
        this.checkEntryStateOnLoadCallback();
        this.onLoadFailureIntern(t);
    }
    
    private void checkEntryStateOnLoadCallback() {
        if (!this.entry.checkAndSwitchProcessingState(17, 5)) {
            throw new IllegalStateException("async callback on wrong entry state. duplicate callback?");
        }
    }
    
    private void onLoadSuccessIntern(V v) {
        if (v instanceof RefreshedTimeWrapper) {
            final RefreshedTimeWrapper wr = (RefreshedTimeWrapper)v;
            this.lastRefreshTime = wr.getRefreshTime();
            v = (V)wr.getValue();
        }
        this.newValueOrException = v;
        this.loadCompleted();
    }
    
    private void onLoadFailureIntern(final Throwable t) {
        this.newValueOrException = (V)new ExceptionWrapper((K)this.key, t, this.loadStartedTime, this.entry);
        this.loadCompleted();
    }
    
    public void loadCompleted() {
        this.entry.nextProcessingStep(5);
        this.entryLocked = true;
        if (!this.metrics().isDisabled() && this.heapCache.isUpdateTimeNeeded()) {
            final long _loadCompletedTime = this.millis();
            final long _delta = _loadCompletedTime - this.loadStartedTime;
            if (this.refresh) {
                this.metrics().refresh(_delta);
            }
            else if (this.entry.isVirgin() || !this.storageRead) {
                this.metrics().load(_delta);
            }
            else {
                this.metrics().reload(_delta);
            }
        }
        this.mutationCalculateExpiry();
    }
    
    @Override
    public void result(final R r) {
        this.result = r;
    }
    
    @Override
    public void entryResult(final ExaminationEntry e) {
        this.result = (R)this.heapCache.returnEntry(e);
    }
    
    @Override
    public RuntimeException propagateException(final K key, final ExceptionInformation inf) {
        return this.heapCache.exceptionPropagator.propagateException(key, inf);
    }
    
    @Override
    public void put(final V value) {
        this.lockFor(3);
        this.needsFinish = false;
        this.newValueOrException = value;
        if (!this.heapCache.isUpdateTimeNeeded()) {
            this.lastRefreshTime = 0L;
        }
        else {
            this.lastRefreshTime = this.getCurrentTime();
        }
        this.mutationCalculateExpiry();
    }
    
    @Override
    public void remove() {
        this.lockForNoHit(3);
        this.needsFinish = false;
        this.remove = true;
        this.mutationMayCallWriter();
    }
    
    @Override
    public void expire(final long expiryTime) {
        this.lockForNoHit(3);
        this.needsFinish = false;
        this.newValueOrException = (V)this.entry.getValueOrException();
        if (this.heapCache.isUpdateTimeNeeded()) {
            this.lastRefreshTime = this.entry.getRefreshTime();
        }
        this.expiry = expiryTime;
        if (this.newValueOrException instanceof ExceptionWrapper) {
            this.setUntil(ExceptionWrapper.class.cast(this.newValueOrException));
        }
        this.checkKeepOrRemove();
    }
    
    @Override
    public void putAndSetExpiry(final V value, final long expiryTime, final long refreshTime) {
        this.lockFor(3);
        this.needsFinish = false;
        this.newValueOrException = value;
        if (refreshTime >= 0L) {
            this.lastRefreshTime = refreshTime;
        }
        else if (this.heapCache.isUpdateTimeNeeded()) {
            this.lastRefreshTime = this.getCurrentTime();
        }
        if (this.newValueOrException instanceof ExceptionWrapper) {
            this.setUntil(ExceptionWrapper.class.cast(this.newValueOrException));
        }
        if (expiryTime != -1L) {
            this.expiry = expiryTime;
            this.expiryCalculated();
        }
        else {
            this.mutationCalculateExpiry();
        }
    }
    
    public void mutationCalculateExpiry() {
        this.entry.nextProcessingStep(8);
        Label_0324: {
            if (this.newValueOrException instanceof ExceptionWrapper) {
                try {
                    this.expiry = 0L;
                    final ExceptionWrapper ew = (ExceptionWrapper)this.newValueOrException;
                    if ((this.entry.isDataValid() || this.entry.isExpired()) && this.entry.getException() == null) {
                        this.expiry = this.timing().suppressExceptionUntil(this.entry, ew);
                    }
                    if (this.expiry > this.loadStartedTime) {
                        this.suppressException = true;
                        this.newValueOrException = (V)this.entry.getValueOrException();
                        this.lastRefreshTime = this.entry.getRefreshTime();
                        this.metrics().suppressedException();
                        this.entry.setSuppressedLoadExceptionInformation((ExceptionInformation)ew);
                    }
                    else {
                        if (this.load) {
                            this.metrics().loadException();
                        }
                        this.expiry = this.timing().cacheExceptionUntil(this.entry, ew);
                    }
                    this.setUntil(ew);
                    break Label_0324;
                }
                catch (Throwable ex) {
                    if (this.load) {
                        this.resiliencePolicyException((RuntimeException)new ResiliencePolicyException(ex));
                        return;
                    }
                    this.expiryCalculationException(ex);
                    return;
                }
            }
            try {
                this.expiry = this.timing().calculateNextRefreshTime(this.entry, this.newValueOrException, this.lastRefreshTime);
                if (this.newValueOrException == null && this.heapCache.isRejectNullValues() && this.expiry != 0L) {
                    final RuntimeException _ouch = this.heapCache.returnNullValueDetectedException();
                    if (this.load) {
                        this.decideForLoaderExceptionAfterExpiryCalculation((RuntimeException)new ResiliencePolicyException(_ouch));
                        return;
                    }
                    this.mutationAbort(_ouch);
                    return;
                }
                else {
                    this.entry.resetSuppressedLoadExceptionInformation();
                }
            }
            catch (Throwable ex) {
                if (this.load) {
                    this.decideForLoaderExceptionAfterExpiryCalculation((RuntimeException)new ExpiryPolicyException(ex));
                    return;
                }
                this.expiryCalculationException(ex);
                return;
            }
        }
        this.expiryCalculated();
    }
    
    private void decideForLoaderExceptionAfterExpiryCalculation(final RuntimeException _ouch) {
        this.newValueOrException = (V)new ExceptionWrapper((K)this.key, _ouch, this.loadStartedTime, this.entry);
        this.expiry = 0L;
        this.mutationCalculateExpiry();
    }
    
    private void resiliencePolicyException(final RuntimeException _ouch) {
        this.newValueOrException = (V)new ExceptionWrapper((K)this.key, _ouch, this.loadStartedTime, this.entry);
        this.expiry = 0L;
        this.expiryCalculated();
    }
    
    private void setUntil(final ExceptionWrapper _ew) {
        if (this.expiry < 0L) {
            _ew.setUntil(-this.expiry);
        }
        else if (this.expiry >= 32L) {
            _ew.setUntil(this.expiry);
        }
    }
    
    public void expiryCalculationException(final Throwable t) {
        this.mutationAbort((RuntimeException)new ExpiryPolicyException(t));
    }
    
    public void expiryCalculated() {
        this.entry.nextProcessingStep(9);
        if (!this.load) {
            if (this.expiry > 0L || this.expiry == -1L || (this.expiry < 0L && -this.expiry > this.loadStartedTime)) {
                if (this.entry.isVirgin()) {
                    this.metrics().putNewEntry();
                }
                else if (!this.wantData) {
                    this.metrics().putNoReadHit();
                }
                else {
                    this.metrics().putHit();
                }
            }
            this.mutationMayCallWriter();
            return;
        }
        if (this.loadAndMutate) {
            this.loadAndExpiryCalculatedMutateAgain();
            return;
        }
        this.checkKeepOrRemove();
    }
    
    public void loadAndExpiryCalculatedMutateAgain() {
        final boolean b = false;
        this.loadAndMutate = b;
        this.load = b;
        final boolean b2 = true;
        this.successfulLoad = b2;
        this.needsFinish = b2;
        final ExaminationEntry<K, V> ee = new LoadedEntry<K, V>() {
            @Override
            public K getKey() {
                return (K)EntryAction.this.entry.getKey();
            }
            
            @Override
            public V getValueOrException() {
                return EntryAction.this.newValueOrException;
            }
            
            @Override
            public long getRefreshTime() {
                return EntryAction.this.lastRefreshTime;
            }
        };
        this.operation.update(this, ee);
        if (this.needsFinish) {
            this.updateDidNotTriggerDifferentMutationStoreLoadedValue();
        }
    }
    
    public void updateDidNotTriggerDifferentMutationStoreLoadedValue() {
        this.checkKeepOrRemove();
    }
    
    public void mutationMayCallWriter() {
        final CacheWriter<K, V> _writer = this.writer();
        if (_writer == null) {
            this.skipWritingNoWriter();
            return;
        }
        if (this.remove) {
            try {
                this.entry.nextProcessingStep(10);
                _writer.delete(this.key);
            }
            catch (Throwable t) {
                this.onWriteFailure(t);
                return;
            }
            this.onWriteSuccess();
            return;
        }
        if (this.newValueOrException instanceof ExceptionWrapper) {
            this.skipWritingForException();
            return;
        }
        this.entry.nextProcessingStep(10);
        try {
            _writer.write(this.key, this.newValueOrException);
        }
        catch (Throwable t) {
            this.onWriteFailure(t);
            return;
        }
        this.onWriteSuccess();
    }
    
    @Override
    public void onWriteSuccess() {
        this.entry.nextProcessingStep(11);
        this.checkKeepOrRemove();
    }
    
    @Override
    public void onWriteFailure(final Throwable t) {
        this.mutationAbort((RuntimeException)new CacheWriterException(t));
    }
    
    public void skipWritingForException() {
        this.checkKeepOrRemove();
    }
    
    public void skipWritingNoWriter() {
        this.checkKeepOrRemove();
    }
    
    public void checkKeepOrRemove() {
        final boolean _hasKeepAfterExpired = this.heapCache.isKeepAfterExpired();
        if (this.expiry != 0L || this.remove || _hasKeepAfterExpired) {
            this.mutationUpdateHeap();
            return;
        }
        if (_hasKeepAfterExpired) {
            this.expiredImmediatelyKeepData();
            return;
        }
        this.expiredImmediatelyAndRemove();
    }
    
    public void expiredImmediatelyKeepData() {
        this.expiredImmediately = true;
        this.mutationUpdateHeap();
    }
    
    public void expiredImmediatelyAndRemove() {
        this.remove = true;
        this.expiredImmediately = true;
        this.mutationUpdateHeap();
    }
    
    public void mutationUpdateHeap() {
        synchronized (this.entry) {
            if (this.heapCache.isRecordRefreshTime()) {
                this.entry.setRefreshTime(this.lastRefreshTime);
            }
            if (this.remove) {
                if (this.expiredImmediately) {
                    this.entry.setNextRefreshTime(4L);
                    this.entry.setValueOrException((V) this.newValueOrException);
                }
                else if (!this.entry.isVirgin()) {
                    this.entry.setNextRefreshTime(2L);
                }
            }
            else {
                this.oldValueOrException = (V)this.entry.getValueOrException();
                this.entry.setValueOrException((V) this.newValueOrException);
            }
        }
        this.heapCache.eviction.updateWeight(this.entry);
        this.mutationMayStore();
    }
    
    public void mutationMayStore() {
        this.skipStore();
    }
    
    public void skipStore() {
        this.callListeners();
    }
    
    public void callListeners() {
        if (!this.mightHaveListeners()) {
            this.mutationReleaseLockAndStartTimer();
            return;
        }
        final CacheEntry<K, V> _currentEntry = this.heapCache.returnCacheEntry((ExaminationEntry<K, V>)this.entry);
        if (this.expiredImmediately) {
            if ((this.storageDataValid || this.heapDataValid) && this.entryExpiredListeners() != null) {
                for (final CacheEntryExpiredListener<K, V> l : this.entryExpiredListeners()) {
                    try {
                        l.onEntryExpired((org.cache2k.Cache<K, V>)this.userCache, _currentEntry);
                    }
                    catch (Throwable t) {
                        this.exceptionToPropagate = (RuntimeException)new ListenerException(t);
                    }
                }
            }
        }
        else if (this.remove) {
            if ((this.storageDataValid || this.heapDataValid) && this.entryRemovedListeners() != null) {
                for (final CacheEntryRemovedListener<K, V> i : this.entryRemovedListeners()) {
                    try {
                        i.onEntryRemoved((org.cache2k.Cache<K, V>)this.userCache, _currentEntry);
                    }
                    catch (Throwable t) {
                        this.exceptionToPropagate = (RuntimeException)new ListenerException(t);
                    }
                }
            }
        }
        else if (this.storageDataValid || this.heapDataValid) {
            if (this.entryUpdatedListeners() != null) {
                final CacheEntry<K, V> _previousEntry = this.heapCache.returnCacheEntry((K)this.entry.getKey(), this.oldValueOrException);
                for (final CacheEntryUpdatedListener<K, V> j : this.entryUpdatedListeners()) {
                    try {
                        j.onEntryUpdated((org.cache2k.Cache<K, V>)this.userCache, _previousEntry, _currentEntry);
                    }
                    catch (Throwable t2) {
                        this.exceptionToPropagate = (RuntimeException)new ListenerException(t2);
                    }
                }
            }
        }
        else if (this.entryCreatedListeners() != null) {
            for (final CacheEntryCreatedListener<K, V> k : this.entryCreatedListeners()) {
                try {
                    k.onEntryCreated((org.cache2k.Cache<K, V>)this.userCache, _currentEntry);
                }
                catch (Throwable t) {
                    this.exceptionToPropagate = (RuntimeException)new ListenerException(t);
                }
            }
        }
        this.mutationReleaseLockAndStartTimer();
    }
    
    public void mutationReleaseLockAndStartTimer() {
        if (this.load && (!this.remove || this.entry.getValueOrException() != null || !this.heapCache.isRejectNullValues())) {
            this.operation.loaded(this, (ExaminationEntry<K, V>)this.entry);
        }
        synchronized (this.entry) {
            this.entry.processingDone();
            this.entryLocked = false;
            if (this.refresh) {
                this.heapCache.startRefreshProbationTimer(this.entry, this.expiry);
                this.updateMutationStatistics();
                this.mutationDone();
                return;
            }
            if (this.remove) {
                this.heapCache.removeEntry(this.entry);
            }
            else {
                this.entry.setNextRefreshTime(this.timing().stopStartTimer(this.expiry, this.entry));
                if (this.entry.isExpired()) {
                    this.entry.setNextRefreshTime(32L);
                    this.userCache.expireOrScheduleFinalExpireEvent(this.entry);
                }
            }
        }
        this.updateMutationStatistics();
        this.mutationDone();
    }
    
    public void updateMutationStatistics() {
        if (this.loadStartedTime > 0L) {
            return;
        }
        this.updateOnlyReadStatistics();
    }
    
    public void updateOnlyReadStatistics() {
        if (this.countMiss) {
            if (this.heapHit) {
                this.metrics().peekHitNotFresh();
            }
            if (this.heapMiss) {
                this.metrics().peekMiss();
            }
        }
        else if (this.doNotCountAccess && this.heapHit) {
            this.metrics().heapHitButNoRead();
        }
    }
    
    @Override
    public void failure(final RuntimeException t) {
        this.updateOnlyReadStatistics();
        this.mutationAbort(t);
    }
    
    public void examinationAbort(final CustomizationException t) {
        this.exceptionToPropagate = (RuntimeException)t;
        if (this.entryLocked) {
            synchronized (this.entry) {
                this.entry.processingDone();
                this.entryLocked = false;
            }
        }
        this.ready();
    }
    
    public void mutationAbort(final RuntimeException t) {
        this.exceptionToPropagate = t;
        synchronized (this.entry) {
            this.entry.processingDone();
            this.entryLocked = false;
            this.needsFinish = false;
        }
        this.ready();
    }
    
    public void mutationDone() {
        this.ready();
    }
    
    public void noMutationRequested() {
        if (this.entryLocked) {
            synchronized (this.entry) {
                this.entry.processingDone();
                if (this.entry.isVirgin()) {
                    this.heapCache.removeEntry(this.entry);
                }
            }
            this.entryLocked = false;
        }
        this.updateOnlyReadStatistics();
        this.ready();
    }
    
    public void ready() {
        if (this.completedCallback != null) {
            this.completedCallback.entryActionCompleted(this);
        }
    }
    
    public void asyncOperationStarted() {
        if (this.syncThread == Thread.currentThread()) {
            synchronized (this.entry) {
                while (this.entry.isProcessing()) {
                    try {
                        this.entry.wait();
                    }
                    catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
        else {
            this.entryLocked = false;
        }
    }
    
    static {
        NON_FRESH_DUMMY = new Entry();
    }
    
    public static class StorageReadException extends CustomizationException
    {
        public StorageReadException(final Throwable cause) {
            super(cause);
        }
    }
    
    public static class StorageWriteException extends CustomizationException
    {
        public StorageWriteException(final Throwable cause) {
            super(cause);
        }
    }
    
    public static class ProcessingFailureException extends CustomizationException
    {
        public ProcessingFailureException(final Throwable cause) {
            super(cause);
        }
    }
    
    public static class ListenerException extends CustomizationException
    {
        public ListenerException(final Throwable cause) {
            super(cause);
        }
    }
    
    public interface ActionCompletedCallback
    {
        void entryActionCompleted(final EntryAction p0);
    }
}
