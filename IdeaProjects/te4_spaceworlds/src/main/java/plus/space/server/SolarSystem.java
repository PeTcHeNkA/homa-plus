package plus.space.server;

import plus.space.common.GalName;
import plus.space.common.SNames;
import plus.tson.TsonMap;


public class SolarSystem extends SolarBigObj implements ISolarSystem{
    public final GalName starName;
    public final int mapX, mapY, mapZ;


    public SolarSystem(SolarRepository repository, TsonMap settings) {
        super(settings);
        starName = new GalName(settings.getStr(SNames.NAME), null);
        repository.starLib.use(starName.getSelfName());

        mapX = settings.getInt( SNames.POS_X );
        mapY = settings.getInt( SNames.POS_Y );
        mapZ = settings.getInt( SNames.POS_Z );

        init(repository, settings);
    }


    public int getType(){
        return -1;
    }


    @Override
    public GalName getFullName() {
        return starName;
    }


    @Override
    public String getName() {
        return starName.getSelfName();
    }


    @Override
    public int getX() {
        return mapX;
    }


    @Override
    public int getY() {
        return mapY;
    }


    @Override
    public int getZ() {
        return mapZ;
    }


    @Override
    public float getSize() {
        return size;
    }


    @Override
    public boolean isReal() {
        return true;
    }
}