import plus.te4net.core.api.Var;
//import plus.te4net.core.node.HPCLTask;
import plus.tson.TsonMap;


public class TestAnnotations {
    public static void main(String[] args) {
        TsonMap map = new TsonMap();
        map.put("key", "v1");
        map.put("t1", 10.5d);

        //HPCLTask task = new HPCLTask(TestAnnotations.class, null, map);

        //task.inst();

    }



    public TestAnnotations(@Var(key = "key") String val, @Var(key = "t1") boolean test){
        System.out.println(val);
        System.out.println(test);

    }



}