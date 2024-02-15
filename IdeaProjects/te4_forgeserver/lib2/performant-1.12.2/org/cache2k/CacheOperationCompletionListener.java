//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k;

import java.util.*;

public interface CacheOperationCompletionListener extends EventListener
{
    void onCompleted();
    
    void onException(final Throwable p0);
}
