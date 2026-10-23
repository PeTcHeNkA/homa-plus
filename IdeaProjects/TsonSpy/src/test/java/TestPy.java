import plus.tson.STsonParser;
import plus.tson.TsonFunc;



public class TestPy {

    public static void main(String[] args) {
        STsonParser parser = new STsonParser("{func = func(){print(\"PY\")}}");
        parser.with(TsonFunc.COMPILER);
        TsonFunc.COMPILER.tryInstallEngine("python");

        parser.getMap().getFunc("func").call();


    }



}
