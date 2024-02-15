//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.checkerframework.checker.regex;

import org.checkerframework.framework.qual.*;
import org.checkerframework.checker.regex.qual.*;
import java.util.regex.*;
import org.checkerframework.dataflow.qual.*;

public final class RegexUtil
{
    private RegexUtil() {
        throw new Error("do not instantiate");
    }
    
    @Pure
    @EnsuresQualifierIf(result = true, expression = { "#1" }, qualifier = Regex.class)
    public static boolean isRegex(final String s) {
        return isRegex(s, 0);
    }
    
    @Pure
    @EnsuresQualifierIf(result = true, expression = { "#1" }, qualifier = Regex.class)
    public static boolean isRegex(final String s, final int groups) {
        Pattern p;
        try {
            p = Pattern.compile(s);
        }
        catch (PatternSyntaxException e) {
            return false;
        }
        return getGroupCount(p) >= groups;
    }
    
    @Pure
    @EnsuresQualifierIf(result = true, expression = { "#1" }, qualifier = Regex.class)
    public static boolean isRegex(final char c) {
        return isRegex(Character.toString(c));
    }
    
    @SideEffectFree
    public static String regexError(final String s) {
        return regexError(s, 0);
    }
    
    @SideEffectFree
    public static String regexError(final String s, final int groups) {
        try {
            final Pattern p = Pattern.compile(s);
            final int actualGroups = getGroupCount(p);
            if (actualGroups < groups) {
                return regexErrorMessage(s, groups, actualGroups);
            }
        }
        catch (PatternSyntaxException e) {
            return e.getMessage();
        }
        return null;
    }
    
    @SideEffectFree
    public static PatternSyntaxException regexException(final String s) {
        return regexException(s, 0);
    }
    
    @SideEffectFree
    public static PatternSyntaxException regexException(final String s, final int groups) {
        try {
            final Pattern p = Pattern.compile(s);
            final int actualGroups = getGroupCount(p);
            if (actualGroups < groups) {
                return new PatternSyntaxException(regexErrorMessage(s, groups, actualGroups), s, -1);
            }
        }
        catch (PatternSyntaxException pse) {
            return pse;
        }
        return null;
    }
    
    @SideEffectFree
    public static String asRegex(final String s) {
        return asRegex(s, 0);
    }
    
    @SideEffectFree
    public static String asRegex(final String s, final int groups) {
        try {
            final Pattern p = Pattern.compile(s);
            final int actualGroups = getGroupCount(p);
            if (actualGroups < groups) {
                throw new Error(regexErrorMessage(s, groups, actualGroups));
            }
            return s;
        }
        catch (PatternSyntaxException e) {
            throw new Error(e);
        }
    }
    
    @SideEffectFree
    private static String regexErrorMessage(final String s, final int expectedGroups, final int actualGroups) {
        return "regex \"" + s + "\" has " + actualGroups + " groups, but " + expectedGroups + " groups are needed.";
    }
    
    @Pure
    private static int getGroupCount(final Pattern p) {
        return p.matcher("").groupCount();
    }
    
    public static class CheckedPatternSyntaxException extends Exception
    {
        private static final long serialVersionUID = 6266881831979001480L;
        private final PatternSyntaxException pse;
        
        public CheckedPatternSyntaxException(final PatternSyntaxException pse) {
            this.pse = pse;
        }
        
        public CheckedPatternSyntaxException(final String desc, final String regex, final int index) {
            this(new PatternSyntaxException(desc, regex, index));
        }
        
        public String getDescription() {
            return this.pse.getDescription();
        }
        
        public int getIndex() {
            return this.pse.getIndex();
        }
        
        @Pure
        @Override
        public String getMessage() {
            return this.pse.getMessage();
        }
        
        public String getPattern() {
            return this.pse.getPattern();
        }
    }
}
