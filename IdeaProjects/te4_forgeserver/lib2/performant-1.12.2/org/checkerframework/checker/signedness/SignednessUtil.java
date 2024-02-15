//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.checkerframework.checker.signedness;

import java.nio.*;
import java.io.*;
import java.math.*;

public final class SignednessUtil
{
    private SignednessUtil() {
        throw new Error("Do not instantiate");
    }
    
    public static ByteBuffer wrapUnsigned(final byte[] array) {
        return ByteBuffer.wrap(array);
    }
    
    public static ByteBuffer wrapUnsigned(final byte[] array, final int offset, final int length) {
        return ByteBuffer.wrap(array, offset, length);
    }
    
    public static int getUnsignedInt(final ByteBuffer b) {
        return b.getInt();
    }
    
    public static short getUnsignedShort(final ByteBuffer b) {
        return b.getShort();
    }
    
    public static byte getUnsigned(final ByteBuffer b) {
        return b.get();
    }
    
    public static byte getUnsigned(final ByteBuffer b, final int i) {
        return b.get(i);
    }
    
    public static ByteBuffer getUnsigned(final ByteBuffer b, final byte[] bs, final int i, final int l) {
        return b.get(bs, i, l);
    }
    
    public static ByteBuffer putUnsigned(final ByteBuffer b, final byte ubyte) {
        return b.put(ubyte);
    }
    
    public static ByteBuffer putUnsigned(final ByteBuffer b, final int i, final byte ubyte) {
        return b.put(i, ubyte);
    }
    
    public static IntBuffer putUnsigned(final IntBuffer b, final int uint) {
        return b.put(uint);
    }
    
    public static IntBuffer putUnsigned(final IntBuffer b, final int i, final int uint) {
        return b.put(i, uint);
    }
    
    public static IntBuffer putUnsigned(final IntBuffer b, final int[] uints) {
        return b.put(uints);
    }
    
    public static IntBuffer putUnsigned(final IntBuffer b, final int[] uints, final int i, final int l) {
        return b.put(uints, i, l);
    }
    
    public static int getUnsigned(final IntBuffer b, final int i) {
        return b.get(i);
    }
    
    public static ByteBuffer putUnsignedShort(final ByteBuffer b, final short ushort) {
        return b.putShort(ushort);
    }
    
    public static ByteBuffer putUnsignedShort(final ByteBuffer b, final int i, final short ushort) {
        return b.putShort(i, ushort);
    }
    
    public static ByteBuffer putUnsignedInt(final ByteBuffer b, final int uint) {
        return b.putInt(uint);
    }
    
    public static ByteBuffer putUnsignedInt(final ByteBuffer b, final int i, final int uint) {
        return b.putInt(i, uint);
    }
    
    public static ByteBuffer putUnsignedLong(final ByteBuffer b, final int i, final long ulong) {
        return b.putLong(i, ulong);
    }
    
    @Deprecated
    public static byte readUnsignedByte(final RandomAccessFile f) throws IOException {
        return f.readByte();
    }
    
    public static char readUnsignedChar(final RandomAccessFile f) throws IOException {
        return f.readChar();
    }
    
    @Deprecated
    public static short readUnsignedShort(final RandomAccessFile f) throws IOException {
        return f.readShort();
    }
    
    public static int readUnsignedInt(final RandomAccessFile f) throws IOException {
        return f.readInt();
    }
    
    public static long readUnsignedLong(final RandomAccessFile f) throws IOException {
        return f.readLong();
    }
    
    public static int readUnsigned(final RandomAccessFile f, final byte[] b, final int off, final int len) throws IOException {
        return f.read(b, off, len);
    }
    
    public static void readFullyUnsigned(final RandomAccessFile f, final byte[] b) throws IOException {
        f.readFully(b);
    }
    
    public static void writeUnsigned(final RandomAccessFile f, final byte[] bs, final int off, final int len) throws IOException {
        f.write(bs, off, len);
    }
    
    public static void writeUnsignedByte(final RandomAccessFile f, final byte b) throws IOException {
        f.writeByte(toUnsignedInt(b));
    }
    
    public static void writeUnsignedChar(final RandomAccessFile f, final char c) throws IOException {
        f.writeChar(toUnsignedInt(c));
    }
    
    public static void writeUnsignedShort(final RandomAccessFile f, final short s) throws IOException {
        f.writeShort(toUnsignedInt(s));
    }
    
    public static void writeUnsignedInt(final RandomAccessFile f, final int i) throws IOException {
        f.writeInt(i);
    }
    
    public static void writeUnsignedLong(final RandomAccessFile f, final long l) throws IOException {
        f.writeLong(l);
    }
    
    public static void getUnsigned(final ByteBuffer b, final byte[] bs) {
        b.get(bs);
    }
    
    @Deprecated
    public static int compareUnsigned(final long x, final long y) {
        return Long.compare(x + Long.MIN_VALUE, y + Long.MIN_VALUE);
    }
    
    @Deprecated
    public static int compareUnsigned(final int x, final int y) {
        return Integer.compare(x + Integer.MIN_VALUE, y + Integer.MIN_VALUE);
    }
    
    public static int compareUnsigned(final short x, final short y) {
        return compareUnsigned(toUnsignedInt(x), toUnsignedInt(y));
    }
    
    public static int compareUnsigned(final byte x, final byte y) {
        return compareUnsigned(toUnsignedInt(x), toUnsignedInt(y));
    }
    
    @Deprecated
    public static String toUnsignedString(final long l) {
        return toUnsignedBigInteger(l).toString();
    }
    
    @Deprecated
    public static String toUnsignedString(final long l, final int radix) {
        return toUnsignedBigInteger(l).toString(radix);
    }
    
    @Deprecated
    public static String toUnsignedString(final int i) {
        return Long.toString(toUnsignedLong(i));
    }
    
    @Deprecated
    public static String toUnsignedString(final int i, final int radix) {
        return Long.toString(toUnsignedLong(i), radix);
    }
    
    public static String toUnsignedString(final short s) {
        return Long.toString(toUnsignedLong(s));
    }
    
    public static String toUnsignedString(final short s, final int radix) {
        return Long.toString(toUnsignedLong(s), radix);
    }
    
    public static String toUnsignedString(final byte b) {
        return Long.toString(toUnsignedLong(b));
    }
    
    public static String toUnsignedString(final byte b, final int radix) {
        return Long.toString(toUnsignedLong(b), radix);
    }
    
    private static BigInteger toUnsignedBigInteger(final long l) {
        if (l >= 0L) {
            return BigInteger.valueOf(l);
        }
        final int upper = (int)(l >>> 32);
        final int lower = (int)l;
        return BigInteger.valueOf(toUnsignedLong(upper)).shiftLeft(32).add(BigInteger.valueOf(toUnsignedLong(lower)));
    }
    
    @Deprecated
    public static long toUnsignedLong(final int i) {
        return (long)i & 0xFFFFFFFFL;
    }
    
    @Deprecated
    public static long toUnsignedLong(final short s) {
        return (long)s & 0xFFFFL;
    }
    
    @Deprecated
    public static int toUnsignedInt(final short s) {
        return s & 0xFFFF;
    }
    
    @Deprecated
    public static long toUnsignedLong(final byte b) {
        return (long)b & 0xFFL;
    }
    
    @Deprecated
    public static int toUnsignedInt(final byte b) {
        return b & 0xFF;
    }
    
    public static short toUnsignedShort(final byte b) {
        return (short)(b & 0xFF);
    }
    
    public static long toUnsignedLong(final char c) {
        return (long)c & 0xFFL;
    }
    
    public static int toUnsignedInt(final char c) {
        return c & '\u00ff';
    }
    
    public static short toUnsignedShort(final char c) {
        return (short)(c & '\u00ff');
    }
    
    public static float toFloat(final byte b) {
        return toUnsignedBigInteger(toUnsignedLong(b)).floatValue();
    }
    
    public static float toFloat(final short s) {
        return toUnsignedBigInteger(toUnsignedLong(s)).floatValue();
    }
    
    public static float toFloat(final int i) {
        return toUnsignedBigInteger(toUnsignedLong(i)).floatValue();
    }
    
    public static float toFloat(final long l) {
        return toUnsignedBigInteger(l).floatValue();
    }
    
    public static double toDouble(final byte b) {
        return toUnsignedBigInteger(toUnsignedLong(b)).doubleValue();
    }
    
    public static double toDouble(final short s) {
        return toUnsignedBigInteger(toUnsignedLong(s)).doubleValue();
    }
    
    public static double toDouble(final int i) {
        return toUnsignedBigInteger(toUnsignedLong(i)).doubleValue();
    }
    
    public static double toDouble(final long l) {
        return toUnsignedBigInteger(l).doubleValue();
    }
    
    public static byte byteFromFloat(final float f) {
        assert f >= 0.0f;
        return (byte)f;
    }
    
    public static short shortFromFloat(final float f) {
        assert f >= 0.0f;
        return (short)f;
    }
    
    public static int intFromFloat(final float f) {
        assert f >= 0.0f;
        return (int)f;
    }
    
    public static long longFromFloat(final float f) {
        assert f >= 0.0f;
        return (long)f;
    }
    
    public static byte byteFromDouble(final double d) {
        assert d >= 0.0;
        return (byte)d;
    }
    
    public static short shortFromDouble(final double d) {
        assert d >= 0.0;
        return (short)d;
    }
    
    public static int intFromDouble(final double d) {
        assert d >= 0.0;
        return (int)d;
    }
    
    public static long longFromDouble(final double d) {
        assert d >= 0.0;
        return (long)d;
    }
}
