package plus.te4net.core.api;

import plus.te4net.core.auth.User;


public interface IAuthProvider {
    User auth(RequestSession session);


    void update(User user);


    void goToAuth(RequestSession session);


    default boolean checkAuth(RequestSession session) {
        User user = auth(session);
        if (user == null) {
            goToAuth(session);
            return false;
        }
        return true;
    }
}