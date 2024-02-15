//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.checkerframework.checker.index.qual;

import java.lang.annotation.*;
import org.checkerframework.framework.qual.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.CONSTRUCTOR })
@PostconditionAnnotation(qualifier = LTLengthOf.class)
@InheritedAnnotation
public @interface EnsuresLTLengthOf {
    @JavaExpression
    String[] value();
    
    @JavaExpression
    @QualifierArgument("value")
    String[] targetValue();
    
    @JavaExpression
    @QualifierArgument("offset")
    String[] offset() default {};
}
