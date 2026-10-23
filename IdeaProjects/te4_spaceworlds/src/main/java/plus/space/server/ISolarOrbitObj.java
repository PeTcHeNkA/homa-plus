package plus.space.server;

import java.util.Collection;


public interface ISolarOrbitObj extends ISolarObj{
    default Collection<SolarPlanet> getSatellites(){throw new UnsupportedOperationException();}
    default SolarPlanet getSatellite(String name){throw new UnsupportedOperationException();}
    default int getDimensionId(){throw new UnsupportedOperationException();}
}