package plus.te4net.mds.auth;

import plus.te4net.mds.cap4a.Cap4a;
import plus.te4net.mds.cap4a.Cap4aLib;
import plus.te4net.mds.crypt.CryptoLibSimple;
import plus.te4net.mds.crypt.WebCryptKey;
import plus.te4net.utl.Logger;
import plus.te4net.utl.MailServer;
import plus.tson.utl.Tuple;
import plus.tson.utl.Te4HashMap;
import plus.tson.format.IVarGetter;
import plus.tson.format.TextFormatter;


public abstract class AuthService<T extends User> implements IAuthProvider {
    private final Cap4aLib<String> lib;
    private final SCryptoMap<String> keys = new SCryptoMap<>((short) 180);
    private final SCryptoMap<MailRequest> mails = new SCryptoMap<>((short) 420);
    private TextFormatter<IVarGetter<String>> mailFormat;
    private final UserMap<T> userMap;
    private final MailServer mailServer;

    public AuthService(UserMap<T> userMap, Cap4aLib<String> lib, MailServer server) {
        this.userMap = userMap;
        this.lib = lib;
        this.mailServer = server;
    }


    protected void serMailFormat(TextFormatter<IVarGetter<String>> mailFormat){
        this.mailFormat = mailFormat;
    }

    protected abstract boolean validLogin(String login);
    protected abstract boolean validPassword(String password);



    private static class SCryptoMap<T> extends CryptoLibSimple<WebCryptKey,T> {
        protected SCryptoMap(short skip) {
            super(skip);
        }

        @Override
        protected WebCryptKey newKey(long time, short usages) {
            return WebCryptKey.newKey(time, usages);
        }
    }


    protected void checkLogin(String login) throws AuthException {
        if(!validLogin(login))throw new AuthException(AuthException.Type.NO_VALID_LOGIN, login);
    }


    protected void checkPassword(String password) throws AuthException {
        if(!validPassword(password))throw new AuthException(AuthException.Type.NO_VALID_PASSWORD);
    }


    public Cap4a createAuthCap4a(String login) throws AuthException {
        checkLogin(login);
        return lib.createKey(90000, 1, login);
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


    public void createMailConfirm(String login, String password, MailRequest.Type type, String cryptoKey, MailServer.CallBack callBack) throws AuthException {
        checkLogin(login);
        if(password!=null) checkPassword(password);
        String loginCreator = keys.use(cryptoKey);
        if(loginCreator != null && loginCreator.equals(login)){
            if(type == MailRequest.Type.NEW_USER){
                if(userMap.hasUser(login))throw new AuthException(AuthException.Type.READY_EXIST);
            } else if(type == MailRequest.Type.BREAK_PASSWORD){
                if(!userMap.hasUser(login))throw new AuthException(AuthException.Type.NULL_USER);
            }
            WebCryptKey mailKey = mails.createKey(90000, 1, new MailRequest(login,password,type));
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


    private static final class Model implements IVarGetter<String>{
        private final String code;
        private final String login;
        private Model(String login, String code) {
            this.code = code;
            Logger.get().info("CODE: "+code);
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
                                          byte[] userData, byte[] tokenData) throws AuthException {
        checkLogin(login);
        Te4HashMap.Node<WebCryptKey, MailRequest> node = mails.getNode(mailConfirm);
        if(node==null)throw new AuthException(AuthException.Type.NO_MAIL);
        MailRequest request = node.getValue();
        if(request != null && request.type == MailRequest.Type.BREAK_PASSWORD && request.login.equals(login)){
            mails.use(mailConfirm);
            T user = userMap.getUserFromLogin(login);
            userMap.removeUserForLogin(login);
            user = userMap.newUser(login, request.password, user.getData());
            userMap.createUser(user, tokenData);
            return userMap.authUser(login, request.password, tokenData);
        } else {
            throw new AuthException(AuthException.Type.NO_MAIL);
        }
    }


    public Tuple<T, String> createAccount(String login, String mailConfirm,
                                          byte[] userData, byte[] tokenData) throws AuthException {
        checkLogin(login);
        Te4HashMap.Node<WebCryptKey, MailRequest> node = mails.getNode(mailConfirm);
        if(node==null)throw new AuthException(AuthException.Type.NO_MAIL);
        MailRequest request = node.getValue();
        if(request != null && request.type == MailRequest.Type.NEW_USER && request.login.equals(login)){
            mails.use(mailConfirm);
            T user = userMap.newUser(login, request.password, userData);
            userMap.createUser(user, tokenData);
            return userMap.authUser(login, request.password, tokenData);
        } else {
            throw new AuthException(AuthException.Type.NO_MAIL);
        }
    }


    public Tuple<T, String> createPerm(String login, String psw, byte[] userData, byte[] tokenData) throws AuthException {
        T user = userMap.newUser(login, psw, userData);
        userMap.createUser(user, tokenData);
        return userMap.authUser(login, psw, tokenData);
    }


    public static final class MailRequest{
        final String login, password;
        final Type type;
        MailRequest(String login, String password, Type type) {
            this.login = login;
            this.password = password;
            this.type = type;
        }
        public enum Type{
            NEW_USER, BREAK_PASSWORD
        }
    }
}