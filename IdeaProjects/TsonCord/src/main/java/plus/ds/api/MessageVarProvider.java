package plus.ds.api;

import plus.ds.IrisSystem;
import plus.tson.format.IVarGetter;
import plus.tson.format.IVarProvider;


public class MessageVarProvider implements IVarProvider<IMessageEvent> {
    private final IrisSystem.GuildProgram program;

    public MessageVarProvider(IrisSystem.GuildProgram program) {
        this.program = program;
    }

    @Override
    public IVarGetter<IMessageEvent> getFmGetter(String name) {
        if(name.startsWith("user")){
            String[] args = name.split(":");
            switch (args[1]){
                case "name":
                    return ev -> ev.getMessage().getAuthor().getName();
                case "gname":
                    return ev -> ev.getMessage().getAuthor().getGlobalName();
                case "ava_url":
                    return ev -> ev.getMessage().getAuthor().getAvatarUrl();
                case "ava_e_url":
                    return ev -> ev.getMessage().getAuthor().getEffectiveAvatarUrl();
                case "id":
                    return ev -> ev.getMessage().getAuthor().getId();
                case "ename":
                    return event -> event.getMessage().getAuthor().getEffectiveName();
                case "var":{

                }
            }
        }
        return null;
    }
}
