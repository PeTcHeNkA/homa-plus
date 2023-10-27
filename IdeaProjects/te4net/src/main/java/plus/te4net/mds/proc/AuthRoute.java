package plus.te4net.mds.proc;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import plus.te4net.core.api.IRequestProcessor;
import plus.te4net.core.api.IRequestSession;
import plus.te4net.core.html.vr.VrCss;
import plus.te4net.core.html.vr.VrHtmlFormat;
import plus.te4net.core.node.HWorker;
import plus.te4net.core.node.HWorkerRepository;
import plus.te4net.mds.auth.*;
import plus.te4net.mds.cap4a.Cap4a;
import plus.te4net.mds.cap4a.Cap4aLib;
import plus.te4net.mds.crypt.WebCryptKey;
import plus.te4net.core.fm.TextMapFormatter;
import plus.te4net.core.node.HPluginSystem;
import plus.te4net.utl.MailServer;
import plus.te4net.utl.MiniLink;
import plus.tson.TsonMap;
import plus.tson.TsonStr;
import plus.tson.utl.Tuple;
import plus.util.SimpleSrcGetter;
import java.nio.charset.StandardCharsets;


public class AuthRoute<U extends User> extends AuthService<U> implements SimpleSrcGetter, IRequestProcessor, IAuthProvider {
    public static final byte[] font = HPluginSystem.getSrc(AuthRoute.class, "Bufcated1.ttf");
    public static final String defCss = new VrCss(
            new String(HPluginSystem.getSrc(AuthRoute.class,"cap4a-template.css"))
    ).toString();

    private final IRequestProcessor fmLogin;
    private final HWorker worker;

    public AuthRoute(HWorker worker, TsonMap config){
        this(worker, new UserMap<U>((UserData<U>) new DefData(config.getOrDefault(
                "database", new TsonStr(worker.getWorkDir().getPath() + "/users.db")
                ).getStr())),
                config.getOrDefault("mail",new TsonMap("{virtual=(true)}")).getMap()
        );
    }

    public AuthRoute(HWorker worker, UserMap<U> userMap, TsonMap mailAuth){
        super(userMap, new Cap4aLib<>(), new MailServer(mailAuth.getMap("mail")));
        setMailFormat(new TextMapFormatter(worker, getStrSrc("mail-code-template.html"),"{%","%}"));
        fmLogin = VrHtmlFormat.ofHtml(worker, getStrSrc("mcauth2.html"));
        this.worker = worker;// mcauth2.html  login-template.html
        onInit(worker);
    }

    protected void onInit(HWorker worker){
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

    public void post(IRequestSession session) {
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
                            args[0].toLowerCase(), args[1], AuthService.MailRequest.Type.NEW_USER, args[2], new MailServer.CallBack() {
                                @Override
                                public void onSuccess() {
                                    HWorkerRepository.getLogger().info("Mail sended to [" + args[0] + "] !");
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
                            args[0].toLowerCase(), args[1], args[2], getTokenData(session)
                    );
                    setToken(result.B, session);
                    break;
                case "mail-restore":
                    createMailConfirm(
                            args[0].toLowerCase(), args[1], AuthService.MailRequest.Type.BREAK_PASSWORD, args[2], new MailServer.CallBack() {
                                @Override
                                public void onSuccess() {
                                    HWorkerRepository.getLogger().info("Mail sended to [" + args[0] + "] !");
                                }

                                @Override
                                public void onError(Exception e) {
                                    e.printStackTrace();
                                }
                            }
                    );
                    break;
                default:
                    session.send("ERROR UNKNOWN_MTD");
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
    public void handle(IRequestSession session) {
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
            String isCap4a = session.queryParams("cap4a-mtd");
            if(isCap4a!=null){
                session.send(handleCap4a(session, isCap4a));
                return;
            }

            if(splat[0]==null){
                login(session);
                return;
            }

            switch (splat[0]) {
                case "confirm_create": confirmCreate(session);return;
                case "confirm_update": confirmUpdate(session);return;
                case "check_mail": hasMail(session);return;
            }
            session.redirect("/auth/");
        //} catch (RouteTextFormatter.Exception e){

        } catch (AuthException e){
            session.send(e.toString(), HttpResponseStatus.INTERNAL_SERVER_ERROR);
        }
    }


    private void hasMail(IRequestSession session) throws AuthException {
        session.send(hasUser(getLogin(session).toLowerCase(), getCryptKey(session))?"TRUE":"FALSE");
    }


    private String getGoTo(IRequestSession session){
        String goTo = session.queryParams("goto");
        return goTo==null?"/":MiniLink.ofMini(goTo);
    }


    private void confirmUpdate(IRequestSession session) throws AuthException {
        onPostConfirm(editPsw(
                getLogin(session), getCryptKey(session), getTokenData(session)
        ), session);
    }


    private void confirmCreate(IRequestSession session) throws AuthException {
        onPostConfirm(createAccount(
                getLogin(session), getCryptKey(session), getName(session), new byte[0], getTokenData(session)
        ), session);
    }


    private void onPostConfirm(Tuple<U, String> result, IRequestSession session){
        setToken(result.B, session);
        String goTo = getGoTo(session);
        if(session.queryParams("api")==null) {
            session.redirect(goTo);
        } else {
            session.send("SUCCESS " + goTo);
        }
    }


    public static String getToken(IRequestSession session){
        Cookie cookie = session.getCookie("aut");
        if(cookie==null)return null;
        return cookie.value();
    }


    public void setToken(String token, IRequestSession session){
        Cookie cToken = new DefaultCookie("aut", token);
        cToken.setDomain(session.getDomain());
        cToken.setPath("/");
        cToken.setMaxAge(2592000);
        cToken.setSecure(true);
        session.setCookie(cToken);
    }


    public static byte[] getTokenData(IRequestSession session){
        return new byte[0];
    }


    private String getLogin(IRequestSession session){
        return session.queryParams("login");
    }


    private String getName(IRequestSession session){
        return session.queryParams("name");
    }


    private String getCap4aID(IRequestSession session){
        return session.queryParams("cap4a-id");
    }


    private String getCap4aAnswer(IRequestSession session){
        return session.queryParams("cap4a-answer");
    }


    private String getCryptKey(IRequestSession session){
        return session.queryParams("crypt-key");
    }


    private byte[] handleCap4a(IRequestSession session, String part) {
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


    private void login(IRequestSession session){
        fmLogin.handle(session);
    }


    @Override
    public User auth(IRequestSession session) {
        String token = getToken(session);
        if(token==null)return null;
        return auth(token, getTokenData(session));
    }


    @Override
    public void goToAuth(IRequestSession session) {
        session.redirect("/auth/?goto="+ MiniLink.toMini(session.path()));
    }
}