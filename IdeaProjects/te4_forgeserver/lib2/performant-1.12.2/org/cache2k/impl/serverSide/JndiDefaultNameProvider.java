//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.impl.serverSide;

import org.cache2k.spi.*;
import org.cache2k.*;
import javax.naming.*;
import org.cache2k.core.util.*;

public class JndiDefaultNameProvider implements Cache2kExtensionProvider
{
    @Override
    public void registerCache2kExtension() {
        try {
            try {
                Context ctx = new InitialContext();
                ctx = (Context)ctx.lookup("java:comp/env");
                final String _name = (String)ctx.lookup("org.cache2k.CacheManager.defaultName");
                if (_name != null) {
                    CacheManager.setDefaultName(_name);
                }
            }
            catch (NoInitialContextException ex2) {}
        }
        catch (NoClassDefFoundError noClassDefFoundError) {}
        catch (Exception ex) {
            Log.getLog((Class)JndiDefaultNameProvider.class).warn("Exception setting default cache manager name", (Throwable)ex);
        }
    }
}
