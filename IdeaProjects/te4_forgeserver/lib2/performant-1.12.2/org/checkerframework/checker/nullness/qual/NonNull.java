//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.checkerframework.checker.nullness.qual;

import org.checkerframework.framework.qual.*;
import java.lang.annotation.*;

@SubtypeOf({ MonotonicNonNull.class })
@QualifierForLiterals({ LiteralKind.STRING })
@DefaultQualifierInHierarchy
@DefaultFor({ TypeUseLocation.EXCEPTION_PARAMETER })
@UpperBoundFor(typeKinds = { TypeKind.PACKAGE, TypeKind.INT, TypeKind.BOOLEAN, TypeKind.CHAR, TypeKind.DOUBLE, TypeKind.FLOAT, TypeKind.LONG, TypeKind.SHORT, TypeKind.BYTE })
@DefaultInUncheckedCodeFor({ TypeUseLocation.PARAMETER, TypeUseLocation.LOWER_BOUND })
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE_USE, ElementType.TYPE_PARAMETER })
public @interface NonNull {
}
