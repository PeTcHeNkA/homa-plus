package test;

import plus.proto.LangPackV2;

import java.io.File;
import java.util.Arrays;

import static test.Main.FILE1;

public class Test3 {

    public static void main(String[] args) throws Exception {
//        byte[] data = new byte[4];
//        LangPackV2.writeInt(data, 123456);
//        System.out.println(Arrays.toString(data));
//        System.out.println(LangPackV2.readInt(data));

        LangPackV2 lpack1 = new LangPackV2();
        lpack1.read(new File("C:\\Users\\Te4hnoPlus\\Desktop\\GFLT\\LangPackageTableEnusDataRES_F.bytes"), false);

        System.out.println(lpack1);
    }
}