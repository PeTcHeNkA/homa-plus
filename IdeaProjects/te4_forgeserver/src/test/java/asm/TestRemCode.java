package asm;

import plus.fserver.plugin.PathTransformer;
import plus.tson.TsonFile;
import java.io.File;


public class TestRemCode {
    public static void main(String[] args) {

        byte[] clazz = TsonFile.readFile(new File("build/classes/java/test/asm/TestAsm2.class"));

        byte[] result = PathTransformer.transformFast("TestAsm2", clazz);


        TsonFile.write(new File("TestAsm2.class"), result);

    }
}
