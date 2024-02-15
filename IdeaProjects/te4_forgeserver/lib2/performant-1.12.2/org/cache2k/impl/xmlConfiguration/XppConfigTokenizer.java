//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.impl.xmlConfiguration;

import java.util.*;
import org.xmlpull.v1.*;
import java.io.*;

public class XppConfigTokenizer extends AbstractConfigurationTokenizer
{
    private final XmlPullParser input;
    private final LinkedList<String> hierarchy;
    private String startName;
    private String value;
    private boolean startFlag;
    
    XppConfigTokenizer(final String _source, final InputStream is, final String _encoding) throws XmlPullParserException {
        super(_source);
        this.hierarchy = new LinkedList<String>();
        this.startFlag = true;
        final XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        (this.input = factory.newPullParser()).setInput(is, _encoding);
    }
    
    public int getLineNumber() {
        return this.input.getLineNumber();
    }
    
    public ConfigurationTokenizer.Item next() throws Exception {
        int _eventType;
        while ((_eventType = this.nextEvent()) != 1) {
            switch (_eventType) {
                case 2: {
                    if (this.startName != null) {
                        this.hierarchy.push(this.startName);
                        this.startName = this.input.getName();
                        return (ConfigurationTokenizer.Item)this.returnNest((String)this.hierarchy.element());
                    }
                    this.startName = this.input.getName();
                    continue;
                }
                case 4: {
                    this.value = this.input.getText();
                    continue;
                }
                case 3: {
                    final String _name = this.input.getName();
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
    
    private int nextEvent() throws XmlPullParserException, IOException {
        if (this.startFlag) {
            this.startFlag = false;
            return this.input.getEventType();
        }
        return this.input.next();
    }
    
    public static class Factory implements TokenizerFactory
    {
        public ConfigurationTokenizer createTokenizer(final String _source, final InputStream in, final String _encoding) throws XmlPullParserException {
            return (ConfigurationTokenizer)new XppConfigTokenizer(_source, in, _encoding);
        }
    }
}
