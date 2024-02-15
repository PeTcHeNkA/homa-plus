//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.checkerframework.checker.initialization.qual;

import org.checkerframework.framework.qual.*;
import java.lang.annotation.*;

@SubtypeOf({ UnknownInitialization.class })
@DefaultQualifierInHierarchy
@DefaultFor({ TypeUseLocation.IMPLICIT_UPPER_BOUND, TypeUseLocation.IMPLICIT_LOWER_BOUND, TypeUseLocation.EXCEPTION_PARAMETER })
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE_USE, ElementType.TYPE_PARAMETER })
public @interface Initialized {
}
