package plus.ds.security;

import plus.tson.TsonClass;
import plus.tson.TsonList;
import plus.tson.TsonMap;
import plus.tson.TsonObj;
import plus.tson.security.ClassManager;
import java.lang.reflect.InvocationTargetException;


public class ComplexityAgent implements ClassManager {
    private final ClassManager parent;

    public ComplexityAgent(ClassManager parent) {
        this.parent = parent;
    }

    @Override
    public Class<?> forName(String clazz) {
        return parent.forName(clazz);
    }


    @Override
    public Class<?> notFounded(String clazz) {
        return Object.class;
    }


    @Override
    public Object newInstance(Class<?> clazz, Object... args) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        TsonMap map = new TsonMap();
        map.put("clazz", new TsonClass(clazz));
        TsonList list = new TsonList();
        for (Object obj:args){
            if(obj instanceof TsonObj tsonObj){
                list.add(tsonObj);
            }
        }
        map.put("constructor", list);
        return map;
    }
}