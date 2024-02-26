package plus.te4net.mds.proc;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import plus.te4net.core.api.IAuthProvider;
import plus.te4net.core.api.IRequestProcessor;
import plus.te4net.core.api.RequestSession;
import plus.te4net.core.auth.*;
import plus.te4net.mds.html.vr.VrCss;
import plus.te4net.mds.html.vr.VrHtmlFormat;
import plus.te4net.core.node.HEWorker;
import plus.te4net.mds.cap4a.Cap4a;
import plus.te4net.mds.cap4a.Cap4aLib;
import plus.te4net.mds.crypt.CryptException;
import plus.te4net.mds.crypt.WebCryptKey;
import plus.te4net.mds.util.TextMapFormatter;
import plus.te4net.mds.util.MailServer;
import plus.te4net.mds.util.MiniLink;
import plus.tson.TsonMap;
import plus.tson.TsonStr;
import plus.tson.utl.Tuple;
import plus.util.SimpleSrcGetter;
import java.nio.charset.StandardCharsets;


public class AuthRoute<U extends User> extends AuthService<U> implements SimpleSrcGetter, IRequestProcessor, IAuthProvider {
    public static final byte[] font = SimpleSrcGetter.getSrc(AuthRoute.class, "Bufcated1.ttf");
    public static final String defCss = new VrCss(
            new String(SimpleSrcGetter.getSrc(AuthRoute.class,"cap4a-template.css"))
    ).toString();

    private final IRequestProcessor fmLogin;
    private final HEWorker worker;

    public AuthRoute(HEWorker worker, TsonMap config){
        this(worker, new UserMap<>(worker.pool(), (UserData<U>) new DefData(config.getOrDefault(
                        "database", new TsonStr(worker.getWorkDir().getPath() + "/users.db")
                ).getStr())),
                config.getOrDefault("mail",new TsonMap("{virtual=(true)}")).getMap()
        );
    }


    public AuthRoute(HEWorker worker, UserMap<U> userMap, TsonMap mailAuth){
        super(worker.pool(), userMap, new Cap4aLib<>(worker.pool()), new MailServer(worker.pool(), mailAuth.getMap("mail")));
        setMailFormat(new TextMapFormatter(getStrSrc("mail-code-template.html"),"{%","%}"));
        fmLogin = VrHtmlFormat.ofHtml(worker, getStrSrc("mcauth2.html"));
        this.worker = worker;// mcauth2.html  login-template.html
        onInit(worker);
    }


    protected void onInit(HEWorker worker){
        worker.addRoute("auth", this);
    }


    static class DefData extends UserData<User>{
        public DefData(String database) {
            super(database);
        }
        @Override
        public User newUser(String login, String password, String name, byte[] data) {
            return new User(login, password, name, data);
        }
    }


    public void post(RequestSession session) {
        try {
            String[] splat = session.splat();
            if(!splat[0].equals("api") || splat[1]==null){
                session.notFound();
                return;
            }
            String[] args = session.encodeBodyAsStr().split(";");
            switch (splat[1]) {
                case "mail-newuser":
                    createMailConfirm(
                            args[0], args[1], args[2], AuthService.MailRequest.Type.NEW_USER, args[3], new MailServer.CallBack() {
                                @Override
                                public void onSuccess() {
                                    HEWorker.getLogger().info("Mail sended to [" + args[0] + "] !");
                                }

                                @Override
                                public void onError(Exception e) {
                                    e.printStackTrace();
                                }
                            }
                    );
                    break;
                case "login":
                    Tuple<U, String> result = auth(
                            args[0], args[1], args[2], getTokenData(session)
                    );
                    setToken(result.B, session);
                    break;
                case "mail-restore":
                    createMailConfirm(
                            args[0], args[1], args[2], AuthService.MailRequest.Type.BREAK_PASSWORD, args[3], new MailServer.CallBack() {
                                @Override
                                public void onSuccess() {
                                    HEWorker.getLogger().info("Mail sended to [" + args[0] + "] !");
                                }

                                @Override
                                public void onError(Exception e) {
                                    e.printStackTrace();
                                }
                            }
                    );
                    break;
                default:
                    onNotfoundPost(session);
                    return;
            }
            session.send("SUCCESS");
        } catch (IndexOutOfBoundsException e){
            session.send("ERROR WRONG_USAGE");
        } catch (AuthException e){
            session.send(e.toString());
        }
    }


    @Override
    public void handle(RequestSession session) {
        if(session.method().equals("POST")){
            post(session);
            return;
        }
        try {
            String[] splat = session.splat();
            if(auth(session)!=null){
                if(session.queryParams("api")==null){
                    if(splat[0] != null && splat[0].equals("logout")){
                        logout(getToken(session), getTokenData(session));
                        setToken("", session);
                        //session.setHeader(HttpHeaderNames.CACHE_CONTROL, "no-cache");
                        session.redirect("/auth/");
                    } else {
                        session.redirect(getGoTo(session));
                    }
                    return;
                }
            }
            String var = session.queryParams("cap4a-mtd");
            if(var != null){
                session.send(handleCap4a(session, var));
                return;
            }

            if(splat[0] == null){
                login(session);
                return;
            }

            switch (splat[0]) {
                case "confirm_create": confirmCreate(session);return;
                case "confirm_update": confirmUpdate(session);return;
                case "check_mail":     hasMail(session);return;
                case "chick_name":     hasName(session);return;
                case "get_token":
                    session.send((var = getToken(session)) == null?"":var);
                    return;
            }
            onNotfound(session);
        } catch (AuthException | CryptException e){
            session.send(e.toString(), HttpResponseStatus.INTERNAL_SERVER_ERROR);
        }
    }


    protected void onNotfound(RequestSession session) throws AuthException, CryptException{
        session.redirect("/auth");
    }


    protected void onNotfoundPost(RequestSession session){
        session.send("ERROR UNKNOWN_MTD");
    }


    private void hasMail(RequestSession session) throws AuthException {
        session.send(hasUser(getLogin(session), getCryptKey(session))?"TRUE":"FALSE");
    }


    private void hasName(RequestSession session) throws AuthException {
        session.send(hasName(getName(session), getCryptKey(session))?"TRUE":"FALSE");
    }


    protected String getGoTo(RequestSession session){
        String goTo = session.queryParams("goto");
        return goTo==null?"/":MiniLink.ofMini(goTo);
    }


    private void confirmUpdate(RequestSession session) throws AuthException {
        onPostConfirm(editPsw(
                getLogin(session), getCryptKey(session), getTokenData(session)
        ), session);
    }


    private void confirmCreate(RequestSession session) throws AuthException {
        onPostConfirm(createAccount(
                getLogin(session), getName(session), getCryptKey(session), new byte[0], getTokenData(session)
        ), session);
    }


    private void onPostConfirm(Tuple<U, String> result, RequestSession session){
        setToken(result.B, session);
        String goTo = getGoTo(session);
        if(session.queryParams("api")==null) {
            session.redirect(goTo);
        } else {
            session.send("SUCCESS " + goTo);
        }
    }


    public static String getToken(RequestSession session){
        Cookie cookie = session.getCookie("aut");
        if(cookie==null)return null;
        return cookie.value();
    }


    public void setToken(String token, RequestSession session){
        Cookie cToken = new DefaultCookie("aut", token);
        cToken.setDomain(session.getDomain());
        cToken.setPath("/");
        cToken.setMaxAge(2592000);
        cToken.setSecure(true);
        cToken.setHttpOnly(true);
        session.setCookie(cToken);
    }


    public static byte[] getTokenData(RequestSession session){
        return new byte[0];
    }


    protected String getLogin(RequestSession session){
        return session.queryParams("login");
    }


    protected String getName(RequestSession session){
        return session.queryParams("name");
    }


    protected String getCap4aID(RequestSession session){
        return session.queryParams("cap4a-id");
    }


    protected String getCap4aAnswer(RequestSession session){
        return session.queryParams("cap4a-answer");
    }


    protected String getCryptKey(RequestSession session){
        return session.queryParams("crypt-key");
    }


    protected byte[] handleCap4a(RequestSession session, String part) {
        if(part.equals("font"))return font;
        try {
            switch (part) {
                case "html": {
                    String login = getLogin(session);
                    Cap4a cap4a = getCap4a(login, getCap4aID(session));
                    if (cap4a == null) cap4a = createAuthCap4a(login);
                    return cap4a.htmlCode.getBytes(StandardCharsets.UTF_8);
                }
                case "css": {
                    String capId = getCap4aID(session);
                    Cap4a cap4a = getCap4a(capId);
                    return (defCss + cap4a.cssCode).getBytes(StandardCharsets.UTF_8);
                }
                case "answer": {
                    WebCryptKey cryptKey = activateCap4a(
                            getLogin(session),
                            getCap4aID(session),
                            getCap4aAnswer(session)
                    );
                    return cryptKey.toString().getBytes(StandardCharsets.UTF_8);
                }
            }
            return "ERROR NO_CAP4A".getBytes(StandardCharsets.UTF_8);
        } catch (AuthException e){
            return ("ERROR "+e.type.name()).getBytes(StandardCharsets.UTF_8);
        }
    }


    protected void login(RequestSession session){
        fmLogin.handle(session);
    }


    @Override
    public User auth(RequestSession session) {
        String token = getToken(session);
        if(token==null)return null;
        return auth(token, getTokenData(session));
    }


    @Override
    public void goToAuth(RequestSession session) {
        session.redirect("/auth?goto="+ MiniLink.toMini(session.path()));
    }
}