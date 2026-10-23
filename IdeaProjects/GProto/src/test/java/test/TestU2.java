package test;

import plus.proto.GroupScanner;
import plus.proto.*;


public class TestU2 {
    private static String path1 = "C:\\Games\\GF2Exilium\\GF2 Game\\GF2_Exilium_Data\\LocalCache\\Data\\Table";
    private static String path2 = "C:/Users/Te4hnoPlus/Desktop/GFLT/LangPackageTableEnusData.bytes";
    private static String path3 = "C:/Users/Te4hnoPlus/Desktop/GFLT/translations_release.json";
    private static String path4 = "result";

    public static void main(String[] args) {
//        if(args.length >= 4){
//            path1 = args[0];
//            path2 = args[1];
//            path3 = args[2];
//            path4 = args[3];
//        } else {
//            System.out.println("Usage: Classificator.jar path_to_game_tables path_to_en_lang_pack path_to_translations path_to_output");
//            return;
//        }

        GroupScanner scanner = new GroupScanner(
                path1.replace("\\","/")
        );

        scanner.readPack(path2.replace("\\","/"));

        JsonTMap map = new JsonTMap();
        map.loadFrom(path3.replace("\\","/"));
        map.apply(scanner.getPack(), false);

        scanner.scanUsages();
        scanner.scanUsages();

        //scanner.export(path4.replace("\\","/"), true);
    }
}
