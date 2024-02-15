//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.checkerframework.checker.fenum.qual;

import java.lang.annotation.*;
import org.checkerframework.framework.qual.*;

@Documented
@Target({ ElementType.TYPE_USE, ElementType.TYPE_PARAMETER })
@TargetLocations({ TypeUseLocation.EXPLICIT_LOWER_BOUND, TypeUseLocation.EXPLICIT_UPPER_BOUND })
@SubtypeOf({})
@Retention(RetentionPolicy.RUNTIME)
@DefaultFor({ TypeUseLocation.LOWER_BOUND })
public @interface FenumBottom {
}
