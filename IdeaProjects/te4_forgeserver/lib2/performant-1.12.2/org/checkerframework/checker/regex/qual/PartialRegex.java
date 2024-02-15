//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.checkerframework.checker.regex.qual;

import org.checkerframework.framework.qual.*;
import java.lang.annotation.*;

@InvisibleQualifier
@SubtypeOf({ UnknownRegex.class })
@Target({})
public @interface PartialRegex {
    String value() default "";
}
