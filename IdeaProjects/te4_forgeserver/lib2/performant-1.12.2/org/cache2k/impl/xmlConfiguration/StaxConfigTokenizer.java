//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.impl.xmlConfiguration;

import java.util.*;
import java.io.*;
import javax.xml.stream.*;

public class StaxConfigTokenizer extends AbstractConfigurationTokenizer
{
    private XMLStreamReader input;
    private LinkedList<String> hierarchy;
    private String startName;
    private String value;
    
    public StaxConfigTokenizer(final String _source, final InputStream in, final String _encoding) throws XMLStreamException {
        super(_source);
        this.hierarchy = new LinkedList<String>();
        final XMLInputFactory f = XMLInputFactory.newInstance();
        this.input = f.createXMLStreamReader(in, _encoding);
    }
    
    public int getLineNumber() {
        return this.input.getLocation().getLineNumber();
    }
    
    public ConfigurationTokenizer.Item next() throws Exception {
        while (this.input.hasNext()) {
            final int _type = this.input.next();
            switch (_type) {
                case 1: {
                    if (this.startName != null) {
                        this.hierarchy.push(this.startName);
                        this.startName = this.input.getLocalName();
                        return (ConfigurationTokenizer.Item)this.returnNest((String)this.hierarchy.element());
                    }
                    this.startName = this.input.getLocalName();
                    continue;
                }
                case 4: {
                    this.value = this.input.getText();
                    continue;
                }
                case 2: {
                    final String _name = this.input.getLocalName();
                    if (this.startName != null && this.startName.equals(_name)) {
                        this.startName = null;
                        return (ConfigurationTokenizer.Item)this.returnProperty(_name, this.value);
                    }
                    if (_name.equals(this.hierarchy.element())) {
                        this.hierarchy.pop();
                        return (ConfigurationTokenizer.Item)this.returnUnnest();
                    }
                    continue;
                }
            }
        }
        return null;
    }
    
    public static class Factory implements TokenizerFactory
    {
        @Override
        public ConfigurationTokenizer createTokenizer(final String _source, final InputStream in, final String _encoding) throws XMLStreamException {
            return (ConfigurationTokenizer)new StaxConfigTokenizer(_source, in, _encoding);
        }
    }
}
