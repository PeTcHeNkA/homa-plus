//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.checkerframework.checker.nullness.qual;

import org.checkerframework.framework.qual.*;
import java.lang.annotation.*;

@Documented
@SubtypeOf({ Nullable.class })
@Target({ ElementType.TYPE_USE })
@MonotonicQualifier(NonNull.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface MonotonicNonNull {
}
