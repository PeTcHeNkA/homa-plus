package plus.mcapi.api;

import plus.te4net.core.api.PluginRoute;
import plus.te4net.core.api.RequestSession;
import plus.tson.TsonFile;
import java.io.File;


public class McSkinLib implements PluginRoute {
    @Override
    public String get() {
        return "mcapi/setskin";
    }

    @Override
    public void handle(RequestSession session) {
        System.out.println(session);
        session.send("SUCCESS");
        TsonFile.write(new File("test.png"), session.encodeBody());
    }
}