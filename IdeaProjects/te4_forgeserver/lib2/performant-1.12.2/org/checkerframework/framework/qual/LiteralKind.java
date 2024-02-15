//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.checkerframework.framework.qual;

import java.util.*;

public enum LiteralKind
{
    NULL, 
    INT, 
    LONG, 
    FLOAT, 
    DOUBLE, 
    BOOLEAN, 
    CHAR, 
    STRING, 
    ALL, 
    PRIMITIVE;
    
    public static List<LiteralKind> allLiteralKinds() {
        final List<LiteralKind> list = new ArrayList<LiteralKind>(Arrays.asList(values()));
        list.remove(LiteralKind.ALL);
        list.remove(LiteralKind.PRIMITIVE);
        return list;
    }
    
    public static List<LiteralKind> primitiveLiteralKinds() {
        return new ArrayList<LiteralKind>(Arrays.asList(LiteralKind.INT, LiteralKind.LONG, LiteralKind.FLOAT, LiteralKind.DOUBLE, LiteralKind.BOOLEAN, LiteralKind.CHAR));
    }
}
