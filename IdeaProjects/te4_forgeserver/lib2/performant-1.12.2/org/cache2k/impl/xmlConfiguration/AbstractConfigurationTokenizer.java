//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.impl.xmlConfiguration;

public abstract class AbstractConfigurationTokenizer implements ConfigurationTokenizer
{
    private final String source;
    
    public AbstractConfigurationTokenizer(final String _source) {
        this.source = _source;
    }
    
    @Override
    public String getSource() {
        return this.source;
    }
    
    protected final Nest returnNest(final String _sectionName) {
        return new MyNest(this.getSource(), this.getLineNumber(), _sectionName);
    }
    
    protected final Unnest returnUnnest() {
        return new MyUnnest(this.getSource(), this.getLineNumber());
    }
    
    protected final Property returnProperty(final String _name, final String _property) {
        return new MyProperty(this.getSource(), this.getLineNumber(), _name, _property);
    }
    
    private static class MyItem implements Item
    {
        private final String source;
        private final int lineNumber;
        
        public MyItem(final String _source, final int _lineNumber) {
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
    }
    
    private static class MyNest extends MyItem implements Nest
    {
        private final String sectionName;
        
        public MyNest(final String _source, final int _lineNumber, final String _sectionName) {
            super(_source, _lineNumber);
            this.sectionName = _sectionName;
        }
        
        @Override
        public String getSectionName() {
            return this.sectionName;
        }
        
        @Override
        public String toString() {
            return "Nest{sectionName='" + this.sectionName + '\'' + '}';
        }
    }
    
    private static class MyUnnest extends MyItem implements Unnest
    {
        public MyUnnest(final String _source, final int _lineNumber) {
            super(_source, _lineNumber);
        }
        
        @Override
        public String toString() {
            return "Unnest";
        }
    }
    
    private static class MyProperty extends MyItem implements Property
    {
        private final String name;
        private String value;
        private boolean expanded;
        
        public MyProperty(final String _source, final int _lineNumber, final String _name, final String _value) {
            super(_source, _lineNumber);
            this.name = _name;
            this.value = _value;
        }
        
        @Override
        public String getName() {
            return this.name;
        }
        
        @Override
        public String getValue() {
            return this.value;
        }
        
        @Override
        public void setValue(final String v) {
            this.value = v;
        }
        
        @Override
        public boolean isExpanded() {
            return this.expanded;
        }
        
        @Override
        public void setExpanded(final boolean _expanded) {
            this.expanded = _expanded;
        }
        
        @Override
        public String toString() {
            return "Property{name='" + this.name + '\'' + ", value='" + this.value + '\'' + '}';
        }
    }
}
