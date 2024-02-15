//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.configuration;

public class Cache2kManagerConfiguration implements ConfigurationBean
{
    private String version;
    private String defaultManagerName;
    private boolean ignoreMissingCacheConfiguration;
    private boolean skipCheckOnStartup;
    private boolean ignoreAnonymousCache;
    
    public Cache2kManagerConfiguration() {
        this.version = null;
        this.defaultManagerName = null;
        this.ignoreMissingCacheConfiguration = false;
        this.skipCheckOnStartup = false;
        this.ignoreAnonymousCache = false;
    }
    
    public boolean isIgnoreMissingCacheConfiguration() {
        return this.ignoreMissingCacheConfiguration;
    }
    
    public void setIgnoreMissingCacheConfiguration(final boolean f) {
        this.ignoreMissingCacheConfiguration = f;
    }
    
    public String getDefaultManagerName() {
        return this.defaultManagerName;
    }
    
    public void setDefaultManagerName(final String v) {
        this.defaultManagerName = v;
    }
    
    public String getVersion() {
        return this.version;
    }
    
    public void setVersion(final String v) {
        this.version = v;
    }
    
    public boolean isSkipCheckOnStartup() {
        return this.skipCheckOnStartup;
    }
    
    public void setSkipCheckOnStartup(final boolean f) {
        this.skipCheckOnStartup = f;
    }
    
    public boolean isIgnoreAnonymousCache() {
        return this.ignoreAnonymousCache;
    }
    
    public void setIgnoreAnonymousCache(final boolean f) {
        this.ignoreAnonymousCache = f;
    }
}
