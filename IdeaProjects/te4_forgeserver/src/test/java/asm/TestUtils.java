package asm;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.ClassNode;
import plus.fserver.api.inject.Field;
import plus.fserver.api.inject.FieldAccessor;
import plus.fserver.plugin.FieldAccessGen;


public class TestUtils {
    public static void main(String[] args) {
        Object str = new TestAsm().field;
        System.out.println(str);

        //byte[] res = test2();

//        byte[] res = test(
//                TsonFile.readFile(new File("build/classes/java/test/asm/TestAsm.class")),
//                "ex1", String.class
//        );

        //TsonFile.write(new File("TestAsm.class"), res);

        test3();
    }


    public static void test3(){
        TestAsm tasm = new TestAsm();
        tasm.field = "VALUE1";

        //Field field = new Field.Impl("a","b", null, "c");

        FieldAccessor<TestAsm, String> acc =
                (FieldAccessor<TestAsm, String>)
                        FieldAccessGen.compile(TestUtils.class.getClassLoader(), TestAsm.class, String.class, "field");

        //System.out.println(acc.get(tasm));
        acc.set(tasm, "XX_NULL_XX");
        System.out.println(tasm.field);

        System.out.println(acc.get(tasm));
    }



    public static byte[] test(byte[] bytes, String name, Class<?> type){
        final ClassNode classNode = new ClassNode();
        final ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);

        //classNode.visitField(0x1, name, Type.getDescriptor(type), null, null);

        //makeMtdSet(classNode, "net/minecraft/entity/Entity", "Lnet/minecraft/world/World", "world");

        //public Ljava/lang/String; field

        final ClassWriter writer = new ClassWriter(1);
        classNode.accept(writer);
        return writer.toByteArray();
    }
//
//
//    public static byte[] test2(){
//        final ClassNode classNode = new ClassNode();
//        makeAccess(classNode, "TestAsm", "net.minecraft.entity.Entity", "net.minecraft.world.World", "world");
//        final ClassWriter writer = new ClassWriter(1);
//        classNode.accept(writer);
//        return writer.toByteArray();
//    }



}