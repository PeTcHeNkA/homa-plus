package plus.fserver.utls;

import net.minecraft.nbt.*;


public class NbtUtils {
    private static final NBTTagByte[]  TAG_BYTES;
    private static final NBTTagByte[]  TAG_BYTES_N;

    private static final NBTTagShort[] TAG_SHORTS;
    private static final NBTTagInt[]   TAG_INTS;
    public static final NBTTagByte     BYTE_TRUE;
    public static final NBTTagByte     BYTE_FALSE;
    public static final NBTTagInt      DATA_VERSION = new NBTTagInt(1343);
    public static final NBTTagInt      ITEM_DEFAULT_LIFESPAN = new NBTTagInt(6000);

    public static NBTTagByte create(byte b){
        if(b < 0)return TAG_BYTES_N[-b];
        return TAG_BYTES[b];
    }

    public static NBTTagInt create(int i){
        if(i > 197 || i < 0)return new NBTTagInt(i);
        return TAG_INTS[i];
    }

    public static NBTTagShort create(short s){
        if(s > 127 || s < 0)return new NBTTagShort(s);
        return TAG_SHORTS[s];
    }


    public static NBTTagByte create(boolean b){
        if(b)return BYTE_TRUE;
        return BYTE_FALSE;
    }


    static {
        NBTTagByte[] bytes = TAG_BYTES = new NBTTagByte[127];
        for (int i = 0; i < bytes.length; i++){
            bytes[i] = new NBTTagByte((byte) i);
        }
        BYTE_FALSE = bytes[0];
        BYTE_TRUE  = bytes[1];

        NBTTagByte[] bytesN = TAG_BYTES_N = new NBTTagByte[128];
        for (int i = 0; i < 128; i++){
            bytesN[i] = new NBTTagByte((byte) -i);
        }

        NBTTagInt[] ints = TAG_INTS = new NBTTagInt[198];
        for (int i = 0; i < ints.length; i++){
            ints[i] = new NBTTagInt(i);
        }

        NBTTagShort[] shorts = TAG_SHORTS = new NBTTagShort[128];
        for (int i = 0; i < shorts.length; i++){
            shorts[i] = new NBTTagShort((short) i);
        }
    }
}
