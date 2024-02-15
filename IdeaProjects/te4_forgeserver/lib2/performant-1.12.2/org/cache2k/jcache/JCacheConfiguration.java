//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.jcache;

import org.cache2k.configuration.*;

public class JCacheConfiguration implements SingletonConfigurationSection
{
    private boolean copyAlwaysIfRequested;
    private boolean supportOnlineListenerAttachment;
    private boolean enableStatistics;
    private boolean enableManagement;
    private boolean enableReadThrough;
    
    public JCacheConfiguration() {
        this.copyAlwaysIfRequested = false;
        this.supportOnlineListenerAttachment = false;
        this.enableStatistics = false;
        this.enableManagement = false;
        this.enableReadThrough = false;
    }
    
    public boolean isCopyAlwaysIfRequested() {
        return this.copyAlwaysIfRequested;
    }
    
    public void setCopyAlwaysIfRequested(final boolean f) {
        this.copyAlwaysIfRequested = f;
    }
    
    public boolean isSupportOnlineListenerAttachment() {
        return this.supportOnlineListenerAttachment;
    }
    
    public void setSupportOnlineListenerAttachment(final boolean f) {
        this.supportOnlineListenerAttachment = f;
    }
    
    public boolean isEnableStatistics() {
        return this.enableStatistics;
    }
    
    public void setEnableStatistics(final boolean f) {
        this.enableStatistics = f;
    }
    
    public boolean isEnableManagement() {
        return this.enableManagement;
    }
    
    public void setEnableManagement(final boolean f) {
        this.enableManagement = f;
    }
    
    public boolean isEnableReadThrough() {
        return this.enableReadThrough;
    }
    
    public void setEnableReadThrough(final boolean f) {
        this.enableReadThrough = f;
    }
    
    public static class Builder implements ConfigurationSectionBuilder<JCacheConfiguration>
    {
        private JCacheConfiguration config;
        
        public Builder() {
            this.config = new JCacheConfiguration();
        }
        
        public Builder copyAlwaysIfRequested(final boolean f) {
            this.config.setCopyAlwaysIfRequested(f);
            return this;
        }
        
        public Builder supportOnlineListenerAttachment(final boolean f) {
            this.config.setSupportOnlineListenerAttachment(f);
            return this;
        }
        
        public Builder enableManagement(final boolean f) {
            this.config.setEnableManagement(f);
            return this;
        }
        
        public Builder enableStatistics(final boolean f) {
            this.config.setEnableStatistics(f);
            return this;
        }
        
        public Builder enableReadThrough(final boolean f) {
            this.config.setEnableReadThrough(f);
            return this;
        }
        
        public JCacheConfiguration buildConfigurationSection() {
            return this.config;
        }
    }
}
