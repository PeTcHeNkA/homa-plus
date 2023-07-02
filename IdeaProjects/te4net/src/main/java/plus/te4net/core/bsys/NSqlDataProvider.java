package plus.te4net.core.bsys;

import plus.te4net.utl.data.SqliteUtils;
import java.sql.Connection;
import java.util.HashSet;


public class NSqlDataProvider implements SqliteUtils, IDataProvider {
    private final HashSet<String> existTables = new HashSet<>();
    private final Connection conn;
    private final String suffix;

    public NSqlDataProvider(String suffix, String dbFile) {
        this.conn = createConnection(dbFile);
        this.suffix = "@"+suffix;
        createTable(suffix);
    }

    public NSqlDataProvider(String suffix, Connection conn) {
        this.conn = conn;
        this.suffix = "@"+suffix;
        createTable(suffix);
    }


    private void checkTable(String table){
        if(existTables.add(table)){
            createTable(table + suffix);
        }
    }


    @Override
    public void saveRaw(String name, String table, byte[] data) {
        if(table.isBlank())table = "root";
        checkTable(table);
        String realTableName = table + suffix;
        if(data==null) removeData(name,realTableName);
        else saveData(name, realTableName, data);
    }


    @Override
    public byte[] getRaw(String name, String table){
        checkTable(table);
        return getData(name, table + suffix);
    }


    @Override
    public Connection getConnection() {
        return conn;
    }
}