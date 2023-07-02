package plus.te4net.mds.auth;

import plus.te4net.core.api.IRequestSession;


public interface IAuthProvider {
    User auth(IRequestSession session);


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