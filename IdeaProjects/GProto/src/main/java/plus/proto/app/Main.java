package plus.proto.app;

import plus.proto.GroupScanner;
import plus.proto.JsonTMap;

public class Main {
    private static String path1 = "C:\\Games\\GF2Exilium\\GF2 Game\\GF2_Exilium_Data\\LocalCache\\Data\\Table";
    private static String path2 = "C:/Users/Te4hnoPlus/Desktop/GFLT/LangPackageTableEnusData.bytes";
    private static String path3 = "C:/Users/Te4hnoPlus/Desktop/GFLT/translations_release.json";
    private static String path4 = "result";

    public static void main(String[] args) throws Exception {
        if(args.length > 1 && args[0].equals("skin")){
            String[] args2 = new String[args.length-1];
            System.arraycopy(args, 0, args2, 0, args2.length);
            SkinMain.main(args2);
            return;
        }
        if(args.length >= 4){
            path1 = args[0];
            path2 = args[1];
            path3 = args[2];
            path4 = args[3];
        } else {
            System.out.println("Usage: Classificator.jar path/to/game/tables path/to/en/lang/pack.bytes path/to/translation.json path/to/output");
            return;
        }

        GroupScanner scanner = new GroupScanner(
                path1.replace("\\","/")
        );

        System.out.println("Reading pack ["+path2+"]");
        scanner.readPack(path2.replace("\\","/"));

        JsonTMap map = new JsonTMap();

        System.out.println("Loading translation ["+path3+"]");
        map.loadFrom(path3.replace("\\","/"));
        map.apply(scanner.getPack(), false);

        scanner.scanUsages();

        System.out.println("Scanning usages in ["+path1+"]");
        scanner.export(path4.replace("\\","/"), true);
    }
}