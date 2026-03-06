package utils;

import plus.tson.TJsonParser;
import plus.tson.TsonFile;
import plus.tson.TsonMap;

public class BBImporter {
    public static void main(String[] args) {
        System.out.println("NEN");
        String pth = "C:\\Users\\HomaPlus\\Desktop\\Project.bbmodel";

        TJsonParser parser = new TJsonParser(TsonFile.read(pth.replace("\\","/")));

        TsonMap map = parser.getMap();

        System.out.println(map);
    }
}
