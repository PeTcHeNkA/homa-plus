package plus.te4net.core.bsys;

import io.netty.util.collection.IntObjectHashMap;
import plus.te4net.core.api.IDataProvider;


public class NHybrideDataProvider extends IntObjectHashMap<IDataProvider> implements IDataProvider{
    private final IDataProvider defaultProvider;
    public NHybrideDataProvider(IDataProvider defaultProvider) {
        this.defaultProvider = defaultProvider;
    }

    public void include(String table, IDataProvider provider){
        if(table==null)throw new NullPointerException("Table is null!");
        if(table.charAt(table.length()-1)!='/')table = table+"/";
        int hs = calcHs(table);
        if(super.containsKey(hs))throw new RuntimeException("Table ["+table+"] ready exists!");
        put(hs, provider);
    }


    public IDataProvider get(String table){
        return get(calcHs(table));
    }


    private int calcHs(String table){
        String[] path = table.split("/");
        int hs = 0;
        for(String s:path){
            hs = hs*31 + s.hashCode();
        }
        return hs;
    }


    private byte[] process(String[] path, byte[] data, boolean save){
        int lastCtnI = -1, key = 0;
        IDataProvider provider = defaultProvider;
        for(int i=0;i<path.length-1;i++){
            key = key * 31 + path[i].hashCode();
            IDataProvider prv;
            if((prv = get(key))!=null){
                provider = prv;
                lastCtnI = i;
            }
        }
        if(provider != defaultProvider){
            String[] nwPath = new String[path.length-lastCtnI];
            System.arraycopy(path, lastCtnI, nwPath, 0, nwPath.length);
            path = nwPath;
        }
        if(save){
            provider.saveRaw(data, path);
            return null;
        } else {
            return provider.getRaw(path);
        }
    }


    @Override
    public byte[] getRaw(String... path) {
        return process(path,null,false);
    }


    @Override
    public void saveRaw(byte[] data, String... path) {
        process(path, data, true);
    }


    @Override
    public String[] getItems(String... path) {
        int lastCtnI = -1, key = 0;
        IDataProvider provider = defaultProvider;
        for(int i=0;i<path.length-1;i++){
            key = key * 31 + path[i].hashCode();
            IDataProvider prv;
            if((prv = get(key))!=null){
                provider = prv;
                lastCtnI = i;
            }
        }
        if(provider != defaultProvider){
            String[] nwPath = new String[path.length-lastCtnI];
            System.arraycopy(path, lastCtnI, nwPath, 0, nwPath.length);
            return provider.getItems(nwPath);
        } else return provider.getItems(path);
    }
}
