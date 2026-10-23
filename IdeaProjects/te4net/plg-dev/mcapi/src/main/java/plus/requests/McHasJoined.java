package plus.requests;

import plus.tson.TsonMap;
import plus.tson.TsonObj;
import plus.tson.TsonSerelizable;


public class McHasJoined implements TsonSerelizable {
    private final String id;
    private final TsonMap properties;

    public McHasJoined(String uuid, TsonMap properties) {
        this.id = uuid;
        this.properties = properties;
    }

    @Override
    public TsonObj toTson() {
        TsonMap map = new TsonMap();
        map.put("id", id);
        map.put("properties", properties);
        return map;
    }


    @Override
    public String toString() {
        return toTson().toJsonStr();
    }
}