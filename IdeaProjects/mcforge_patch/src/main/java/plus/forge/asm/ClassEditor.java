package plus.forge.asm;

import org.apache.commons.io.IOUtils;
import org.objectweb.asm.*;

import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import static plus.forge.TweakEngine.prepareName;


//todo in work
public class ClassEditor {
    public static byte[] getSrc(ClassLoader loader, String name){
        InputStream stream = loader.getResourceAsStream(prepareName(name));
        if (stream == null) {
            return null;
        }
        try {
            byte[] result = IOUtils.toByteArray(stream);
            stream.close();
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    String test = "TESTSTR";

    public static void main(String... args){
        System.out.println(ManagementFactory.getRuntimeMXBean().getSpecVersion());


        ClassLoader loader = ClassEditor.class.getClassLoader();
        byte[] data = getSrc(loader, "plus.forge.asm.ClassEditor");

        int flags = ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS;


        ClassReader reader = new ClassReader(data);
        ClassWriter writer = new ClassWriter(reader, flags);
        reader.accept(new ClassPublicator(Opcodes.ASM5), flags);

        byte[] result = writer.toByteArray();;

        System.out.println(result);
        //System.out.println(~16);
    }
}