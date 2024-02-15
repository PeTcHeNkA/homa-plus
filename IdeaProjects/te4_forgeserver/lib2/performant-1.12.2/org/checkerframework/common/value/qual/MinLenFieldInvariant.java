//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.checkerframework.common.value.qual;

import java.lang.annotation.*;

@Inherited
@Target({ ElementType.TYPE })
public @interface MinLenFieldInvariant {
    int[] minLen();
    
    String[] field();
}
