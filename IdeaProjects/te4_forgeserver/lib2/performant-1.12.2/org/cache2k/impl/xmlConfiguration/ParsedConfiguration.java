//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.impl.xmlConfiguration;

import java.util.*;

public class ParsedConfiguration implements SourceLocation
{
    private String source;
    private int lineNumber;
    private String name;
    private String type;
    private String container;
    private String propertyContext;
    private Map<String, ConfigurationTokenizer.Property> properties;
    private List<ParsedConfiguration> sections;
    
    public ParsedConfiguration(final String _source, final int _lineNumber) {
        this.properties = new HashMap<String, ConfigurationTokenizer.Property>();
        this.sections = new ArrayList<ParsedConfiguration>();
        this.lineNumber = _lineNumber;
        this.source = _source;
    }
    
    @Override
    public String getSource() {
        return this.source;
    }
    
    @Override
    public int getLineNumber() {
        return this.lineNumber;
    }
    
    public String getContainer() {
        return this.container;
    }
    
    public void setContainer(final String _container) {
        this.container = _container;
    }
    
    public String getName() {
        return this.name;
    }
    
    public void setName(final String _name) {
        this.name = _name;
    }
    
    public String getType() {
        return this.type;
    }
    
    public void setType(final String _type) {
        this.type = _type;
    }
    
    public String getPropertyContext() {
        return this.propertyContext;
    }
    
    public void setPropertyContext(final String _propertyContext) {
        this.propertyContext = _propertyContext;
    }
    
    public Map<String, ConfigurationTokenizer.Property> getPropertyMap() {
        return this.properties;
    }
    
    public List<ParsedConfiguration> getSections() {
        return this.sections;
    }
    
    public void addProperty(final ConfigurationTokenizer.Property p) {
        if ("name".equals(p.getName())) {
            this.name = p.getValue();
        }
        if ("type".equals(p.getName())) {
            this.type = p.getValue();
        }
        this.properties.put(p.getName(), p);
    }
    
    public void addSection(final ParsedConfiguration c) {
        this.sections.add(c);
    }
    
    public ParsedConfiguration getSection(final String _name) {
        for (final ParsedConfiguration c : this.sections) {
            if (_name.equals(c.getName())) {
                return c;
            }
        }
        return null;
    }
    
    public String getStringPropertyByPath(final String s) {
        final ConfigurationTokenizer.Property p = this.getPropertyByPath(s);
        if (p == null) {
            return null;
        }
        return p.getValue();
    }
    
    public ConfigurationTokenizer.Property getPropertyByPath(final String s) {
        int idx = 0;
        final String[] _components = s.split("\\.");
        ParsedConfiguration cfg = this;
        while (idx < _components.length - 1) {
            cfg = cfg.getSection(_components[idx++]);
            if (cfg == null) {
                return null;
            }
        }
        final ConfigurationTokenizer.Property p = cfg.getPropertyMap().get(_components[idx]);
        if (p == null) {
            return null;
        }
        return p;
    }
    
    @Override
    public String toString() {
        return "ParsedSection{container=" + this.container + ", name=" + this.name + ", type=" + this.type;
    }
}
