package plus.te4net.core.node;

import plus.te4net.core.api.IDataProvider;
import plus.te4net.core.api.HttpClient;
import plus.te4net.core.api.Var;
import plus.te4net.core.bsys.DataProviderBuilder;
import plus.tson.TsonList;
import plus.tson.TsonMap;
import plus.tson.TsonObj;
import plus.tson.TsonStr;
import plus.util.PCLTask;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;


final class HPCLTask extends PCLTask {
    private final TsonMap config;
    HEWorker worker;

    public HPCLTask(Class<?> clazz, HEWorker worker, TsonMap config){
        this(clazz, 0, worker, config);
    }


    HPCLTask(Class<?> clazz, int priority, HEWorker worker, TsonMap config){
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
            Parameter[] params = max.getParameters();
            Object[] args = new Object[params.length];
            for(int i=0;i<args.length;i++){
                args[i] = getArg(params[i]);
            }
            max.setAccessible(true);
            return max.newInstance(args);
        }
    }


    private Object getArg(Parameter param){
        Class<?> type = param.getType();
        Var var = searchVar(param);
        if(var==null) {
            if (type == TsonMap.class) return config;
            if (type == HEWorker.class || type == IDataProvider.class ||
                    type == HWorker.class) return worker;
            if (type == HttpClient.class) return worker.client();

            if (type == int.class || type == float.class || type == double.class) return 0;
            if (type == boolean.class) return false;

            return new HPCLTask(type.getClass(), worker, config).inst();
        } else {
            boolean nullDef = var.def() == null || var.def().isEmpty();
            TsonObj obj = config.get(var.key());

            if(type == int.class){
                if(obj==null){
                    if(nullDef) return 0;
                    return Integer.parseInt(var.def());
                }
                return obj.getInt();
            }
            if(type == float.class){
                if(obj==null){
                    if(nullDef) return 0f;
                    return Float.parseFloat(var.def());
                }
                return obj.getFloat();
            }
            if(type == boolean.class){
                if(obj==null){
                    if(nullDef) return false;
                    return var.def().equalsIgnoreCase("true");
                }
                return obj.getBool();
            }

            if(obj==null && nullDef) return null;

            if (type == TsonMap.class){
                if(obj==null) return new TsonMap(var.def());
                return obj.getMap();
            }
            if(type == String.class){
                if(obj==null) return var.def();
                return obj.getStr();
            }
            if(type == TsonObj.class){
                if(obj==null) return TsonObj.ofString(var.def());
                return obj;
            }
            if(type == TsonList.class){
                if(obj==null) return new TsonList(var.def());
                return obj.getList();
            }
            if(type == IDataProvider.class){
                if(obj==null) return DataProviderBuilder.ofTson(worker, TsonObj.ofString(var.def()));
                return DataProviderBuilder.ofTson(worker, obj);
            }
            if(type == TsonStr.class){
                if(obj==null) return new TsonStr(var.def());
                return obj.getList();
            }
            return null;
        }
    }


    private Var searchVar(Parameter param){
        for (Annotation a:param.getAnnotations()){
            if(a instanceof Var)return (Var) a;
        }
        return null;
    }
}