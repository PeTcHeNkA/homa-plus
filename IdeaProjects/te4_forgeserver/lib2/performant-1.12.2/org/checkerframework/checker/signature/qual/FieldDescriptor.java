//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.checkerframework.checker.signature.qual;

import org.checkerframework.framework.qual.*;
import java.lang.annotation.*;

@SubtypeOf({ SignatureUnknown.class })
@QualifierForLiterals(stringPatterns = { "^\\[*([BCDFIJSZ]|L[A-Za-z_][A-Za-z_0-9]*(/[A-Za-z_][A-Za-z_0-9]*)*(\\$[A-Za-z_0-9]+)*;)$" })
@Target({ ElementType.TYPE_USE, ElementType.TYPE_PARAMETER })
public @interface FieldDescriptor {
}
