package plus.te4net.core.node;

import plus.te4net.core.api.IHttpClient;
import plus.tson.TsonMap;
import plus.util.PCLTask;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;


final class HPCLTask extends PCLTask {
    private final TsonMap config;
    final HWorker worker;

    HPCLTask(Class<?> clazz, HWorker worker, TsonMap config){
        this(clazz, 0, worker, config);
    }

    HPCLTask(Class<?> clazz, int priority, HWorker worker, TsonMap config){
        super(clazz, 500+priority);
        this.worker = worker;
        this.config = config;
    }


    @Override
    public Object inst() {
        try {
            return newInst0(super.clazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private Object newInst0(Class<?> clazz) throws Exception {
        Constructor<?> empty = null;
        Constructor<?> max = null;
        int curArgs=0;
        for(Constructor<?> constructor:clazz.getDeclaredConstructors()){
            Type[] types = constructor.getGenericParameterTypes();
            if(constructor.getModifiers() != Modifier.PUBLIC)continue;
            if(curArgs > types.length)continue;
            curArgs = types.length;
            if(types.length==0){
                empty = constructor;
            } else {
                max = constructor;
            }
        }
        if(max==null){
            if(empty==null)return null;
            empty.setAccessible(true);
            return empty.newInstance();
        } else {
            Type[] types = max.getGenericParameterTypes();
            Object[] args = new Object[types.length];
            for(int i=0;i<args.length;i++){
                args[i] = getArg(types[i]);
            }
            max.setAccessible(true);
            return max.newInstance(args);
        }
    }


    private Object getArg(Type type){
        if(type==TsonMap.class)return config;
        if(type==HWorker.class)return worker;
        if(type==HWorkerRepository.class)return worker.getRepository();
        if(type==IHttpClient.class)return worker.client();
        return new HPCLTask(type.getClass(), worker, config).inst();
    }
}