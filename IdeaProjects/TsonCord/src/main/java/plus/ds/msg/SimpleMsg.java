package plus.ds.msg;

import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import plus.ds.api.IMessageEvent;
import plus.ds.api.IMsgAction;
import plus.ds.api.IMsgBuilder;


public final class SimpleMsg implements IMsgBuilder, IMsgAction {
    private final String msg;
    public SimpleMsg(String msg) {
        this.msg = msg;
    }


    @Override
    public MessageCreateData build(IMessageEvent e) {
        return new MessageCreateBuilder().setContent(msg).build();
    }


    @Override
    public void apply(MessageCreateBuilder msfBuilder, IMessageEvent event) {
        msfBuilder.setContent(msg);
    }
}