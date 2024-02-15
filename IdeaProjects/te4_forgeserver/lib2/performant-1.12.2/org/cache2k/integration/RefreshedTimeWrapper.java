//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.integration;

public final class RefreshedTimeWrapper<V> extends LoadDetail<V>
{
    private final long refreshTime;
    
    public RefreshedTimeWrapper(final Object value, final long refreshTime) {
        super(value);
        this.refreshTime = refreshTime;
    }
    
    public long getRefreshTime() {
        return this.refreshTime;
    }
}
