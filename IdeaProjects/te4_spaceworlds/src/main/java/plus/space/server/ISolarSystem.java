package plus.space.server;


public interface ISolarSystem extends ISolarOrbitObj{
    int getX();
    int getY();
    int getZ();
    int getTextureId();
    float getSize();
    default boolean isReal(){return false;}
}