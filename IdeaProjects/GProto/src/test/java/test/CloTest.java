package test;

import plus.proto.clo.CloData;
import plus.proto.tparsers.IntConsumer;
import plus.proto.tparsers.UPack;
import plus.tson.TsonFile;
import java.io.File;
import java.util.List;


public class CloTest {
    public static void main(String[] args) throws Exception {
        File file = new File("ClothesDataMOD.bytes");
        CloData clo = new CloData();

        clo.readFrom(file);

        //System.out.println(clo.scanNames());

//        for (String name:clo.scanNames()){
//            System.out.println(clo.search(name));
//        }

        //CloData.CharacterSkinData items = clo.search("Tololo");
        //System.out.println(items.getSkin(0));
//        CloData.CharacterSkinData flo2  = clo.search("Flo");
//
//        System.out.println(items);
//        System.out.println(flo2);
//
//        //System.out.println(items.getAll());
//
//        items.modifySkin(flo2.getSkin(1));
//
//        //System.out.println("====================");
//        clo.write(new File("ClothesDataMOD.bytes"));

        //TsonFile.write(new File("data.json"), clo.toString());
        //System.out.println(clo);
    }
}
