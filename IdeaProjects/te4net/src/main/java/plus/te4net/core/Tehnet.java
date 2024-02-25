package plus.te4net.core;

import plus.te4net.core.api.IDataProvider;
import plus.te4net.core.api.PublicUsage;
import plus.te4net.core.node.TehnetSimple;
import plus.tson.TsonMap;


@PublicUsage
public class Tehnet extends TehnetSimple {
    public Tehnet(){
        this(new TsonMap());
    }


    public Tehnet(TsonMap config){
        super(config.getOrDefaultInt("threads", 18), config);
    }


    protected IDataProvider getDefDataProvider(){
        return null;
    }
}