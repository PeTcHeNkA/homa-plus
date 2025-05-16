package test;

import plus.proto.LangPack;
import plus.tson.TsonFile;

import java.io.File;
import static test.Main.FILE1;


public class NewTest {
    public static void main(String[] args) {
        //"Hostiles 1"
        LangPack pack = new LangPack();
        pack.read(new File(FILE1));

        pack.replace("Hostiles 1", "Hostiles 1 XXXXXXXXXXXXXXXXXXXXX");


        pack.applyChanges();

        pack.write(new File("LangPackageTableEnusData.bytes"));
    }
}
