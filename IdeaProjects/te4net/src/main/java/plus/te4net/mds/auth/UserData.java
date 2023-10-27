package plus.te4net.mds.auth;

import plus.te4net.utl.CharNum;
import plus.te4net.utl.data.SqliteUtils;
import plus.tson.utl.Tuple;

import java.sql.*;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;


public abstract class UserData<T extends User> implements SqliteUtils, IUserData<T>{
    private final CharNum charNum = new CharNum("0123456789qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM!#$%&*+-._|");
    public Connection connection;

    @Override
    public Connection getConnection() {
        return connection;
    }

    public UserData(String database){
        connection = SqliteUtils.newConnection(database);
        try {
            createTables();
        } catch (SQLException e){
            throw new RuntimeException(e);
        }
    }


    protected void createTables() throws SQLException {
        String sqlCode = "CREATE TABLE IF NOT EXISTS USERS (LOGIN VARCHAR PRIMARY KEY, PASSWORD VARCHAR NOT NULL, NAME VARCHAR(16), DATA MEDIUMTEXT);";
        String sqlCode2 = "CREATE TABLE IF NOT EXISTS TOKENS (TOKEN VARCHAR(64) PRIMARY KEY, LOGIN VARCHAR REFERENCES USERS (LOGIN) ON DELETE CASCADE, DATA MEDIUMTEXT, TIME INTEGER NOT NULL);";
        runSql(sqlCode);
        runSql(sqlCode2);
    }


//    public Selector select(String... items){
//        return new Selector(items);
//    }
//    public class Selector {
//        private String tableFrom = USERS;
//        private String whereCode = null;
//        private final String[] who;
//        public Selector(String... who) {
//            this.who = who;
//        }
//
//        public Selector from(String table){
//            tableFrom = table;
//            return this;
//        }
//        public Selector where(String code){
//            whereCode = code;
//            return this;
//        }
//        public Selector whereEq(String column, String value){
//            whereCode = "'"+column+"' = '"+value+"'";
//            return this;
//        }
//        public TsonList run() throws SQLException{
//            String code = "SELECT "+(who==null?"*":("'"+String.join("', '",who)))
//                    +"' FROM '"+tableFrom;
//            if(whereCode == null){
//                code = code + "';";
//            } else {
//                code = code + "' WHERE "+whereCode;
//            }
//            TsonList lines = new TsonList();
//            runSql(code, result -> {
//                do {
//                    TsonList line = new TsonList(who.length);
//                    for (int i = 0; i < who.length; i++) {
//                        Object o = result.getObject(i);
//                        if (o instanceof String) {
//                            line.add((String) o);
//                        } else if (o instanceof Number) {
//                            line.add(((Number) o).doubleValue());
//                        } else {
//                            line.add(new TsonField<>(o));
//                        }
//                    }
//                    lines.add(line);
//                } while (result.next());
//            });
//            return lines;
//        }
//    }


    @Override
    public void updateUserData(T user) {
        String sqlCode = "UPDATE USERS SET PASSWORD = '"+user.password+"', DATA = ?";
        try {
            runSqlWithBytes(sqlCode, user.getData());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    @Override
    public final void addUserInTable(T user) {
        String sqlCode = "INSERT INTO USERS (LOGIN, PASSWORD, DATA) VALUES ('"+
                user.login+"', '"+user.password+"', ?)";
        try {
            runSqlWithBytes(sqlCode, user.getData());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public final void removeUserInTable(String login) {
        try {
            runSql("DELETE FROM USERS WHERE LOGIN = '"+login+"'");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public final void clearTokensFor(String login) {
        try {
            runSql("DELETE FROM TOKENS WHERE LOGIN = '"+login+"'");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    final void clearOldTokens(long time) throws SQLException {
        runSql("DELETE FROM TOKENS WHERE TIME <= "+((int)(time>>14)));
    }


    @Override
    public final String createTokenFor(String user, byte[] data){
        String token = new String(charNum.genCode(64));
        String sqlCode = "INSERT INTO TOKENS (TOKEN, LOGIN, DATA, TIME) VALUES ('"
                +token+"', '"+user+"', ?, '"+((int)(System.currentTimeMillis()>>14))+"')";
        try {
            runSqlWithBytes(sqlCode, data);
        } catch (SQLException e) {
            return null;
        }
        return token;
    }


    @Override
    public final void removeToken(String token) {
        try {
            runSql("DELETE FROM TOKENS WHERE TOKEN = '"+token+"'");
        } catch (SQLException ignored) {}
    }


    @Override
    public final ArrayList<Tuple<String,byte[]>> getTokensFor(String login) {
        String sqlCode = "SELECT TOKEN, DATA FROM TOKENS WHERE LOGIN = '"+login+"'";
        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement(sqlCode);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        ResultSet result = null;
        try {
            result = ps.executeQuery();
            ArrayList<Tuple<String,byte[]>> tokens = new ArrayList<>();
            if(result.next()) {
                tokens.add(new Tuple<>(
                        result.getString(1),
                        result.getBytes(2)
                ));
            }
            result.close();
            ps.close();
            return tokens;
        } catch (Exception e){
            try {
                ps.close();
                if(result!=null) {
                    result.close();
                }
            } catch (SQLException e2){
                e2.printStackTrace();
            }
        }
        return new ArrayList<>();
    }


    //добавить проверку на устаревшый токен
    @Override
    public T getUserFromToken(String token, byte[] data) {
        String sqlCode = "SELECT LOGIN, PASSWORD, NAME, DATA FROM USERS WHERE LOGIN = " +
                "(SELECT LOGIN FROM TOKENS WHERE TOKEN = '"+token+"' and DATA = ?)";
        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement(sqlCode);
            ps.setBytes(1, data);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        ResultSet result = null;
        T user = null;
        try {
            result = ps.executeQuery();
            if(result.next()) {
                user = newUser(
                        result.getString(1),
                        result.getString(2),
                        result.getString(3),
                        result.getBytes(3)
                );
            }
            result.close();
            ps.close();
        } catch (Exception e){
            try {
                ps.close();
                if (result != null) {
                    result.close();
                }
            } catch (SQLException ignored){}
        }
        return user;
    }


    @Override
    public T getUserFromName(String name) {
        String sqlCode = "SELECT LOGIN, PASSWORD, DATA FROM USERS WHERE NAME = '" + name + "'";
        AtomicReference<T> user = new AtomicReference<>();
        try {
            runSql(sqlCode, result -> {
                if(!result.next())return;
                user.set(newUser(
                        result.getString(1),
                        result.getString(2),
                        name,
                        result.getBytes(3)
                ));
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user.get();
    }


    @Override
    public T getUserFromLogin(String login) {
        String sqlCode = "SELECT PASSWORD, NAME, DATA FROM USERS WHERE LOGIN = '" + login + "'";
        AtomicReference<T> user = new AtomicReference<>();
        try {
            runSql(sqlCode, result -> {
                if(!result.next())return;
                user.set(newUser(
                        login,
                        result.getString(1),
                        result.getString(2),
                        result.getBytes(3)
                ));
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user.get();
    }


    protected final void runSqlWithBytes(String sqlCode, byte[] data) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(sqlCode);
        ps.setBytes(1, data);
        try {
            ps.execute();
            ps.close();
        } catch (Exception e){
            ps.close();
            throw new SQLException(e);
        }
    }
}