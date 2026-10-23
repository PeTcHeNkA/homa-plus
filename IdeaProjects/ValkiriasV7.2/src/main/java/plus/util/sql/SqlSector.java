package plus.util.sql;

import plus.util.cfg.Config;
import java.sql.*;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class SqlSector implements DataFloader{
    private final Statement statmt;
    private final String Table;
    private final List<String> Collums;
    private final String SaveFormat;
    private static boolean hasDriver = false;
    private String localUrl = null;
    private boolean globalNetwork = false;


    public SqlSector(String URL, String User, String Password, String table,List<String> collums) {
        try {
            Collums = collums;
            Table = table==null?"YandeReDb":table;

            if(!hasDriver){
                Class.forName("org.sqlite.JDBC");
                hasDriver = true;
            }
            Connection conn = DriverManager.getConnection("jdbc:mysql://" + URL, User, Password);
            statmt = conn.createStatement();
            statmt.execute(String.format("CREATE TABLE if not exists '%s' ('user' text);", table));
            System.out.println("| YandeRe : SqLite initialized");
        } catch (Exception err) {
            err.printStackTrace();
            throw new NullPointerException();
        }
        globalNetwork = true;
        localUrl = URL.replace(":", "_").replace("/", "_");
        CreateCollums(table);

        SaveFormat = String.format("INSERT INTO '%s' ('user', '%s') VALUES ('%%s')", Table, (String.join("', '", Collums)));
    }


    public SqlSector(String sqlName,String table,List<String> collums) {
        try {
            Collums = collums;
            Table = table==null?"YandeReDb":table;

            if(!hasDriver){
                Class.forName("org.sqlite.JDBC");
                hasDriver = true;
            }
            java.sql.Connection conn = DriverManager.getConnection(String.format("jdbc:sqlite:%s.s3db", sqlName));
            statmt = conn.createStatement();
            statmt.execute(String.format("CREATE TABLE if not exists '%s' ('user' text);", table));
            System.out.println("| YandeRe : SqLite initialized");
        } catch (Exception err) {
            err.printStackTrace();
            throw new NullPointerException();
        }
        CreateCollums(table);
        SaveFormat = String.format("INSERT INTO '%s' ('user', '%s') VALUES ('%%s')", Table, (String.join("', '", Collums)));
    }

    private void CreateCollums(String table){
        for(String s:Collums){
            try {
                statmt.execute(String.format("SELECT %s FROM %s", s, table));
            } catch (SQLException e){
                try {
                    System.out.println(String.format("| YandeRe : add %s to table [%s]", s, table));
                    statmt.execute(String.format("ALTER TABLE %s ADD '%s' text", table, s));
                } catch (Exception er){
                    System.out.println("| YandeRe : SqLite critical error!");
                    er.printStackTrace();
                    throw new NullPointerException();
                }
            }
        }
    }

    public void removeData(String user) {
        try {
            statmt.execute(String.format("DELETE FROM '%s' WHERE user = '%s' ", Table, user));
        } catch (SQLException err) {
            //err.printStackTrace();
        }
    }

    @Override
    public List<String> getData(String user){

        try {
            ResultSet rawResult = statmt.executeQuery(String.format("SELECT * FROM %s WHERE user = '%s' ", Table, user));
            if(rawResult.isClosed())return null;
            List<String> result = new ArrayList<>();
            for(String c:Collums){
                result.add(rawResult.getString(c));
            }
            return result;
        } catch (SQLException err) {
            err.printStackTrace();
            return null;
        }
    }

    Config c = null;
    HashMap<String,List<String>> notSawed = null;
    @Override
    public void putData(String user, List<String> data){
        if(globalNetwork){
            Runnable task = () -> {
                try {
                    if(notSawed == null) {
                        notSawed = new HashMap<>();
                    }
                    notSawed.put(user, data);
                    saveData1(user, data);
                    notSawed.remove(user);
                    //if(notSawed.isEmpty())
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                    if(c==null){
                        c = new Config(localUrl+".hash");
                        c.set(user, data);
                        c.saveChanges();
                    }
                }
            };
            Thread thread = new Thread(task);
            thread.start();
        } else {
            try {
                saveData1(user, data);
            } catch (SQLException err) {
                err.printStackTrace();
            }
        }
    }


    private void saveData1(String user, List<String> data) throws SQLException {
        statmt.execute(String.format("DELETE FROM '%s' WHERE user = '%s' ", Table, user));
        data.add(0, user);
        statmt.execute(String.format( SaveFormat, (String.join("', '", data)) ));
    }
}