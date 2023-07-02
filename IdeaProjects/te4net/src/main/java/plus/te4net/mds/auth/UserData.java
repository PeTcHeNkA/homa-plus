package plus.te4net.mds.auth;

import plus.te4net.utl.CharNum;
import plus.te4net.utl.data.SqliteUtils;
import plus.tson.utl.Tuple;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;


public abstract class UserData<T extends User> {
    private final CharNum charNum = new CharNum("0123456789qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM!#$%&*+-._|");
    public Connection connection;

    public UserData(String database){
        connection = SqliteUtils.newConnection(database);
        try {
            createTables();
        } catch (SQLException e){
            throw new RuntimeException(e);
        }
    }


    private void createTables() throws SQLException {
        String sqlCode = "CREATE TABLE IF NOT EXISTS USERS (LOGIN VARCHAR PRIMARY KEY, PASSWORD VARCHAR NOT NULL, DATA MEDIUMTEXT);";
        String sqlCode2 = "CREATE TABLE IF NOT EXISTS TOKENS (TOKEN VARCHAR(42) PRIMARY KEY, LOGIN VARCHAR REFERENCES USERS (LOGIN) ON DELETE CASCADE, DATA MEDIUMTEXT, TIME INTEGER NOT NULL);";
        runSql(sqlCode);
        runSql(sqlCode2);
    }


    final void updateUserData(T user) throws SQLException {
        String sqlCode = "UPDATE USERS SET PASSWORD = '"+user.password+"', DATA = ?";
        runSqlWithBytes(sqlCode, user.getData());
    }


    final void addUserInTable(T user) throws SQLException {
        String sqlCode = "INSERT INTO USERS (LOGIN, PASSWORD, DATA) VALUES ('"+
                user.login+"', '"+user.password+"', ?)";
        runSqlWithBytes(sqlCode, user.getData());
    }


    final void removeUserInTable(String login) throws SQLException {
        runSql("DELETE FROM USERS WHERE LOGIN = '"+login+"'");
    }


    final void clearTokensFor(String login) throws SQLException {
        runSql("DELETE FROM TOKENS WHERE LOGIN = '"+login+"'");
    }


    final void clearOldTokens(long time) throws SQLException {
        runSql("DELETE FROM TOKENS WHERE TIME <= "+((int)(time>>14)));
    }


    final String createTokenFor(String user, byte[] data) throws SQLException {
        String token = new String(charNum.genCode(42));
        String sqlCode = "INSERT INTO TOKENS (TOKEN, LOGIN, DATA, TIME) VALUES ('"
                +token+"', '"+user+"', ?, '"+((int)(System.currentTimeMillis()>>14))+"')";
        runSqlWithBytes(sqlCode, data);
        return token;
    }


    final void removeToken(String token) throws SQLException {
        runSql("DELETE FROM TOKENS WHERE TOKEN = '"+token+"'");
    }


    final ArrayList<Tuple<String,byte[]>> getTokensFor(String login) throws SQLException {
        String sqlCode = "SELECT TOKEN, DATA FROM TOKENS WHERE LOGIN = '"+login+"'";
        PreparedStatement ps = connection.prepareStatement(sqlCode);
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
            ps.close();
            if(result!=null) {
                result.close();
            }
            throw new SQLException(e);
        }
    }


    //добавить проверку на устаревшый токен
    final T getUserFromToken(String token, byte[] data) throws SQLException {
        String sqlCode = "SELECT LOGIN, PASSWORD, DATA FROM USERS WHERE LOGIN = " +
                "(SELECT LOGIN FROM TOKENS WHERE TOKEN = '"+token+"' and DATA = ?)";
        PreparedStatement ps = connection.prepareStatement(sqlCode);
        ps.setBytes(1, data);
        ResultSet result = null;
        T user = null;
        try {
            result = ps.executeQuery();
            if(result.next()) {
                user = newUser(
                        result.getString(1),
                        result.getString(2),
                        result.getBytes(3)
                );
            }
            result.close();
            ps.close();
        } catch (Exception e){
            ps.close();
            if(result!=null) {
                result.close();
            }
            throw new SQLException(e);
        }
        return user;
    }


    final T getUserFromLogin(String login) throws SQLException {
        String sqlCode = "SELECT PASSWORD, DATA FROM USERS WHERE LOGIN = '" + login + "'";
        AtomicReference<T> user = new AtomicReference<>();
        runSql(sqlCode, result -> {
            if(!result.next())return;
            user.set(newUser(
                    login,
                    result.getString(1),
                    result.getBytes(2)
            ));
        });
        return user.get();
    }


    interface CallBack{
        void onResult(ResultSet result) throws SQLException;
    }


    final void runSql(String code, CallBack c) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(code);
        ResultSet result = statement.executeQuery();
        try {
            c.onResult(result);
            result.close();
            statement.close();
        } catch (Exception e){
            result.close();
            statement.close();
            throw new SQLException(e);
        }
    }


    final void runSqlWithBytes(String sqlCode, byte[] data) throws SQLException {
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


    final void runSql(String code) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(code);
        statement.execute();
        statement.close();
    }


    protected abstract T newUser(String login, String password, byte[] data);
}
