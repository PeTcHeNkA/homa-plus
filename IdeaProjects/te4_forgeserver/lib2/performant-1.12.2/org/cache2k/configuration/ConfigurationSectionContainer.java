//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.configuration;

import java.io.*;
import java.util.*;

public class ConfigurationSectionContainer extends AbstractCollection<ConfigurationSection> implements Collection<ConfigurationSection>, Serializable
{
    private Collection<ConfigurationSection> sections;
    
    public ConfigurationSectionContainer() {
        this.sections = new ArrayList<ConfigurationSection>();
    }
    
    @Override
    public boolean add(final ConfigurationSection section) {
        if (section instanceof SingletonConfigurationSection && this.getSection(section.getClass()) != null) {
            throw new IllegalArgumentException("Section of same type already inserted: " + section.getClass().getName());
        }
        return this.sections.add(section);
    }
    
    public <T extends ConfigurationSection> T getSection(final Class<T> sectionType) {
        for (final ConfigurationSection s : this.sections) {
            if (sectionType.equals(s.getClass())) {
                return (T)s;
            }
        }
        return null;
    }
    
    @Override
    public Iterator<ConfigurationSection> iterator() {
        return this.sections.iterator();
    }
    
    @Override
    public int size() {
        return this.sections.size();
    }
    
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + this.sections.toString();
    }
}
