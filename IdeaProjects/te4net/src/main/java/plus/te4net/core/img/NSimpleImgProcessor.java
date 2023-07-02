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
        byte[] result = getStd(session, provider, getOptimalSize(session));
        if(result==null){
            session.notFound();
        } else {
            handleImg(session, result, Arrays.hashCode(result));
        }
    }


    private static String[] getImgName(String[] splat){
        int size = 1;
        for(;size<splat.length;size++){
            if(splat[size]==null)break;
        }
        String[] st = new String[2+size];
        if(size==1){
            st[2] = splat[0];
        } else {
            System.arraycopy(splat, 0, st, 2, st.length-2);
        }
        return st;
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


    static byte[] getStd(IRequestSession session, IDataProvider provider, ImgSize size){
        String[] key = getImgName(session.splat());
        key[0] = session.worker().getMainDomain();
        key[1] = size.suffix();

        byte[] result = provider.getRaw(key);
        if(result!=null || size == ImgSize.ORIG)return result;

        key[1] = ImgSize.ORIG.suffix();

        byte[] orig = provider.getRaw(key);
        if(orig==null)return null;
        result = Compressor.press(session.worker().getCompressorSettings(), key[key.length-1], orig, size);

        key[1] = size.suffix();

        provider.saveRaw(result, key);
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
