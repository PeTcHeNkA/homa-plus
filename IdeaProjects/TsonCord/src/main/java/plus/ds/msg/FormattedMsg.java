package plus.ds.msg;

import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import plus.ds.IrisSystem;
import plus.ds.api.IMessageEvent;
import plus.ds.api.IMsgAction;
import plus.ds.api.IMsgBuilder;
import plus.ds.api.MessageVarProvider;
import plus.tson.format.IVarGetter;
import plus.tson.format.TextFormatter;


public class FormattedMsg implements IMsgBuilder, IMsgAction {
    IVarGetter<IMessageEvent> formatter;
    public FormattedMsg(IrisSystem.GuildProgram program, String format){
        formatter = TextFormatter.compile(format, new MessageVarProvider(program)).simplify();
    }


    @Override
    public MessageCreateData build(IMessageEvent event) {
        return new MessageCreateBuilder().setContent(formatter.get(event)).build();
    }


    @Override
    public void apply(MessageCreateBuilder msfBuilder, IMessageEvent event) {
        msfBuilder.setContent(formatter.get(event));
    }
}