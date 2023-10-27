package plus.te4net.mds.auth;

import java.util.ArrayList;
import plus.asyutl.shoulder.IBasicTask;
import plus.te4net.core.node.HWorkerRepository;
import plus.tson.utl.Tuple;
import plus.tson.utl.Te4HashMap;


public class UserMap<T extends User> extends Te4HashMap<TokenInfo, T> implements IBasicTask {
    protected final IUserData<T> users;
    protected int cd = 0;

    public UserMap(UserData<T> userData){
        this.users = userData;
        HWorkerRepository.shoulder.addTask(this);
    }


    public void update(T user){
        users.updateUserData(user);
    }


    Tuple<T, String> authUser(String login, String password, byte[] data) throws AuthException {
        T user = users.getUserFromLogin(login);
        if (user == null) throw new AuthException(AuthException.Type.NULL_USER);
        if (!user.password.equals(password)) throw new AuthException(AuthException.Type.WRONG_PASSWORD, user);
        String token = users.createTokenFor(login, data);
        if (token == null) {
            throw new AuthException(AuthException.Type.SQL);
        }
        put(new TokenIKey(token, data), user);
        return new Tuple<>(user, token);
    }


    //создает записи в таблице для пользователя
    void createUser(T user){
        users.addUserInTable(user);
    }


    T getUserFromLogin(String login){
        return users.getUserFromLogin(login);
    }


    void removeUserForLogin(String login) {
        ArrayList<Tuple<String, byte[]>> tokens = users.getTokensFor(login);
        users.removeUserInTable(login);
        for (Tuple<String, byte[]> token : tokens) {
            remove(new TokenInfo(token.A, token.B));
        }
    }


    boolean hasUser(String login){
        return users.getUserFromLogin(login) != null;
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


    void clearUserTokens(String login) {
        for (Tuple<String, byte[]> token : users.getTokensFor(login)) {
            TokenInfo key = new TokenInfo(token.A, token.B);
            remove(key);
        }
        users.clearTokensFor(login);
    }


    void removeToken(String token, byte[] data) {
        TokenInfo key = new TokenInfo(token, data);
        if (containsKey(key)) {
            users.removeToken(token);
            remove(key);
        } else {
            if (users.getUserFromToken(token, data) != null) users.removeToken(token);
        }
    }


    T getUserFromTable(String token, byte[] data) {
        return users.getUserFromToken(token, data);
    }


    @Override
    public boolean needRun() {
        if(cd++>180){
            cd = 0;
            run();
            users.onCleanCache();
        }
        return false;
    }


    @Override
    public void run() {
        long time = System.currentTimeMillis()-(1000*60*5);
        for(TokenInfo key:keysToArray(new TokenInfo[size()])){
            if (key.getUsages()<time) {
                int hash = hash(key);
                removeNode(hash, key, null, false, true);
            }
        }
    }


    T newUser(String login, String password, String name, byte[] data){
        return users.newUser(login, password, name, data);
    }


    T editPassword(T user, String password){
        return users.editPassword(user, password);
    }


    T editName(T user, String newName){
        return users.editName(user, newName);
    }
}