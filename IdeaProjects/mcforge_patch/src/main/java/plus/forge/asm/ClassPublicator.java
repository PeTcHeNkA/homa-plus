package plus.forge.asm;

import org.objectweb.asm.*;
import java.util.HashSet;

//todo in work
public class ClassPublicator extends ClassVisitor {
    private final HashSet<String> keys = new HashSet<>();

    public static byte[] makePublic(byte[] data, String... fields){
        if(fields.length==0)return data;
        int flags = ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS;
        ClassReader reader = new ClassReader(data);
        ClassWriter writer = new ClassWriter(reader, flags);
        reader.accept(new ClassPublicator(Opcodes.ASM5, fields), flags);
        return writer.toByteArray();
    }


    protected ClassPublicator(int api, String... fields) {
        super(api);
        for (String key : fields) keys.add(key);
    }


    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        if (keys.contains(name)) {
            access = access & ~16;
        }
        //System.out.println(access+":"+name);
        return super.visitField(access, name, desc, signature, value);
    }
}