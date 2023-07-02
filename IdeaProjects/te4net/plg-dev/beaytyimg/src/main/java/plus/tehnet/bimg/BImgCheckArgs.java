package plus.tehnet.bimg;

import plus.te4net.core.api.IRequestSession;
import plus.te4net.core.api.PluginFunction;
import plus.te4net.core.fm.RouteTextFormatter;
import plus.te4net.core.img.ImgSize;
import plus.te4net.core.node.HWorker;


public class BImgCheckArgs implements PluginFunction {
    @Override
    public String name() {
        return "check-b-args";
    }


    @Override
    public String onCall(IRequestSession session) {
        String[] splat = session.splat();
        if(splat[0]==null)return null;
        HWorker worker = session.worker();
        if(worker.getImgLib().containsRaw(
                getImgName(splat), worker.getMainDomain()+"/"+ImgSize.ORIG.suffix()
        ))return null;
        throw new RouteTextFormatter.Redirect("/img-build/");
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