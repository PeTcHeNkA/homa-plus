//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.checkerframework.checker.lock.qual;

import java.lang.annotation.*;
import org.checkerframework.framework.qual.*;

@SubtypeOf({ GuardedByUnknown.class })
@Documented
@DefaultQualifierInHierarchy
@DefaultInUncheckedCodeFor({ TypeUseLocation.PARAMETER })
@DefaultFor(value = { TypeUseLocation.EXCEPTION_PARAMETER, TypeUseLocation.UPPER_BOUND }, typeKinds = { TypeKind.BOOLEAN, TypeKind.BYTE, TypeKind.CHAR, TypeKind.DOUBLE, TypeKind.FLOAT, TypeKind.INT, TypeKind.LONG, TypeKind.SHORT }, types = { String.class, Void.class })
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE_USE, ElementType.TYPE_PARAMETER })
public @interface GuardedBy {
    @JavaExpression
    String[] value() default {};
}
