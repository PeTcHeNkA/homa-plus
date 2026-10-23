package plus.ds.memory;

import plus.ds.IrisSystem;
import plus.ds.api.IMessageEvent;
import plus.ds.api.IMsgFilter;
import plus.ds.api.MessageVarProvider;
import plus.tson.TsonMap;
import plus.tson.format.IVarGetter;


public class MemoryWriter implements IMsgFilter {
    final IrisSystem.GuildProgram program;
    final IVarGetter<IMessageEvent> valueGetter;
    final String[] key;

    public MemoryWriter(IrisSystem.GuildProgram program, TsonMap map) {
        this.program = program;
        valueGetter = new MessageVarProvider(program).getFmGetter(map.getStr("value"));
        this.key = map.getStr("key").split("\\.");
    }


    @Override
    public boolean isSuccess(IMessageEvent event, String msg) {
        program.getDataProvider().saveRaw(valueGetter.get(event).getBytes(), key);
        return true;
    }
}
