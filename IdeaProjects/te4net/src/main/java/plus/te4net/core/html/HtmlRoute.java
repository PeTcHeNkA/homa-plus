package plus.te4net.core.html;

import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import plus.te4net.core.api.IRequestProcessor;
import plus.te4net.core.api.IRequestSession;
import plus.te4net.core.api.PluginBase;
import plus.te4net.core.api.PluginRoute;
import plus.te4net.core.node.HWorker;
import plus.te4net.core.fm.RouteTextFormatter;
import plus.te4net.utl.IpUtls;
import plus.te4net.utl.Logger;
import java.nio.charset.StandardCharsets;


public class HtmlRoute implements PluginBase, IRequestProcessor, PluginRoute {
    private final RouteTextFormatter formatter;
    private final HWorker worker;
    private final String routeName;

    public HtmlRoute(HWorker worker, String path, String routeName) {
        this.routeName = routeName;
        this.worker = worker;
        byte[] data = getSrc(path);
        this.formatter = RouteTextFormatter.ofHtml(worker, new String(data, StandardCharsets.UTF_8));
    }

    public HtmlRoute(HWorker worker, byte[] data, String routeName) {
        this.routeName = routeName;
        this.worker = worker;
        this.formatter = RouteTextFormatter.ofHtml(worker, new String(data, StandardCharsets.UTF_8));
    }


    @Override
    public void handle(IRequestSession session) {
        checkIp(session);
        formatter.writeFormatted(session);
    }


    protected void checkIp(IRequestSession session){
        Cookie rCookie = session.getCookie("uia");
        String rIp = session.context().channel().remoteAddress().toString();
        if(rCookie!=null){
            int prevIp = Integer.parseInt(rCookie.value());
            int curIp = IpUtls.intIpFromStr(rIp);
            if(prevIp == curIp){
                return;
            }
            Logger.get().info("IP "+IpUtls.strIpFromInt(prevIp)+" -> "+rIp);
        } else {
            DefaultCookie cookie = new DefaultCookie("uia", String.valueOf(IpUtls.intIpFromStr(rIp)));
            cookie.setDomain(session.getDomain());
            cookie.setSecure(true);
            cookie.setPath("/");
            cookie.setMaxAge(2147483647);
            session.setCookie(cookie);
            Logger.get().info("New user IP: "+rIp);
        }
    }


    @Override
    public String get() {
        return routeName;
    }
}