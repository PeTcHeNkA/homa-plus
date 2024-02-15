//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.checkerframework.checker.nullness.qual;

import java.lang.annotation.*;
import org.checkerframework.framework.qual.*;

@SubtypeOf({ UnknownKeyFor.class })
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE_USE, ElementType.TYPE_PARAMETER })
public @interface KeyFor {
    @JavaExpression
    String[] value();
}
