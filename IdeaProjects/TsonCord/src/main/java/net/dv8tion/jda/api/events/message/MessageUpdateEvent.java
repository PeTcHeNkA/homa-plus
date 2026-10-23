package net.dv8tion.jda.api.events.message;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import plus.ds.api.IMessageEvent;


public class MessageUpdateEvent extends GenericMessageEvent implements IMessageEvent {
    private final Message message;

    public MessageUpdateEvent(JDA api, long responseNumber, Message message) {
        super(api, responseNumber, message.getIdLong(), message.getChannel());
        this.message = message;
    }


    @Override
    public Type type() {
        return Type.UPDATE;
    }


    @Override
    public User getUser() {
        return message.getAuthor();
    }


    @Override
    public Message getMessage()
    {
        return message;
    }



    public User getAuthor()
    {
        return message.getAuthor();
    }


    public Member getMember()
    {
        return  message.getMember();
    }
}
