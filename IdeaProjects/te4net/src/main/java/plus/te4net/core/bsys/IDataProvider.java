package plus.te4net.core.bsys;


public interface IDataProvider {
    byte[] getRaw(String... path);


    void saveRaw(byte[] data, String... path);


    default boolean containsRaw(String... path){
        return getRaw(path) != null;
    }
}