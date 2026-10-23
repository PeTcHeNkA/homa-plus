package plus.ds.memory;

import plus.ds.IrisSystem;
import plus.ds.api.IMessageEvent;
import plus.ds.api.MessageVarProvider;
import plus.te4net.core.api.IDataProvider;
import plus.tson.TsonMap;
import plus.tson.format.IVarGetter;
import static plus.ds.memory.MemoryFilter.keyByEvent;


public class MemoryReader implements IVarGetter<IMessageEvent> {
    private final IrisSystem.GuildProgram program;
    private final String[] key;
    private final IVarGetter<IMessageEvent> def;

    public MemoryReader(IrisSystem.GuildProgram program, TsonMap map) {
        this.program = program;
        this.key = map.getStr("key").split("\\.");
        def = new MessageVarProvider(program).getFmGetter(map.getStr("value"));
    }


    @Override
    public String get(IMessageEvent event) {
        IDataProvider provider = program.getDataProvider();
        byte[] result = provider.getRaw(keyByEvent(key, event));
        if(result != null)return new String(result);
        return def.get(event);
    }
}