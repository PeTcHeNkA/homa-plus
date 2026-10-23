//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package com.google.errorprone.annotations;

import java.lang.annotation.*;

@Target({ ElementType.CONSTRUCTOR, ElementType.METHOD })
public @interface RestrictedApi {
    String checkerName() default "RestrictedApi";
    
    String explanation();
    
    String link();
    
    String allowedOnPath() default "";
    
    Class<? extends Annotation>[] whitelistAnnotations() default {};
    
    Class<? extends Annotation>[] whitelistWithWarningAnnotations() default {};
}
