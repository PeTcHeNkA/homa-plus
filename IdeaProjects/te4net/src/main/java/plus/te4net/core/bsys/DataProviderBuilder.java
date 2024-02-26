package plus.te4net.core.bsys;

import plus.asyutl.buf.FastBuffer;
import plus.te4net.core.api.IDataProvider;
import plus.te4net.core.api.PublicUsage;
import plus.te4net.core.node.HEWorker;
import plus.tson.*;
import java.sql.Connection;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;


@PublicUsage
public class DataProviderBuilder {
    enum Type{
        FILE, SQL, JAR, HYBRID, DOUBLE,
    }
    private static final Executor EMPTY = Runnable::run;
    private IDataProvider provider;
    private IDataProvider latest;
    private Executor executor;

    public DataProviderBuilder(Executor executor) {
        this.executor = executor;
    }


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
            case DOUBLE:provider = new NDubleProvider((IDataProvider) init[0], (IDataProvider) init[1], executor);
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
            case DOUBLE: innner = new NDubleProvider((IDataProvider) init[0], (IDataProvider) init[1], executor);break;
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


    public static IDataProvider ofTson(HEWorker worker, TsonObj obj){
        return ofTson(worker.pool(), worker.getDefDataPovider(), obj, new FastBuffer<>());
    }


    public static IDataProvider ofTson(Executor executor, TsonObj obj){
        return ofTson(executor, null, obj, new FastBuffer<>());
    }


    public static IDataProvider ofTson(Executor executor, IDataProvider defProvider, TsonObj obj){
        return ofTson(executor, defProvider, obj, new FastBuffer<>());
    }


    public static IDataProvider ofTson(TsonObj obj){
        return ofTson(EMPTY, null, obj, new FastBuffer<>());
    }


    public static IDataProvider ofTson(IDataProvider defProvider, TsonObj obj){
        return ofTson(EMPTY, defProvider, obj, new FastBuffer<>());
    }


    private static IDataProvider ofTson(Executor executor, IDataProvider defProvider, TsonObj obj, FastBuffer<IDataProvider> def){
        if(obj==null)return defProvider;
        if(obj.isString()){
            String str = obj.getStr();
            String[] items = str.split(":");
            switch (items[0]) {
                case "dir":
                    return new NFileDataProvider(items[1],
                            items.length > 2 && items[2].equalsIgnoreCase("true"));
                case "sql":
                    if(items.length==3) return new NSqlDataProvider(items[1], items[2]);
                    if(items.length==2) return new NSqlDataProvider(null, items[1]);
                case "root":
                    if(items.length==1) {
                        return defProvider;
                    } else {
                        return new NPrefixDataProvider(defProvider, items[1]);
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
            String type = map.getOrDefault("type", new TsonStr("hybrid")).getStr();
            map.remove("type");
            switch (type){
                case "hybrid":{
                    IDataProvider root = ofTson(executor, defProvider, map.remove("root"));
                    if(!(root instanceof NHybrideDataProvider)){
                        root = new NHybrideDataProvider(root);
                    }
                    def.put(root);
                    NHybrideDataProvider hb = (NHybrideDataProvider) root;
                    for(String key: map.keySet()){
                        hb.include(key, ofTson(executor, defProvider, map.get(key), def));
                    }
                    def.flush();
                    return hb;
                }
                case "memory":
                    if(executor instanceof ScheduledExecutorService service){
                        return new NMemoryCachedProvider(service,
                                ofTson(executor, defProvider, map.get("parent"), def),
                                map.getOrDefault("time", new TsonInt(180)).getInt()
                        );
                    } else {
                        throw new RuntimeException("[ memory ] not supported by current Executor. Use [ScheduledExecutorService]");
                    }
            }
        }
        if (obj.isList()) {
            TsonList lst = obj.getList();
            if(lst.size()==2){
                return new NDubleProvider(
                        ofTson(executor, defProvider, lst.get(0),def),
                        ofTson(executor, defProvider, lst.get(1),def),
                        executor);
            } else if(lst.size()==1){
                return new NDubleProvider(
                        ofTson(executor, defProvider, lst.get(0),def),
                        def.get(),
                        executor);
            }
            throw new RuntimeException(obj+" size not 1 or 2!");
        }
        throw new RuntimeException("[ "+obj+" ] not supported!");
    }
}
