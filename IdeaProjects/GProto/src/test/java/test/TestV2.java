package test;

import com.google.protobuf.InvalidProtocolBufferException;
import plus.proto.JsonTMap;
import plus.proto.LangPackV2;
import java.io.File;
import static test.Main.FILE1;
import static test.Main.FILE2;


public class TestV2 {
    public static String FILE_RU = "C:\\Users\\Te4hnoPlus\\Desktop\\GFLT\\LangPackageTableEnusDataRES.bytes";

    public static void main(String[] args) throws Exception {
        long time = System.currentTimeMillis();

        LangPackV2 lpack1 = new LangPackV2();
        lpack1.read(new File(FILE1), false);

        JsonTMap map = new JsonTMap();
        map.loadFrom("C:\\Users\\Te4hnoPlus\\Desktop\\GFLT\\translation_ru.json");
        map.apply(lpack1, false);

        lpack1.write(new File(FILE2));
        System.out.println("SAVED");

        long cur = System.currentTimeMillis();
        System.out.println("Spend time: " + (cur - time)+"ms");
    }
}