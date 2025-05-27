package test;

import com.google.protobuf.InvalidProtocolBufferException;
import plus.proto.GroupScanner;
import plus.proto.LangPackV2;

import java.io.File;

public class TestU2 {
    public static void main(String[] args) throws InvalidProtocolBufferException {
        GroupScanner scanner = new GroupScanner(
                "C:\\Games\\GF2Exilium\\GF2 Game\\GF2_Exilium_Data\\LocalCache\\Data\\Table".replace("\\","/")
        );

        LangPackV2 pack2 = new LangPackV2();
        pack2.read(new File(scanner.getRoot()+"/LangPackageTableEnusData.bytes"), false);

        scanner.readPack("C:/Users/Te4hnoPlus/Desktop/GFLT/LangPackageTableEnusData.bytes");
        scanner.getPack().mergeWith(pack2);

        scanner.scanUsages();

        scanner.export("result", true);
    }
}
