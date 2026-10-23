package plus.space.server;

import plus.space.common.GalName;


public interface ISolarObj {
    GalName getFullName();
    default String getName(){return getFullName().getSelfName();}
}