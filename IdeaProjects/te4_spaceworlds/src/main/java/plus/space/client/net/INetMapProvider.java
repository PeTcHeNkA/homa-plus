package plus.space.client.net;

import plus.space.common.GalName;
import plus.tson.TsonList;
import java.util.function.Consumer;


public interface INetMapProvider {
    void getPlanetFromServer(GalName name, Consumer<TsonList> callBack);
    void getSystemsFromServer(Consumer<TsonList> callBack);
    void getActualSizeFromServer(Consumer<Integer> callBack);
    void sendFlyTo(GalName name, Consumer<Integer> callBack);
}