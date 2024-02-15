//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.core;

import java.io.*;
import java.util.concurrent.*;

public class ExclusiveExecutor implements Executor, Closeable
{
    private ThreadPoolExecutor threadPoolExecutor;
    
    public ExclusiveExecutor(final int _threadCount, final String _threadNamePrefix) {
        final int _corePoolThreadSize = 0;
        this.threadPoolExecutor = new ThreadPoolExecutor(0, _threadCount, 21L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), HeapCache.TUNABLE.threadFactoryProvider.newThreadFactory(_threadNamePrefix), new ThreadPoolExecutor.AbortPolicy());
    }
    
    @Override
    public void execute(final Runnable cmd) {
        this.threadPoolExecutor.execute(cmd);
    }
    
    public ThreadPoolExecutor getThreadPoolExecutor() {
        return this.threadPoolExecutor;
    }
    
    @Override
    public void close() {
        this.threadPoolExecutor.shutdown();
    }
}
