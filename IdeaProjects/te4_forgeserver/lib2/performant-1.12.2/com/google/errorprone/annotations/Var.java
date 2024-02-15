//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package com.google.errorprone.annotations;

import java.lang.annotation.*;
import javax.lang.model.element.*;

@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE })
@Retention(RetentionPolicy.RUNTIME)
@IncompatibleModifiers({ Modifier.FINAL })
public @interface Var {
}
