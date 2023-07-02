package plus.te4net.core.img;

import io.netty.handler.codec.http.HttpHeaderNames;
import plus.te4net.core.api.IRequestProcessor;
import plus.te4net.core.api.IRequestSession;
import plus.te4net.core.node.HWorker;
import plus.te4net.core.bsys.IDataProvider;
import java.util.Arrays;


public class NSimpleImgProcessor implements IRequestProcessor {
    protected final IDataProvider provider;

    public NSimpleImgProcessor(IDataProvider provider) {
        this.provider = provider;
    }

    @Override
    public void handle(IRequestSession session) {
        String splatKey = getImgName(session.splat());
//        String key = session.worker().getDomain()+"/"+splatKey;
        ImgSize size = getOptimalSize(session);
        byte[] result = getStd(session.worker(), provider, splatKey, size);
        if(result==null){
            session.notFound();
        } else {
            handleImg(session, result, Arrays.hashCode(result));
        }
    }

    static String getImgName(String[] splat){
        StringBuilder b = new StringBuilder(splat[0]);
        for(int i=1;i<splat.length;i++){
            if(splat[i]==null)break;
            b.append("/").append(splat[i]);
        }
        return b.toString();
    }


    static void handleImg(IRequestSession session, byte[] bytes, int hs) {
        String etagStr = session.getRHeader(HttpHeaderNames.IF_NONE_MATCH);
        boolean isReady = false;
        try {
            isReady = etagStr!=null && (Integer.parseInt(etagStr) == hs);
        } catch (NumberFormatException ignored){}
        if(isReady){
            session.notModified();
        } else {
            String qp = session.queryParams("dl");
            if (qp != null && qp.charAt(0) == 't') {
                session.setHeader(HttpHeaderNames.CONTENT_TYPE, "download");
            } else {
                session.setHeader(HttpHeaderNames.CONTENT_TYPE, "image");
            }
            session.setHeader(HttpHeaderNames.ETAG, String.valueOf(hs));
            session.send(bytes);
        }
    }


    static byte[] getStd(HWorker worker, IDataProvider provider, String key, ImgSize size){
        String tPrefix = worker.getMainDomain()+"/";
        byte[] result = provider.getRaw(key, tPrefix+size.suffix());
        if(result!=null || size == ImgSize.ORIG)return result;
        byte[] orig = provider.getRaw(key, tPrefix+ImgSize.ORIG.suffix());
        if(orig==null)return null;
        result = Compressor.press(worker.getCompressorSettings(), key, orig, size);
        provider.saveRaw(key, tPrefix+size.suffix(), result);
        return result;
    }


    static ImgSize getOptimalSize(IRequestSession session){
        String alt = session.queryParams("size");
        if(alt != null){
            char n = alt.charAt(0);
            if(n=='1')return ImgSize.P100;
            if(n=='2')return ImgSize.P75;
            if(n=='0')return ImgSize.ORIG;
            return ImgSize.P50;
        }
        int i1 = 0;
        char[] dca = session.getRHeader(HttpHeaderNames.USER_AGENT).toCharArray();
        for(char c:dca){
            ++i1;
            if(c == '('){
                break;
            }
        }
        char df = dca[i1];
        if(df=='W'){
            return ImgSize.P100;
        } else if(df=='L'){
            for(int i=i1;i<i1+10;i++){
                if(dca[i]=='A')return ImgSize.P50;
            }
            return ImgSize.P75;
        } else if(df=='i'){
            return ImgSize.P75;
        } else if(df=='c'){
            return ImgSize.P50;
        }
        return ImgSize.P100;
    }
}
