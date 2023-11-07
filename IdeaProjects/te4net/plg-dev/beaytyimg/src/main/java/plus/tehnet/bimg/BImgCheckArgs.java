package plus.tehnet.bimg;

import plus.te4net.core.api.RequestSession;
import plus.te4net.core.api.PluginFunction;
import plus.te4net.core.node.HEWorker;
import plus.te4net.core.node.HWorker;
import plus.te4net.mds.html.vr.VrHtmlFormat;
import plus.te4net.mds.img.ImgSize;


public class BImgCheckArgs implements PluginFunction {
    @Override
    public String name() {
        return "check-b-args";
    }


    @Override
    public String onCall(RequestSession session) {
        String[] splat = session.splat();
        if(splat[0]==null)return null;
        HWorker worker = session.worker();
        if(((HEWorker)worker).getImgLib().containsRaw(
                worker.getMainDomain(), ImgSize.ORIG.suffix(), getImgName(splat)
        ))return null;
        throw new VrHtmlFormat.Redirect("/img-build/");
    }


    private String getImgName(String[] splat){
        StringBuilder b = new StringBuilder(splat[0]);
        for(int i=1;i<splat.length;i++){
            if(splat[i]==null)break;
            b.append("/").append(splat[i]);
        }
        return b.toString();
    }
}