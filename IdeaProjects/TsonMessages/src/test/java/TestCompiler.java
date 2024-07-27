import plus.tson.ext.FuncCompiler;

public class TestCompiler {


    public String test1(String arg1, String arg2, String arg3) {
        return arg1+arg2+arg3;
    }


    public static void main(String[] args) {
        FuncCompiler.Func3A<String> func = FuncCompiler.compile(TestCompiler.class, "test1", String.class, String.class, String.class);

        System.out.println(func.call(new TestCompiler(), "a1", "A2", "A3"));
    }

}
