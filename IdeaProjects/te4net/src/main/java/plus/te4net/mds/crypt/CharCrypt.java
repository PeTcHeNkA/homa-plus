package plus.te4net.mds.crypt;

import java.util.Random;



public class CharCrypt extends Random {

    public byte[] lib =
            "qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM1234567890-"
            .getBytes();



    public void nextBytes(byte[] bytes) {
        for (int i = 0; i < bytes.length;i++){
            bytes[i] = lib[nextInt(lib.length)];
        }
    }


    public int nextInt(int bound) {
        int r = next(31);
        int m = bound - 1;
        if ((bound & m) == 0) r = (int)((bound * (long)r) >> 31);
        else {
            for (int u = r; u - (r = u % bound) + m < 0; u = next(31)) ;
        }
        return r;
    }
}
