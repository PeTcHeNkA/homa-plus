package asm;

import plus.fserver.api.inject.FieldAccessor;
import plus.fserver.plugin.FieldAccessGen;


public class TestUtils {
    public static void main(String[] args) {
        test3();
    }


    public static void test3(){
        TestAsm tasm = new TestAsm();
        tasm.field = "VALUE1";

        FieldAccessor<TestAsm, String> acc = (FieldAccessor<TestAsm, String>) FieldAccessGen.compile(
                TestUtils.class.getClassLoader(), TestAsm.class, String.class, "field"
        );

        //System.out.println(acc.get(tasm));
        acc.set(tasm, "XX_NULL_XX");
        System.out.println(tasm.field);

        System.out.println(acc.get(tasm));
    }
}