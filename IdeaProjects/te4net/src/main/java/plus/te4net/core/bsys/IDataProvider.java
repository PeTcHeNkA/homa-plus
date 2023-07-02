package plus.te4net.core.bsys;


public interface IDataProvider {
    byte[] getRaw(String name, String table);


    void saveRaw(String name, String table, byte[] data);


    default boolean containsRaw(String name, String table){
        return getRaw(name, table) != null;
    }
}