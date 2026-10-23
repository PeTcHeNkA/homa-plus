package plus.te4net.core.auth;

import java.util.ArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import plus.asyutl.shoulder.IBasicTask;
import plus.te4net.core.node.HEWorker;
import plus.tson.utl.Tuple;
import plus.tson.utl.Te4HashMap;


public class UserMap<T extends User> extends Te4HashMap<TokenInfo, T> implements Runnable {
    protected final IUserData<T> users;
    private final ScheduledExecutorService service;
    private ScheduledFuture<?> future;

    public UserMap(ScheduledExecutorService service, UserData<T> userData){
        this.service = service;
        this.users = userData;
    }


    private void restartIfNeed(){
        if(future == null)future = service.scheduleAtFixedRate(this, 10_000, 10_000, TimeUnit.MILLISECONDS);
    }


    public void update(T user){
        users.updateUserData(user);
    }


    Tuple<T, String> authUser(String login, String password, byte[] data) throws AuthException {
        T user = users.getUserFromLogin(login);
        if (user == null) throw new AuthException(AuthException.Type.NULL_USER);
        if (!user.password.equals(password)) throw new AuthException(AuthException.Type.WRONG_PASSWORD);
        String token = users.createTokenFor(login, data);
        if (token == null) {
            throw new AuthException(AuthException.Type.SQL);
        }
        fput(new TokenIKey(token, data), user);
        restartIfNeed();
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


    boolean hasName(String name){
        return users.getUserFromName(name) != null;
    }


    T getUser(String token, byte[] data){
        Node<TokenInfo,T> node = getNode(new TokenInfo(token, data));
        if(node==null){
            T user = getUserFromTable(token, data);
            if(user != null) {
                fput(new TokenIKey(token, data), user);
                restartIfNeed();
            }
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
        } else if (users.getUserFromToken(token, data) != null) users.removeToken(token);
    }


    T getUserFromTable(String token, byte[] data) {
        return users.getUserFromToken(token, data);
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
        if(isEmpty() && future != null){
            future.cancel(true);
            future = null;
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