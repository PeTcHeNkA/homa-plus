//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.impl.xmlConfiguration;

import org.cache2k.*;

public class ConfigurationException extends CacheException
{
    public ConfigurationException(final String message) {
        super(message);
    }
    
    public ConfigurationException(final String message, final Throwable cause) {
        super(message, cause);
    }
    
    public ConfigurationException(final String message, final String _source) {
        super(message + " at " + _source);
    }
    
    public ConfigurationException(final String _message, final SourceLocation _location) {
        super(_message + " (" + _location.getSource() + ":" + _location.getLineNumber() + ")");
    }
}
