package plus.te4net.core.bsys;

import plus.asyutl.buf.FastBuffer;
import plus.te4net.core.node.HWorker;
import plus.tson.TsonList;
import plus.tson.TsonMap;
import plus.tson.TsonObj;

import java.sql.Connection;


public class DataProviderBuilder {
    enum Type{
        FILE, SQL, JAR, HYBRID, DUBLE,
    }
    private IDataProvider provider;
    private IDataProvider latest;

    public DataProviderBuilder defaultBy(IDataProvider provider){
        this.provider = provider;
        this.latest = provider;
        return this;
    }

    public DataProviderBuilder defaultBy(String filePath){
        provider = new NFileDataProvider(filePath);
        return this;
    }

    public DataProviderBuilder defaultBy(Type type, Object... init){
        switch (type){
            case JAR: provider = new NJarFileDataProvider();break;
            case HYBRID: provider = new NHybrideDataProvider((IDataProvider) init[0]);break;
            case FILE: provider = new NFileDataProvider((String) init[0]);break;
            case SQL: provider = new NSqlDataProvider((String) init[0], (Connection) init[1]);break;
            case DUBLE:provider = new NDubleProvider((IDataProvider) init[0], (IDataProvider) init[1]);
        }
        return this;
    }


    public DataProviderBuilder useHybrid(){
        if(provider instanceof NHybrideDataProvider)return this;
        provider = new NHybrideDataProvider(provider);
        latest = provider;
        return this;
    }

    public DataProviderBuilder mount(String table, Type type, Object... init){
        IDataProvider innner;
        switch (type){
            case HYBRID:innner = new NHybrideDataProvider((IDataProvider) init[0]);break;
            case FILE:  innner = new NFileDataProvider((String) init[0]);break;
            case SQL:   innner = new NSqlDataProvider((String) init[0], (Connection) init[1]);break;
            case JAR:   innner = new NJarFileDataProvider();break;
            case DUBLE: innner = new NDubleProvider((IDataProvider) init[0], (IDataProvider) init[1]);break;
            default: throw new RuntimeException();
        }
        if(!(provider instanceof NHybrideDataProvider)){
            provider = new NHybrideDataProvider(provider);
        }
        NHybrideDataProvider hb = (NHybrideDataProvider) latest;
        hb.include(table, innner);
        latest = innner;
        return this;
    }

    public DataProviderBuilder up(){
        latest = provider;
        return this;
    }

    public IDataProvider build(){
        return provider;
    }


    public static IDataProvider ofTson(HWorker worker, TsonObj obj){
        return ofTson(worker, obj, new FastBuffer<>());
    }

    //{root='dir:domains/lochost.ru/img',lochost.ru='dir:domains/lochost.ru/img'}
    private static IDataProvider ofTson(HWorker worker, TsonObj obj, FastBuffer<IDataProvider> def){
        if(obj.isString()){
            String str = obj.getStr();
            String[] items = str.split(":");
            switch (items[0]) {
                case "dir":
                    return new NFileDataProvider(items[1],
                            items.length > 2 && items[2].equalsIgnoreCase("true"));
                case "sql":
                    return new NSqlDataProvider(items[1], items[2]);
                case "root":
                    if(items.length==1) {
                        return worker.getRepository().getDefDataPovider();
                    } else {
                        return new NPrefixDataProvider(worker.getRepository().getDefDataPovider(),items[1]);
                    }
                case "def":
                    if (items.length == 2) {
                        return items[1].isBlank() ? def.get() : def.get(Integer.parseInt(items[1]));
                    } else if (items.length == 3) {
                        return new NPrefixDataProvider(def.get(Integer.parseInt(items[2])), items[1]);
                    }
                    break;
            }
        }
        if(obj.isMap()) {
            TsonMap map = obj.getMap();
            IDataProvider root = ofTson(worker, map.remove("root"));
            if(!(root instanceof NHybrideDataProvider)){
                root = new NHybrideDataProvider(root);
            }
            def.put(root);
            NHybrideDataProvider hb = (NHybrideDataProvider) root;
            for(String key: map.keySet()){
                hb.include(key, ofTson(worker, map.get(key), def));
            }
            def.flush();
            return hb;
        }
        if (obj.isList()) {
            TsonList lst = obj.getList();
            if(lst.size()==2){
                return new NDubleProvider(
                        ofTson(worker, lst.get(0),def),
                        ofTson(worker, lst.get(1),def)
                );
            } else if(lst.size()==1){
                return new NDubleProvider(
                        ofTson(worker, lst.get(0),def),
                        def.get()
                );
            }
            throw new RuntimeException(obj+" size not 1 or 2!");
        }
        throw new RuntimeException("["+obj+"] not supported!");
    }
}
