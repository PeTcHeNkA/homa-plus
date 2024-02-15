//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.checkerframework.checker.lock.qual;

import org.checkerframework.framework.qual.*;
import java.lang.annotation.*;

@InvisibleQualifier
@SubtypeOf({})
@Documented
@DefaultQualifierInHierarchy
@DefaultFor(value = { TypeUseLocation.LOWER_BOUND }, types = { Void.class })
@QualifierForLiterals({ LiteralKind.NULL })
@DefaultQualifierInHierarchyInUncheckedCode
@DefaultInUncheckedCodeFor({ TypeUseLocation.PARAMETER, TypeUseLocation.LOWER_BOUND })
@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface LockPossiblyHeld {
}
