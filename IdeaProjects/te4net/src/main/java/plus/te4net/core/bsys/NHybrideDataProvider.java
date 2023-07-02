package plus.te4net.core.bsys;

import io.netty.util.collection.IntObjectHashMap;


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

    //TODO НЕ ПРОВЕРЕНО
    private byte[] process(String name, String table, byte[] data, boolean save){
        String[] path = table.split("/");
        int lastCtnI = -1;
        int key = 0;
        int lastKey = 0;
        for(int i=0;i<path.length;i++){
            String cur = path[i];
            key = key*31 + cur.hashCode();
            if(containsKey(key)){
                lastCtnI = i;
                lastKey = key;
            }
        }
        IDataProvider provider;
        if(lastCtnI > -1){
            StringBuilder newTable = new StringBuilder(path[lastCtnI]);
            for (int i=lastCtnI+1;i<path.length;i++){
                newTable.append("/").append(path[i]);
            }
            table = newTable.toString();
            provider = getOrDefault(lastKey, defaultProvider);
        } else {
            provider = defaultProvider;
        }
        if(save){
            provider.saveRaw(name, table, data);
            return null;
        } else {
            return provider.getRaw(name, table);
        }
    }


    @Override
    public byte[] getRaw(String name, String table) {
        return process(name,table,null,false);
    }


    @Override
    public void saveRaw(String name, String table, byte[] data) {
        process(name, table, data, true);
    }
}
