//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.impl.xmlConfiguration;

import org.cache2k.configuration.*;
import java.util.*;

public class ConfigurationContext
{
    private final Cache2kManagerConfiguration managerConfiguration;
    private boolean configurationPresent;
    private ClassLoader classLoader;
    private Cache2kConfiguration<?, ?> defaultManagerConfiguration;
    private Map<String, String> predefinedSectionTypes;
    private ParsedConfiguration templates;
    
    public ConfigurationContext() {
        this.managerConfiguration = new Cache2kManagerConfiguration();
        this.configurationPresent = false;
    }
    
    public Cache2kConfiguration<?, ?> getDefaultManagerConfiguration() {
        return this.defaultManagerConfiguration;
    }
    
    public void setDefaultManagerConfiguration(final Cache2kConfiguration<?, ?> _defaultManagerConfiguration) {
        this.defaultManagerConfiguration = _defaultManagerConfiguration;
    }
    
    public boolean isConfigurationPresent() {
        return this.configurationPresent;
    }
    
    public void setConfigurationPresent(final boolean _configurationPresent) {
        this.configurationPresent = _configurationPresent;
    }
    
    public ClassLoader getClassLoader() {
        return this.classLoader;
    }
    
    public void setClassLoader(final ClassLoader _classLoader) {
        this.classLoader = _classLoader;
    }
    
    public Map<String, String> getPredefinedSectionTypes() {
        return this.predefinedSectionTypes;
    }
    
    public void setPredefinedSectionTypes(final Map<String, String> v) {
        this.predefinedSectionTypes = v;
    }
    
    public ParsedConfiguration getTemplates() {
        return this.templates;
    }
    
    public void setTemplates(final ParsedConfiguration _templates) {
        this.templates = _templates;
    }
    
    public Cache2kManagerConfiguration getManagerConfiguration() {
        return this.managerConfiguration;
    }
}
