//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.checkerframework.checker.tainting.qual;

import java.lang.annotation.*;
import org.checkerframework.framework.qual.*;

@SubtypeOf({ Tainted.class })
@QualifierForLiterals({ LiteralKind.STRING })
@Target({ ElementType.TYPE_USE, ElementType.TYPE_PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@DefaultFor({ TypeUseLocation.LOWER_BOUND })
public @interface Untainted {
}
