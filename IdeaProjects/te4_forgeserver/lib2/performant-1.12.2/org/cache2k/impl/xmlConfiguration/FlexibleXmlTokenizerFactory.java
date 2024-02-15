//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.impl.xmlConfiguration;

import org.xmlpull.v1.*;
import java.io.*;

public class FlexibleXmlTokenizerFactory implements TokenizerFactory
{
    private final TokenizerFactory realTokenizer;
    
    public FlexibleXmlTokenizerFactory() {
        this.realTokenizer = this.detectUsableTokenizer();
    }
    
    TokenizerFactory detectUsableTokenizer() {
        try {
            XmlPullParser.class.toString();
            return new XppConfigTokenizer.Factory();
        }
        catch (LinkageError ex) {
            return new StaxConfigTokenizer.Factory();
        }
    }
    
    @Override
    public ConfigurationTokenizer createTokenizer(final String _source, final InputStream in, final String _encoding) throws Exception {
        return this.realTokenizer.createTokenizer(_source, in, _encoding);
    }
}
