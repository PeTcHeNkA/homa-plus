package plus.ds.dyn;

import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import plus.ds.IrisSystem;
import plus.ds.api.IListener;
import plus.ds.api.IMessageEvent;
import plus.ds.api.IMsgBuilder;
import plus.ds.api.IMsgFilter;
import plus.tson.TsonBool;
import plus.tson.TsonInt;
import plus.tson.TsonMap;


public class SimpleMsgListener implements IListener<IMessageEvent> {
    private final IMsgBuilder msgBuilder;
    private final IMsgFilter filter;
    private final boolean reply;
    private final boolean cansel;
    private final int priority;

    public SimpleMsgListener(IrisSystem.GuildProgram program, TsonMap map){
        cansel = map.getOrDefault("cansel",new TsonBool(false)).getBool();
        priority = map.getOrDefault("priority",new TsonInt(0)).getInt();
        filter = IMsgFilter.build(map.get("filter"));
        msgBuilder = IMsgBuilder.build(program, map);
        reply = map.getOrDefault("reply", new TsonBool(false)).getBool();
        program.addBaseListener(this);
    }


    @Override
    public boolean handle(IMessageEvent event) {
        if(event.getMessage().getAuthor().isBot())return false;
        String msg = event.getMessage().getContentDisplay();
        if(!filter.isSuccess(event, msg))return false;
        MessageCreateData msg0 = msgBuilder.build(event);
        if(msg0==null)return cansel;
        if(reply) {
            event.getMessage().reply(msg0).queue();
        } else {
            event.getChannel().sendMessage(msg0).queue();
        }
        return cansel;
    }


    @Override
    public int priority() {
        return priority;
    }
}