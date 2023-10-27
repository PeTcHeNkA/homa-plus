package plus.te4net.core.bsys;


public class NPrefixDataProvider implements IDataProvider {
    private final IDataProvider parent;
    private final String prefix;

    public NPrefixDataProvider(IDataProvider parent, String prefix) {
        this.parent = parent;
        if(prefix.charAt(0)=='/'){
            prefix = prefix.substring(1);
        }
        if(prefix.charAt(prefix.length()-1)=='/'){
            prefix = prefix.substring(0, prefix.length()-1);
        }
        this.prefix = prefix;
    }

    private String[] expand(String[] prev){
        String[] result = new String[prev.length+1];
        System.arraycopy(prev, 0, result, 1, prev.length);
        result[0] = prefix;
        return result;
    }


    @Override
    public byte[] getRaw(String... path) {
        return parent.getRaw(expand(path));
    }


    @Override
    public void saveRaw(byte[] data, String... path) {
        parent.saveRaw(data, expand(path));
    }


    @Override
    public boolean containsRaw(String... path) {
        return parent.containsRaw(expand(path));
    }


    @Override
    public String[] getItems(String... path) {
        return parent.getItems(expand(path));
    }
}
