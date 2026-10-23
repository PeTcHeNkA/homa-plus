package plus.space.server;

import plus.space.common.SNames;
import plus.tson.TsonMap;
import plus.tson.TsonObj;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public abstract class SolarBigObj implements ISolarOrbitObj{
    private final Map<String,SolarPlanet> satellites;
    private final int textureId;
    public final float size;

    public SolarBigObj(TsonMap settings) {
        TsonObj list = settings.get(SNames.ORBIT);
        if(list==null || list.getList().isEmpty()){
            satellites = Collections.emptyMap();
        } else {
            satellites = new HashMap<>();
        }
        textureId = settings.getInt(SNames.TEXTURE);
        size = settings.getFloat(SNames.SIZE);
    }


    protected void init(SolarRepository repository, TsonMap settings){
        TsonObj list = settings.get(SNames.ORBIT);
        if(list==null)return;
        for(TsonObj satelliteSettings:list.getList()){
            SolarPlanet satellite = new SolarPlanet(repository, satelliteSettings.getMap(), getFullName());
            satellites.put(satellite.getName(), satellite);
        }
    }


    public abstract int getType();


    public int getTextureId() {
        return textureId;
    }


    @Override
    public Collection<SolarPlanet> getSatellites(){
        return satellites.values();
    }


    @Override
    public SolarPlanet getSatellite(String name){
        return satellites.get(name);
    }
}