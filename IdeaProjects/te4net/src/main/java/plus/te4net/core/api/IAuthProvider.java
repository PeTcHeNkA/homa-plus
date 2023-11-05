package plus.te4net.core.api;

import plus.te4net.core.auth.User;


public interface IAuthProvider {
    User auth(IRequestSession session);


    void update(User user);


    void goToAuth(IRequestSession session);


    default boolean checkAuth(IRequestSession session) {
        User user = auth(session);
        if (user == null) {
            goToAuth(session);
            return false;
        }
        return true;
    }
}