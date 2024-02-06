package asm;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import plus.tson.TsonFile;
import java.io.File;
import java.util.HashMap;
import static org.objectweb.asm.Opcodes.*;


public class MathJITEngine {
    private static final String OBJECT       = "java/lang/Object";
    private static final String INIT         = "<init>";
    private static final String DISC_VOID    = "()V";
    private static final String OP_INTERFACE = Operator2Num.class.getName();


    private final VrLoader loader = new VrLoader(getClass().getClassLoader());


    static class VrLoader extends ClassLoader{
        private final HashMap<String,byte[]> data = new HashMap<>();
        public VrLoader(ClassLoader classLoader) {
            super(classLoader);
        }
        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            byte[] clazz = data.get(name);
            if(clazz == null)return super.findClass(name);
            return defineClass(name, clazz, 0, clazz.length);
        }
    }

    public enum Operator{
        ADD(DADD), SUB(DSUB), DIV(DDIV), MUL(DMUL);
        private final int opcode;

        Operator(int opcode) {
            this.opcode = opcode;
        }
    }


    public void compile(String name, Operator operator){
        ClassWriter writer = new ClassWriter(0);

        writer.visit(V11, ACC_PUBLIC+ACC_SUPER, name, null, OBJECT,
                new String[]{"asm.Operator2Num"});

        //constructor
        MethodVisitor method = writer.visitMethod(ACC_PUBLIC, INIT, DISC_VOID, null, null);
        method.visitVarInsn(ALOAD, 0);
        method.visitMethodInsn(INVOKESPECIAL, OBJECT, INIT, DISC_VOID, false);
        method.visitInsn(RETURN);
        method.visitMaxs(1,1);
        method.visitEnd();

        //func
        MethodVisitor sumFunc = writer.visitMethod(ACC_PUBLIC, "calc",
                "(Lplus/tson/TsonObj;Lplus/tson/TsonObj;)Lplus/tson/TsonObj;", null, null);
        Label start = new Label();
        Label end   = new Label();
        //label 1
        sumFunc.visitLabel(start);
        sumFunc.visitTypeInsn(NEW, "plus/tson/TsonDouble");
        sumFunc.visitInsn(DUP);
        sumFunc.visitIntInsn(ALOAD, 1);
        sumFunc.visitMethodInsn(
                INVOKEINTERFACE, "plus/tson/TsonObj",
                "getDouble", "()D", true
        );
        sumFunc.visitIntInsn(ALOAD, 2);
        sumFunc.visitMethodInsn(
                INVOKEINTERFACE, "plus/tson/TsonObj",
                "getDouble", "()D", true
        );
        sumFunc.visitInsn(operator.opcode);
        //return
        sumFunc.visitMethodInsn(
                INVOKESPECIAL, "plus/tson/TsonDouble",
                INIT, "(D)V", false
        );
        sumFunc.visitInsn(ARETURN);
        //label 2
        sumFunc.visitLabel(end);

        sumFunc.visitLocalVariable(
                "this", "L"+name+";", null,
                start, end, 0
        );

        sumFunc.visitLocalVariable(
                "a1", "Lplus/tson/TsonObj;", null,
                start, end, 1
        );
        sumFunc.visitLocalVariable(
                "a2", "Lplus/tson/TsonObj;", null,
                start, end, 2
        );
        sumFunc.visitMaxs(6,3);
        sumFunc.visitEnd();

        byte[] data = writer.toByteArray();
        TsonFile.write(new File(name+".class"), data);

        loader.data.put(name, data);
    }
}
