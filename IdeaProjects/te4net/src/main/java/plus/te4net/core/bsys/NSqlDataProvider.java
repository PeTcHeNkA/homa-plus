package plus.te4net.core.bsys;

import plus.te4net.core.api.IDataProvider;
import plus.te4net.core.data.SqliteUtils;
import java.sql.Connection;
import java.util.HashSet;


public class NSqlDataProvider implements SqliteUtils, IDataProvider {
    private final HashSet<String> existTables = new HashSet<>();
    private final Connection conn;
    private final String suffix;

    public NSqlDataProvider(String suffix, String dbFile) {
        this.conn = createConnection(dbFile);
        this.suffix = suffix==null?"@root":"@"+suffix;
        //createTable(this.suffix);
    }

    public NSqlDataProvider(String suffix, Connection conn) {
        this.conn = conn;
        this.suffix = suffix==null?"@root":"@"+suffix;
        //createTable(this.suffix);
    }


    private void checkTable(String table){
        if(existTables.add(table)){
            createTable(table + suffix);
        }
    }


    private String getTable(String[] path){
        if(path.length==2)return path[0];
        int len = path.length-1;
        StringBuilder path01 = new StringBuilder(path[0]);
        for(int i=1;i<len;i++){
            path01.append('/').append(path[i]);
        }
        return path01.toString();
    }


    @Override
    public void saveRaw(byte[] data, String[] path) {
        String table = getTable(path);
        if(table.isBlank())table = "root";
        checkTable(table);
        String realTableName = table + suffix;
        if(data==null) removeData(path[path.length-1],realTableName);
        else saveData(path[path.length-1], realTableName, data);
    }


    @Override
    public byte[] getRaw(String[] path){
        String table = getTable(path);
        checkTable(table);
        return getData(path[path.length-1], table + suffix);
    }


    @Override
    public Connection getConnection() {
        return conn;
    }


    @Override
    public String[] getItems(String... path) {
        StringBuilder path01 = new StringBuilder(path[0]);
        for(int i=1;i<path.length;i++){
            path01.append('/').append(path[i]);
        }
        String table = path01.toString();
        checkTable(table);
        return SqliteUtils.super.getKeys(table + suffix);
    }
}