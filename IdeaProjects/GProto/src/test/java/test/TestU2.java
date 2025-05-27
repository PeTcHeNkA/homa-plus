package test;

import plus.proto.GroupScanner;

public class TestU2 {
    public static void main(String[] args) {
        GroupScanner scanner = new GroupScanner(
                "C:\\Games\\GF2Exilium\\GF2 Game\\GF2_Exilium_Data\\LocalCache\\Data\\Table".replace("\\","/")
        );

        scanner.readPack(scanner.getRoot()+"/LangPackageTableEnusData.bytes");
        scanner.scanUsages();

        scanner.export("result");
    }
}
