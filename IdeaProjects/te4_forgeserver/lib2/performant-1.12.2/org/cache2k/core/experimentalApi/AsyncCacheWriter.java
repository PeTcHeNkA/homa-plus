//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.core.experimentalApi;

import java.util.concurrent.*;
import java.util.*;

public interface AsyncCacheWriter<K, V>
{
    void write(final K p0, final V p1, final Callback p2, final Executor p3);
    
    void remove(final K p0, final Callback p1, final Executor p2);
    
    public interface Callback extends EventListener
    {
        void onWriteSuccess();
        
        void onWriteFailure(final Throwable p0);
    }
}
