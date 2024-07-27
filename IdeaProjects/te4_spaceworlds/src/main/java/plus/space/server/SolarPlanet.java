package plus.space.server;

import plus.space.common.GalName;
import plus.space.common.SNames;
import plus.tson.TsonMap;


public class SolarPlanet extends SolarBigObj{
    final SolarWorldItem planet;
    final int orbit;
    final GalName realName;
    final TsonMap settings;


    public SolarPlanet(SolarRepository repository, TsonMap settings, GalName parent) {
        super(settings);
        orbit = settings.getInt(SNames.DISTANCE);
        this.settings = settings;
        planet = new SolarWorldItem(repository, this);
        realName = new GalName(getName(), parent);
        init(repository, settings);
    }


    @Override
    public int getDimensionId(){
        return planet.dimID;
    }


    public boolean hasOxygen(){
        return planet.hasOxygen;
    }


    @Override
    public int getType() {
        return planet.type.ID;
    }


    @Override
    public GalName getFullName() {
        return realName;
    }


    @Override
    public String getName(){
        return planet.name;
    }


    public int getOrbit(){
        return orbit;
    }
}