package asm;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import plus.tson.TsonFile;
import java.io.File;

import static org.objectweb.asm.Opcodes.V11;


public class Test {

    public static void main(String[] args) {
        byte[] bytes = TsonFile.readFile(new File("C:\\Users\\Te4hnoPlus\\IdeaProjects\\TsonCord\\build\\classes\\java\\test\\TestASM3.class"));

        ClassReader reader = new ClassReader(bytes);

        ClassWriter writer = new ClassWriter(reader, 0);

        //writer.visit(V11, reader.getAccess(), reader.getClassName(), null,null,null);

        bytes = writer.toByteArray();

        TsonFile.write(new File("TestASM3.class"), bytes);
        //new TestASM3();
    }


    public class World{
        public boolean isRemote;
    }




}
