//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.checkerframework.checker.nullness.qual;

import java.lang.annotation.*;
import org.checkerframework.framework.qual.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.CONSTRUCTOR })
@PostconditionAnnotation(qualifier = KeyFor.class)
@InheritedAnnotation
public @interface EnsuresKeyFor {
    String[] value();
    
    @JavaExpression
    @QualifierArgument("value")
    String[] map();
}
