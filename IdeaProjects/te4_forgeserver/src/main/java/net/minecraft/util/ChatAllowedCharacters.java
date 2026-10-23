package net.minecraft.util;

import io.netty.util.ResourceLeakDetector;
import io.netty.util.ResourceLeakDetector.Level;


public class ChatAllowedCharacters {
    public static final ResourceLeakDetector.Level NETTY_LEAK_DETECTION;
    public static final char[] ILLEGAL_STRUCTURE_CHARACTERS;
    public static final char[] ILLEGAL_FILE_CHARACTERS;

    public ChatAllowedCharacters() {}


    public static boolean isAllowedCharacter(char c) {
        return c != 167 && c >= ' ' && c != 127;
    }


    public static String filterAllowedCharacters(String msg) {
        char[] chars = msg.toCharArray();
        int size = chars.length, realSize = 0;

        for(int i = 0; i < size; ++i) {
            char cur = chars[i];
            if (isAllowedCharacter(cur)) {
                chars[realSize] = cur;
                ++realSize;
            }
        }
        if(realSize == size)return msg;
        return new String(chars, 0, realSize);
    }


    static {
        NETTY_LEAK_DETECTION = Level.DISABLED;
        ILLEGAL_STRUCTURE_CHARACTERS = new char[]{'.', '\n', '\r', '\t', '\u0000', '\f', '`', '?', '*', '\\', '<', '>', '|', '"'};
        ILLEGAL_FILE_CHARACTERS = new char[]{'/', '\n', '\r', '\t', '\u0000', '\f', '`', '?', '*', '\\', '<', '>', '|', '"', ':'};
        if (System.getProperty("io.netty.leakDetection.level") == null) {
            ResourceLeakDetector.setLevel(NETTY_LEAK_DETECTION);
        }
    }
}