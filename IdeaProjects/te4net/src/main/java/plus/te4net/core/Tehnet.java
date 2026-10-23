package plus.te4net.core;

import plus.te4net.core.api.IDataProvider;
import plus.te4net.core.api.PublicUsage;
import plus.te4net.core.node.TehnetSimple;
import plus.tson.TsonMap;
import static plus.tson.ext.TsonAccessUtils.*;


@PublicUsage
public class Tehnet extends TehnetSimple {
    public Tehnet(){
        this(new TsonMap());
    }


    public Tehnet(TsonMap config){
        super(getR(config, "threads", Runtime.getRuntime().availableProcessors()), config);
    }


    protected IDataProvider getDefDataProvider(){
        return null;
    }
}