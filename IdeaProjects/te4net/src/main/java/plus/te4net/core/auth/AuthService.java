package plus.te4net.core.auth;

import plus.te4net.core.api.IAuthProvider;
import plus.te4net.core.node.HEWorker;
import plus.te4net.mds.cap4a.Cap4a;
import plus.te4net.mds.cap4a.Cap4aLib;
import plus.te4net.mds.crypt.CryptoLibSimple;
import plus.te4net.mds.crypt.WebCryptKey;
import plus.te4net.mds.util.MailServer;
import plus.tson.utl.Tuple;
import plus.tson.utl.Te4HashMap;
import plus.tson.format.IVarGetter;
import plus.tson.format.TextFormatter;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;


public abstract class AuthService<T extends User> implements IAuthProvider {
    private final Cap4aLib<String> lib;
    private final SCryptoMap<String> keys;
    private final SCryptoMap<MailRequest> mails;
    private TextFormatter<IVarGetter<String>> mailFormat;
    protected final UserMap<T> userMap;
    private final MailServer mailServer;

    public AuthService(ScheduledExecutorService service, UserMap<T> userMap, Cap4aLib<String> lib, MailServer server) {
        this.userMap = userMap;
        this.lib = lib;
        this.mailServer = server;
        keys = new SCryptoMap<>(service, 180_000);
        mails = new SCryptoMap<>(service, 600_000);
    }


    protected void setMailFormat(TextFormatter<IVarGetter<String>> mailFormat){
        this.mailFormat = mailFormat;
    }


    protected boolean validLogin(String login) {
        return AuthDefValidator.validLogin(login);
    }


    protected boolean validPassword(String password) {
        return AuthDefValidator.validPassword(password);
    }


    protected boolean validName(String name){
        return AuthDefValidator.validName(name);
    }


    private static class SCryptoMap<T> extends CryptoLibSimple<WebCryptKey,T> {
        protected SCryptoMap(ScheduledExecutorService service, int skip) {
            super(service, skip);
        }

        @Override
        protected WebCryptKey newKey(long time, short usages) {
            return WebCryptKey.newKey(time, usages);
        }
    }


    protected void checkLogin(String login) throws AuthException {
        if(!validLogin(login))throw new AuthException(AuthException.Type.NO_VALID_LOGIN, login);
    }

    
    protected void checkName(String name) throws AuthException {
        if(name == null || !validName(name))throw new AuthException(AuthException.Type.NO_VALID_NAME, name);
    }


    protected void checkPassword(String password) throws AuthException {
        if(!validPassword(password))throw new AuthException(AuthException.Type.NO_VALID_PASSWORD);
    }


    public Cap4a createAuthCap4a(String login) throws AuthException {
        checkLogin(login);
        return lib.createKey(90000, 5, login);
    }


    public WebCryptKey activateCap4a(String login, String cap4aUID, String cap4aAnswer) throws AuthException {
        checkLogin(login);
        Te4HashMap.Node<Cap4a, String> node = lib.getNode(cap4aUID);
        if(node!=null && node.getValue().equals(login) && node.getKey().check(cap4aAnswer)){
            return keys.createKey(90000, 1, login);
        }
        throw new AuthException(AuthException.Type.NO_CAP4A);
    }


    public Cap4a getCap4a(String cap4aUID) {
        return lib.getKey(cap4aUID);
    }


    public Cap4a getCap4a(String login, String cap4aUID) throws AuthException {
        checkLogin(login);
        Te4HashMap.Node<Cap4a, String> node = lib.getNode(cap4aUID);
        if(node!=null && node.getValue().equals(login)){
            return node.getKey();
        }
        return null;
    }


    public T auth(String token, byte[] tokenData){
        return userMap.getUser(token, tokenData);
    }


    public void logout(String token, byte[] tokenData){
        userMap.removeToken(token, tokenData);
    }


    public Tuple<T, String> auth(String login, String password, String cryptoKey, byte[] tokenData) throws AuthException {
        checkLogin(login);
        checkPassword(password);
        String loginCreator = keys.use(cryptoKey);
        if(loginCreator != null && loginCreator.equals(login)){
            return userMap.authUser(login, password, tokenData);
        } else {
            throw new AuthException(AuthException.Type.NO_CAP4A);
        }
    }


    public Tuple<T, String> authNoKey(String login, String password, byte[] tokenData) throws AuthException {
        checkLogin(login);
        checkPassword(password);
        return userMap.authUser(login, password, tokenData);
    }


    public void createMailConfirm(String login, String name, String password, MailRequest.Type type, String cryptoKey, MailServer.CallBack callBack) throws AuthException {
        checkLogin(login);
        if(password!=null) checkPassword(password);
        String loginCreator = keys.use(cryptoKey);
        if(loginCreator != null && loginCreator.equals(login)){
            if(type == MailRequest.Type.NEW_USER){
                if(userMap.hasUser(login))throw new AuthException(AuthException.Type.READY_EXIST);
                if(userMap.hasName(name))throw new AuthException(AuthException.Type.READY_EXIST_NAME);
            } else if(type == MailRequest.Type.BREAK_PASSWORD){
                T user = userMap.getUserFromLogin(login);
                if(user==null||!Objects.equals(user.name,name))
                    throw new AuthException(AuthException.Type.NULL_USER);
            }
            WebCryptKey mailKey = mails.createKey(90000, 1, new MailRequest(login, password, name, type));
            mailServer.send(
                    login,
                    mailFormat.get(new Model(login, mailKey.toString())),
                    callBack
            );
        } else throw new AuthException(AuthException.Type.NO_CAP4A);
    }


    public boolean hasUser(String login, String cryptKey) throws AuthException {
        checkLogin(login);
        if(!keys.hasKey(cryptKey))throw new AuthException(AuthException.Type.NO_CAP4A);
        return userMap.hasUser(login);
    }


    public boolean hasName(String name, String cryptKey) throws AuthException {
        checkName(name);
        if(!keys.hasKey(cryptKey))throw new AuthException(AuthException.Type.NO_CAP4A);
        return userMap.hasName(name);
    }


    static final class Model implements IVarGetter<String>{
        private final String code;
        private final String login;
        private Model(String login, String code) {
            this.code = code;
            HEWorker.getLogger().info("CODE: "+code);
            this.login = login;
        }
        @Override
        public String get(String s) {
            switch (s) {
                case "code": return code;
                case "login": return login;
                default: return null;
            }
        }
    }


    public Tuple<T, String> editPsw(String login, String mailConfirm,
                                    byte[] tokenData) throws AuthException {
        checkLogin(login);
        Te4HashMap.Node<WebCryptKey, MailRequest> node = mails.getNode(mailConfirm);
        if(node==null)throw new AuthException(AuthException.Type.NO_MAIL);
        MailRequest request = node.getValue();
        if(request != null && request.type == MailRequest.Type.BREAK_PASSWORD && request.login.equals(login)){
            mails.use(mailConfirm);
            T user = userMap.getUserFromLogin(login);
            userMap.removeUserForLogin(login);
            user = userMap.editPassword(user, request.password);
            userMap.createUser(user);
            return userMap.authUser(login, request.password, tokenData);
        } else {
            throw new AuthException(AuthException.Type.NO_MAIL);
        }
    }


    public Tuple<T, String> createAccount(String login, String name, String mailConfirm,
                                          byte[] userData, byte[] tokenData) throws AuthException {
        checkLogin(login);
        Te4HashMap.Node<WebCryptKey, MailRequest> node = mails.getNode(mailConfirm);
        if(node==null)throw new AuthException(AuthException.Type.NO_MAIL);
        MailRequest request = node.getValue();
        if(request != null && request.type == MailRequest.Type.NEW_USER && request.login.equals(login)){
            mails.use(mailConfirm);

            if(name==null){
                name = request.name;
                if(name==null) throw new AuthException(AuthException.Type.NO_VALID_NAME);
            } else if(!Objects.equals(name, request.name))
                throw new AuthException(AuthException.Type.NO_VALID_NAME);
            checkName(name);

            T user = userMap.newUser(login, request.password, name, userData);
            userMap.createUser(user);
            return userMap.authUser(login, request.password, tokenData);
        } else {
            throw new AuthException(AuthException.Type.NO_MAIL);
        }
    }


    public Tuple<T, String> createPerm(String login, String name, String psw, byte[] userData, byte[] tokenData) throws AuthException {
        T user = userMap.newUser(login, psw, name, userData);
        userMap.createUser(user);
        return userMap.authUser(login, psw, tokenData);
    }


    public void update(User user) {
        userMap.update((T) user);
    }


    public static final class MailRequest{
        final String login, password, name;
        final Type type;
        MailRequest(String login, String password, String name, Type type) {
            this.login = login;
            this.password = password;
            this.name = name;
            this.type = type;
        }
        public enum Type{
            NEW_USER, BREAK_PASSWORD
        }
    }
}