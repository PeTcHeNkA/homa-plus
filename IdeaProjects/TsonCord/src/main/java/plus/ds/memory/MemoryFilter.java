package plus.ds.memory;

import plus.ds.IrisSystem;
import plus.ds.api.IMessageEvent;
import plus.ds.api.IMsgFilter;
import plus.tson.TsonMap;


public class MemoryFilter implements IMsgFilter {
    private final IrisSystem.GuildProgram program;
    private final IMsgFilter root;
    private final String[] key;

    public MemoryFilter(IrisSystem.GuildProgram program, TsonMap map) {
        this.program = program;
        IMsgFilter filter = IMsgFilter.build(program, map.get("filter"));
        if(filter==null){
            filter = IMsgFilter.notNull;
        }
        root = filter;
        this.key = map.getStr("key").split("\\.");
    }


    static String[] keyByEvent(String[] key, IMessageEvent event){
        return keyByUser(key, event.getUser().getId());
    }


    static String[] keyByUser(String[] key, String uid){
        String[] newKey = new String[key.length+1];
        System.arraycopy(key, 0, newKey, 1, key.length);
        newKey[0] = uid;
        return newKey;
    }


    @Override
    public boolean isSuccess(IMessageEvent event, String msg) {
        byte[] data = program.getDataProvider().getRaw(keyByEvent(key, event));
        if(data==null)return root.isSuccess(event, null);
        return root.isSuccess(event, new String(data));
    }
}
