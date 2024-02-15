//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.core.operation;

import org.cache2k.integration.*;

public interface Progress<K, V, R>
{
    long getCurrentTime();
    
    void wantData();
    
    boolean isPresent();
    
    boolean isPresentOrInRefreshProbation();
    
    boolean isPresentOrMiss();
    
    void wantMutation();
    
    void result(final R p0);
    
    void entryResult(final ExaminationEntry p0);
    
    RuntimeException propagateException(final K p0, final ExceptionInformation p1);
    
    void load();
    
    void refresh();
    
    void loadAndMutation();
    
    void expire(final long p0);
    
    void remove();
    
    void put(final V p0);
    
    void failure(final RuntimeException p0);
    
    void putAndSetExpiry(final V p0, final long p1, final long p2);
}
