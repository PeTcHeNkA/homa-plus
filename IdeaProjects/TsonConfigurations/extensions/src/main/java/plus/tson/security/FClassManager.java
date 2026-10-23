package plus.tson.security;

import plus.tson.TsonMap;
import plus.tson.utl.Te4HashMap;


public class FClassManager implements ClassManager{
    //private final Te4HashMap<String,ConstructorStd> map = new Te4HashMap<>();

    FClassManager(){
        //map.put("#", Test::new);
    }

    class Test{
        public Test(TsonMap map){

        }
    }

    @Override
    public Object newInstance(String className, Object... args) throws Exception {
        return ClassManager.super.newInstance(className, args);
    }
}
