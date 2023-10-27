package plus.te4net.core.html;

import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import plus.te4net.core.api.*;
import plus.te4net.core.html.vr.VrHtmlFormat;
import plus.te4net.core.node.HWorker;
import plus.te4net.core.node.HWorkerRepository;
import plus.te4net.utl.IpUtls;
import plus.tson.TsonFile;
import java.io.File;
import java.nio.charset.StandardCharsets;

@PublicUsage
public class HtmlRoute implements PluginBase, IRequestProcessor, PluginRoute {
    private final IRequestProcessor formatter;
//    private final HWorker worker;
    private final String routeName;

    public HtmlRoute(HWorker worker, File file){
        this.routeName = file.getName();
        this.formatter = VrHtmlFormat.ofHtml(worker, TsonFile.read(file,""));
    }

    public HtmlRoute(HWorker worker, String path, String routeName) {
        this.routeName = routeName;
        byte[] data = getSrc(path);
        this.formatter = VrHtmlFormat.ofHtml(worker, data==null?"":new String(data, StandardCharsets.UTF_8));
    }

    public HtmlRoute(HWorker worker, byte[] data, String routeName) {
        this.routeName = routeName;
//        this.worker = worker;
        this.formatter = VrHtmlFormat.ofHtml(worker, new String(data, StandardCharsets.UTF_8));
    }


    @Override
    public void handle(IRequestSession session) {
        checkIp(session);
        formatter.handle(session);
    }


    protected void checkIp(IRequestSession session){
        Cookie rCookie = session.getCookie("uia");
        String rIp = session.context().channel().remoteAddress().toString();
        rIp = rIp.substring(1,rIp.indexOf(':'));
        if(rCookie!=null){
            int prevIp = Integer.parseInt(rCookie.value());
            int curIp = IpUtls.intIpFromStr(rIp);
            if(prevIp == curIp){
                return;
            }
            HWorkerRepository.getLogger().info("IP "+IpUtls.strIpFromInt(prevIp)+" -> "+rIp);
        }
        DefaultCookie cookie = new DefaultCookie("uia", String.valueOf(IpUtls.intIpFromStr(rIp)));
        cookie.setDomain(session.getDomain());
        cookie.setPath("/");
        cookie.setMaxAge(2147483647);
        cookie.setSecure(true);
        session.setCookie(cookie);
        HWorkerRepository.getLogger().info("New user IP: "+rIp);
    }


    @Override
    public String get() {
        return routeName;
    }
}