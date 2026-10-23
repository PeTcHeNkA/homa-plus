package plus.te4net.core.auth;

import plus.tson.utl.Tuple;
import java.util.ArrayList;


public interface IUserData<T extends User> {
    void updateUserData(T user);
    T getUserFromToken(String token, byte[] data);
    T getUserFromLogin(String login);
    T getUserFromName(String login);
    String createTokenFor(String user, byte[] data);
    void addUserInTable(T user);
    void removeUserInTable(String login);
    void removeToken(String token);
    ArrayList<Tuple<String,byte[]>> getTokensFor(String login);
    default void onCleanCache(){}
    T newUser(String login, String password, String name, byte[] data);
    default T editPassword(T user, String password) {
        return newUser(user.login, password, user.name, user.getData());
    }
    default T editName(T user, String newName) {
        return newUser(user.login, user.password, newName, user.getData());
    }
    void clearTokensFor(String login);
}