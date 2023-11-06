package plus.te4net.mds.crypt;

import plus.te4net.mds.util.FastRandom;
import java.util.Arrays;


public class CryptKey {
    protected final byte[] key = new byte[14];
    protected final long time;
    public short usages;

    public CryptKey(String key) {
        if(key==null)throw new CryptException(CryptException.Type.NULL_KEY);
        time = 0;
        byte[] bytes = key.getBytes();
        if(bytes.length != 14)throw new CryptException(CryptException.Type.INVALID_KEY);
        System.arraycopy(bytes, 0, this.key, 0, 14);
    }


    protected CryptKey(long time, short usages) {
        this.time = time;
        this.usages = usages;
    }


    public static CryptKey newKey(long time, short usages){
        CryptKey result = new CryptKey(time, usages);
        FastRandom.INST.nextBytes(result.key);
        return result;
    }


    public CryptKey(byte[] bytes, int delta) {
        long result = 0;
        for (int i = 0; i < 6; i++) {
            result <<= Byte.SIZE;
            result |= (bytes[i + delta] & 0xFF);
        }
        time = result;
        usages = (short) (((bytes[6] & 0xFF) << 8) | ((bytes[7] & 0xFF)));
        System.arraycopy(bytes, delta + 8, key, 0, 14);
    }


    public void save(byte[] data, int delta) {
        System.arraycopy(key, 0, data, 8 + delta, 14);
        data[delta + 6] = (byte) (usages >> 8);
        data[delta + 7] = (byte) (usages);
        long l = time;
        for (int i = 5; i >= 0; i--) {
            data[i + delta] = (byte) (l & 0xFF);
            l >>= Byte.SIZE;
        }
    }


    public long getTime() {
        return time;
    }


    public boolean maybeUse(){
        if (usages == Short.MIN_VALUE) return true;
        if (usages < Short.MAX_VALUE) {
            return System.currentTimeMillis() < time;
        }
        return false;
    }


    public boolean use() {
        if (usages == Short.MIN_VALUE) return true;
        if (usages < Short.MAX_VALUE) {
            ++usages;
            return System.currentTimeMillis() < time;
        }
        return false;
    }


    @Override
    public String toString() {
        return new String(key);
    }


    @Override
    public int hashCode() {
        int h = 0;
        for (byte v : key) {
            h = 31 * h + (v & 0xff);
        }
        return h;
    }


    @Override
    public boolean equals(Object o) {
        if (o instanceof CryptKey) {
            return Arrays.equals(key, ((CryptKey) o).key);
        }
        return false;
    }
}
