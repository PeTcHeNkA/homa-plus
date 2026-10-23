package plus.fserver.plugin;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import plus.fserver.api.inject.Field;


public class FieldAccessGen {
    private static volatile int ID;

    private static synchronized int nextId(){
        return ID++;
    }


    public static void injectField(ClassNode classNode, Field field){
        Class<?> type = field.type();
        String typeName;
        if(type == Object.class){
            typeName = field.tName();
        } else {
            typeName = "L"+type.getName().replace(".","/")+";";
        }
        injectField(classNode, field.name(), typeName);
    }


    public static void injectField(ClassNode classNode, String name, String type){
        classNode.visitField(0x1, name, type, null, null);
    }


    public static Object compile(ClassLoader loader, Class<?> clazz, Class<?> type, String name){
        return compile(loader, clazz.getName(), type.getName(), name);
    }


    public static Object compile(ClassLoader loader, String clazz, String type, String name){
        String className = "Access_"+nextId();

        final ClassNode classNode = new ClassNode();
        makeAccess(classNode, className, clazz, type, name);

        final ClassWriter writer = new ClassWriter(1);
        classNode.accept(writer);

        byte[] bytes = writer.toByteArray();
        //TsonFile.write(new File(className+".class"), bytes);

        try {
            return MemoryClassLoader.define(loader, "plus.fserver.jit." + className, bytes).newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }


    private static void makeAccess(ClassNode classNode, String name, String clazz, String result, String fieldName){
        clazz  =     clazz .replace(".","/");
        result =     result.replace(".","/");

        String selfName = "plus/fserver/jit/"+name;

        classNode.visit(Opcodes.V1_8, 0x1, selfName,
                "Ljava/lang/Object;Lplus/fserver/api/inject/FieldAccessor<L"+clazz+";L"+result+";>;",
                "java/lang/Object", new String[]{"plus/fserver/api/inject/FieldAccessor"});

        makeCtor(classNode);

        makeMtdGet(classNode, clazz, result, fieldName);
        makeMtdGetBridge(classNode, selfName, clazz, result, fieldName);

        makeMtdSet(classNode, clazz, result, fieldName);
        makeMtdSetBridge(classNode, selfName, clazz, result);
    }


    private static void makeMtdSetBridge(ClassNode classNode, String selfName, String clazz, String result){
        MethodVisitor method = classNode.visitMethod(0x1041, "set",
                "(Ljava/lang/Object;Ljava/lang/Object;)V", null, null
        );

        method.visitCode();

        method.visitVarInsn(Opcodes.ALOAD, 0);
        method.visitVarInsn(Opcodes.ALOAD, 1);
        method.visitTypeInsn(Opcodes.CHECKCAST, clazz);
        method.visitVarInsn(Opcodes.ALOAD, 2);
        method.visitTypeInsn(Opcodes.CHECKCAST, result);

        method.visitMethodInsn(Opcodes.INVOKEVIRTUAL, selfName, "set", "(L"+clazz+";L"+result+";)V", false);

        method.visitInsn(Opcodes.RETURN);
        method.visitMaxs(3, 3);

        method.visitEnd();
    }


    private static void makeMtdSet(ClassNode classNode, String clazz, String result, String fieldName){
        MethodVisitor method = classNode.visitMethod(0x1, "set",
                "(L"+clazz+";L"+result+";)V", null, null
        );

        method.visitCode();
        method.visitVarInsn(Opcodes.ALOAD, 1);
        method.visitVarInsn(Opcodes.ALOAD, 2);
        method.visitFieldInsn(Opcodes.PUTFIELD, clazz, fieldName, "L"+result+";");

        method.visitInsn(Opcodes.RETURN);
        method.visitMaxs(2, 3);

        method.visitEnd();
    }


    private static void makeCtor(ClassNode classNode){
        MethodVisitor con = classNode.visitMethod(0x1, "<init>", "()V", null, null);

        con.visitCode();
        con.visitVarInsn(Opcodes.ALOAD, 0);

        con.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);

        con.visitInsn(Opcodes.RETURN);
        con.visitMaxs(1, 1);
        con.visitEnd();
    }


    private static void makeMtdGet(ClassNode classNode, String clazz, String result, String fieldName){
        MethodVisitor method = classNode.visitMethod(0x1, "get",
                "(L"+clazz+";)L"+result+";", null, null
        );
        method.visitCode();
        method.visitVarInsn(Opcodes.ALOAD, 1);
        method.visitFieldInsn(Opcodes.GETFIELD, clazz, fieldName, "L"+result+";");
        method.visitInsn(Opcodes.ARETURN);
        method.visitMaxs(1, 2);

        method.visitEnd();
    }


    private static void makeMtdGetBridge(ClassNode classNode, String selfName, String clazz, String result, String fieldName){
        MethodVisitor method = classNode.visitMethod(0x1, "get",
                "(Ljava/lang/Object;)Ljava/lang/Object;", null, null
        );
        method.visitCode();

        method.visitVarInsn(Opcodes.ALOAD, 0);
        method.visitVarInsn(Opcodes.ALOAD, 1);
        method.visitTypeInsn(Opcodes.CHECKCAST, clazz);

        method.visitMethodInsn(Opcodes.INVOKEVIRTUAL, selfName, "get", "(L"+clazz+";)L"+result+";", false);

        method.visitInsn(Opcodes.ARETURN);
        method.visitMaxs(2, 2);

        method.visitEnd();
    }
}