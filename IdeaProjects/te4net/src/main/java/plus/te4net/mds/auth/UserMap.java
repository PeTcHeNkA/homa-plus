package plus.te4net.mds.auth;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import plus.asyutl.shoulder.IBasicTask;
import plus.te4net.core.node.HWorkerRepository;
import plus.tson.utl.Tuple;
import plus.tson.utl.Te4HashMap;


public class UserMap<T extends User> extends Te4HashMap<TokenInfo, T> implements IBasicTask {
    private final UserData<T> users;
    private int cd = 0;

    public UserMap(UserData<T> userData){
        this.users = userData;
        HWorkerRepository.shoulder.addTask(this);
    }


    void update(T user){
        try {
            users.updateUserData(user);
        } catch (SQLException e){
            e.printStackTrace();
        }
    }


    Tuple<T, String> authUser(String login, String password, byte[] data) throws AuthException {
        try {
            T user = users.getUserFromLogin(login);
            if(user==null)throw new AuthException(AuthException.Type.NULL_USER);
            if(!user.password.equals(password))throw new AuthException(AuthException.Type.WRONG_PASSWORD, user);
            String token = users.createTokenFor(login, data);
            put(new TokenIKey(token, data), user);
            return new Tuple<>(user, token);
        } catch (SQLException e){
            e.printStackTrace();
            throw new AuthException(AuthException.Type.SQL);
        }
    }


    //создает записи в таблице для пользователя
    String createUser(T user, byte[] tokenData){
        try {
            users.addUserInTable(user);
            String token = users.createTokenFor(user.login, tokenData);
            put(new TokenIKey(token, tokenData), user);
            return token;
        } catch (SQLException e){
            throw new RuntimeException(e);
        }
    }


    T getUserFromLogin(String login){
        try {
            return users.getUserFromLogin(login);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }


    void removeUserForLogin(String login){
        try {
            ArrayList<Tuple<String, byte[]>> tokens = getTokensFor(login);
            users.removeUserInTable(login);
            for (Tuple<String, byte[]> token : tokens) {
                remove(new TokenInfo(token.A, token.B));
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
    }


    boolean hasUser(String login){
        try {
            return users.getUserFromLogin(login) != null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    T getUser(String token, byte[] data){
        Node<TokenInfo,T> node = getNode(new TokenInfo(token, data));
        if(node==null){
            T user = getUserFromTable(token, data);
            if(user != null) put(new TokenIKey(token, data), user);
            return user;
        } else {
            node.getKey().use();
            return node.getValue();
        }
    }


    void clearUserTokens(String login){
        try {
            for (Tuple<String, byte[]> token : getTokensFor(login)) {
                TokenInfo key = new TokenInfo(token.A, token.B);
                users.removeToken(token.A);
                remove(key);
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
    }


    ArrayList<Tuple<String,byte[]>> getTokensFor(String login){
        try {
            return users.getTokensFor(login);
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }


    void removeToken(String token, byte[] data){
        TokenInfo key = new TokenInfo(token, data);
        try {
            if (containsKey(key)) {
                users.removeToken(token);
                remove(key);
            } else {
                if (users.getUserFromToken(token, data) != null) users.removeToken(token);
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
    }


    T getUserFromTable(String token, byte[] data) {
        try {
            return users.getUserFromToken(token, data);
        } catch (SQLException e){
            e.printStackTrace();
            return null;
        }
    }


    @Override
    public boolean needRun() {
        if(cd++>180){
            cd = 0;
            clearOldUsers();
        }
        return false;
    }


    public void clearOldUsers(){
        long time = System.currentTimeMillis()-(1000*60*5);
        for(TokenInfo key:keysToArray(new TokenInfo[size()])){
            if (key.getUsages()<time) {
                int hash = hash(key);
                removeNode(hash, key, null, false, true);
            }
        }
    }


    @Override
    public void run() {}


    T newUser(String login, String password, byte[] data){
        return users.newUser(login, password, data);
    }
}


final class TokenIKey extends TokenInfo{
    private long lastUsage = System.currentTimeMillis();
    TokenIKey(String token, byte[] data) {
        super(token, data);
    }

    long getUsages(){
        return lastUsage;
    }


    void use(){
        lastUsage = System.currentTimeMillis();
    };
}


class TokenInfo{
    private final String token;
    private final byte[] data;

    TokenInfo(String token, byte[] data) {
        this.token = token;
        this.data = data;
    }


    @Override
    public final boolean equals(Object o) {
        if (o instanceof TokenInfo) {
            TokenInfo tokenInfo = (TokenInfo) o;
            return token.equals(tokenInfo.token) && Arrays.equals(data, tokenInfo.data);
        } else return false;
    }


    @Override
    public final int hashCode() {
        return token.hashCode();
    }


    long getUsages(){
        return -1;
    }


    void use(){};
}