package plus.te4net.mds.crypt;

import plus.te4net.mds.util.FastRandom;
import plus.te4net.core.data.SqliteUtils;
import java.sql.Connection;
import java.util.HashSet;
import java.util.Set;


public class CryptoLibSql<V> extends CryptoMap<CryptoDk,V> implements SqliteUtils {
    private final Connection conn;
    private final Set<Character> usedTables = new HashSet<>();
    //todo maybe optimize

    protected CryptoLibSql(String cacheFile) {
        conn = createConnection(cacheFile);
    }


    private void createTableIfNeed(char c){
        if(!usedTables.contains(c)){
            usedTables.add(c);
            createTable("K_"+ c);
        }
    }


    public CryptoDk create(int time, byte[] data, V val){
        CryptoDk key = createKey(time, -1, val);
        String strKey = key.toString();
        char section = strKey.charAt(0);
        createTableIfNeed(section);
        saveData(strKey, "K_"+section, data);
        return key;
    }


    public CryptoDk get(String key){
        Node<CryptoDk, V> node = getNode(new CryptKey(key));
        if(node!=null)return node.getKey();
        char section = key.charAt(0);
        createTableIfNeed(section);
        byte[] data = getData(key,"K_"+section);
        return null;
    }


    @Override
    protected CryptoDk newKey(long time, short usages) {
        return new CryptoDk(time, usages);
    }


    @Override
    protected CryptoDk encodeKey(byte[] bytes, int delta) {
        throw new UnsupportedOperationException();
    }


    @Override
    protected V encodeValue(byte[] data, int delta) {
        throw new UnsupportedOperationException();
    }


    @Override
    public Connection getConnection() {
        return conn;
    }
}

class CryptoDk extends CryptKey{
    public byte[] data;

    protected CryptoDk(long time, short usages) {
        super(time, usages);
        FastRandom.INST.nextBytes(key);
    }


    @Override
    public String toString() {
        return super.toString();
    }
}