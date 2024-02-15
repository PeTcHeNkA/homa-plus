//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.checkerframework.checker.index.qual;

import java.lang.annotation.*;

@Target({ ElementType.TYPE_USE, ElementType.TYPE_PARAMETER, ElementType.METHOD })
public @interface LengthOf {
    String[] value();
}
