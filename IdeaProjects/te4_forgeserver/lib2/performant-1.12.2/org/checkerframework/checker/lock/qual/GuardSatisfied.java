//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.checkerframework.checker.lock.qual;

import org.checkerframework.framework.qual.*;
import java.lang.annotation.*;

@SubtypeOf({ GuardedByUnknown.class })
@Documented
@Retention(RetentionPolicy.RUNTIME)
@TargetLocations({ TypeUseLocation.RECEIVER, TypeUseLocation.PARAMETER, TypeUseLocation.RETURN })
@Target({ ElementType.TYPE_USE })
public @interface GuardSatisfied {
    int value() default -1;
}
