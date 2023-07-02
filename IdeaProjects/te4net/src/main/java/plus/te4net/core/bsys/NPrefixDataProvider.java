package plus.te4net.core.bsys;


public class NPrefixDataProvider implements IDataProvider {
    private final IDataProvider parent;
    private final String prefix;
    public NPrefixDataProvider(IDataProvider parent, String prefix) {
        this.parent = parent;
        this.prefix = prefix;
    }


    @Override
    public byte[] getRaw(String name, String table) {
        return parent.getRaw(name, prefix+table);
    }


    @Override
    public void saveRaw(String name, String table, byte[] data) {
        parent.saveRaw(name, prefix+table, data);
    }


    @Override
    public boolean containsRaw(String name, String table) {
        return parent.containsRaw(name, prefix+table);
    }
}
