package plus.tehnet.bimg;

import plus.te4net.core.api.IRequestSession;
import plus.te4net.core.api.PluginFunction;
import plus.te4net.utl.MiniLink;


public class BImgIncludeVidFunc implements PluginFunction {
    private static final String prefix =
            "<meta property=\"og:video:type\" content=\"text/html\">"+
                    "<meta property=\"og:video:width\" content=\"1280\">" +
                    "<meta property=\"og:video:height\" content=\"720\">"+
                    "<meta property=\"og:type\" content=\"video.other\">" +
                    "<meta name=\"twitter:player\" content=\"";
    private static final String postfix = "\">";

    @Override
    public String name() {
        return "inc-vid";
    }


    @Override
    public String onCall(IRequestSession session) {
        String result = MiniLink.ofMini(session.queryParams("vid"));
        if(result == null)return null;
        return prefix+result+postfix;
    }
}
