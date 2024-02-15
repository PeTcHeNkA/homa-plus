//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.core.spi;

import org.cache2k.*;
import org.cache2k.configuration.*;

public interface CacheLifeCycleListener
{
    void cacheCreated(final Cache p0, final Cache2kConfiguration p1);
    
    void cacheDestroyed(final Cache p0);
}
