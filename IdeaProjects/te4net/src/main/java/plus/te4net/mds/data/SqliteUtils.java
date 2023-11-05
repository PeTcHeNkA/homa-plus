package plus.te4net.mds.data;

import java.sql.*;
import java.util.ArrayList;
import static plus.te4net.core.node.HServer.preLoad;


public interface SqliteUtils{
    static boolean IS_H2 = false;


    default Connection createConnection(String database){
        return newConnection(database);
    }


    static Connection newConnection(String database){
        Connection conn;
        if(IS_H2){
            if(!database.startsWith(".") || !database.startsWith("./")){
                database = "./"+database;
            }
        }
        try {
            if(IS_H2) {
                Class.forName("org.h2.Driver");
                conn = DriverManager.getConnection("jdbc:h2:"+database);
            } else {
                Class.forName("org.sqlite.JDBC");
                conn = DriverManager.getConnection("jdbc:sqlite:"+database);
            }
        } catch (Exception e){
            throw new RuntimeException(e);
        }
        return conn;
    }


    Connection getConnection();


    default void createTable(String name) {
        try {
            Statement statmt = getConnection().createStatement();
            if(SqliteUtils.IS_H2) {
                statmt.execute("CREATE TABLE IF NOT EXISTS \"" + name + "\" " +
                        "(\"NAME\" VARCHAR, \"DATA\" VARBINARY, PRIMARY KEY(NAME))");
            } else {
                statmt.execute("CREATE TABLE IF NOT EXISTS \""+name+"\" " +
                        "(\"NAME\" VARCHAR PRIMARY KEY, \"DATA\" MEDIUMTEXT)");
            }
            statmt.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    default void removeData(String name, String table) {
        try {
            String qw2 = "DELETE FROM '" + table + "' WHERE NAME = '" + name + "'";
            PreparedStatement st2 = getConnection().prepareStatement(qw2);
            st2.execute();
            st2.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }


    default void saveData(String name, String table, byte[] data) {
        try {
            Connection conn = getConnection();
            if(SqliteUtils.IS_H2) {
                String qw2 = "DELETE FROM '" + table +"' WHERE NAME = '"+name+"'";
                PreparedStatement st2 = conn.prepareStatement(qw2);
                st2.execute();
                st2.close();
                String qw = "INSERT INTO '" + table + "' VALUES (?, ?)";
                PreparedStatement ps = conn.prepareStatement(qw);
                ps.setNString(1, name);
                ps.setBytes(2, data);
                ps.execute();
                ps.close();
            } else {
                String qw = "INSERT OR REPLACE INTO '"+table+"' (NAME,DATA) VALUES ('"+name+"', ?)";
                PreparedStatement ps = conn.prepareStatement(qw);
                ps.setBytes(1, data);
                ps.execute();
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }


    default String[] getKeys(String table){
        try {
            String qw = "SELECT NAME FROM '" + table + "'";
            PreparedStatement ps = getConnection().prepareStatement(qw);
            ResultSet result = ps.executeQuery();
            ArrayList<String> arr = new ArrayList<>();
            while (result.next()) {
                arr.add(result.getString(1));
            }
            ps.close();
            return arr.toArray(new String[0]);
        } catch (Exception e){
            e.printStackTrace();
            return new String[0];
        }
    }


    default byte[] getData(String name, String table){
        try {
            String qw = "SELECT DATA FROM '" + table + "' WHERE NAME = '" + name + "'";
            PreparedStatement ps = getConnection().prepareStatement(qw);
            ResultSet result = ps.executeQuery();
            if(result.next()) {
                byte[] data = result.getBytes(1);
                ps.close();
                return data;
            } else {
                ps.close();
                return null;
            }
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }


    default void runSql(String code, SqlCallBack c) throws SQLException {
        PreparedStatement statement = getConnection().prepareStatement(code);
        ResultSet result = statement.executeQuery();
        try {
            //result.next();
            c.onResult(result);
            result.close();
            statement.close();
        } catch (Exception e){
            result.close();
            statement.close();
            throw new SQLException(e);
        }
    }


    default void runSql(String code) throws SQLException {
        PreparedStatement statement = getConnection().prepareStatement(code);
        statement.execute();
        statement.close();
    }


    default void addColumn(String table, String column, SqlType type, boolean index) throws SQLException {
        runSql(
                "ALTER TABLE '"+table+"' ADD '"+column+"' "+type+";"
        );
        if(index){
            runSql(
                    "CREATE INDEX '"+column+"_index' ON '"+table+"' ('"+column+"');"
            );
        }
    }
}