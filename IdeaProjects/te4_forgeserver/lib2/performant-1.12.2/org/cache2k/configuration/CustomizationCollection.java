//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.configuration;

import java.util.*;
import java.io.*;

public interface CustomizationCollection<T> extends Collection<CustomizationSupplier<T>>, Serializable
{
    boolean add(final CustomizationSupplier<T> p0);
    
    boolean addAll(final Collection<? extends CustomizationSupplier<T>> p0);
}
