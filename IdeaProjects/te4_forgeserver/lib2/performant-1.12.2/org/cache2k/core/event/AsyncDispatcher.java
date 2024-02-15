//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.core.event;

import org.cache2k.core.*;
import java.util.concurrent.*;
import java.util.*;

public class AsyncDispatcher<K>
{
    private static final int KEY_LOCKS_MASK;
    private static Object[] KEY_LOCKS;
    private Map<K, Queue<AsyncEvent<K>>> keyQueue;
    private Executor executor;
    private InternalCache cache;
    
    private static Object getLockObject(final Object key) {
        final int hc = key.hashCode();
        return AsyncDispatcher.KEY_LOCKS[hc & AsyncDispatcher.KEY_LOCKS_MASK];
    }
    
    public AsyncDispatcher(final InternalCache _cache, final Executor _executor) {
        this.keyQueue = new ConcurrentHashMap<K, Queue<AsyncEvent<K>>>();
        this.cache = _cache;
        this.executor = _executor;
    }
    
    public void queue(final AsyncEvent<K> _event) {
        final K key = _event.getKey();
        synchronized (getLockObject(key)) {
            Queue<AsyncEvent<K>> q = this.keyQueue.get(key);
            if (q != null) {
                q.add(_event);
                return;
            }
            q = new LinkedList<AsyncEvent<K>>();
            this.keyQueue.put(key, q);
        }
        final Runnable r = new Runnable() {
            @Override
            public void run() {
                AsyncDispatcher.this.runMoreOrStop(_event);
            }
        };
        this.executor.execute(r);
    }
    
    public void runMoreOrStop(AsyncEvent<K> _event) {
        while (true) {
            try {
                _event.execute();
            }
            catch (Throwable t) {
                this.cache.getLog().warn("Async event exception", t);
            }
            final K key = _event.getKey();
            synchronized (getLockObject(key)) {
                final Queue<AsyncEvent<K>> q = this.keyQueue.get(key);
                if (q.isEmpty()) {
                    this.keyQueue.remove(key);
                    return;
                }
                _event = q.remove();
            }
        }
    }
    
    static {
        KEY_LOCKS_MASK = 2 << 31 - Integer.numberOfLeadingZeros(Runtime.getRuntime().availableProcessors()) - 1;
        AsyncDispatcher.KEY_LOCKS = new Object[AsyncDispatcher.KEY_LOCKS_MASK + 1];
        for (int i = 0; i < AsyncDispatcher.KEY_LOCKS.length; ++i) {
            AsyncDispatcher.KEY_LOCKS[i] = new Object();
        }
    }
}
