package net.dv8tion.jda.api.events.message;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import plus.ds.api.IMessageEvent;


public class MessageDeleteEvent extends GenericMessageEvent implements IMessageEvent {
    public MessageDeleteEvent(JDA api, long responseNumber, long messageId, MessageChannel channel) {
        super(api, responseNumber, messageId, channel);
    }

    @Override
    public Message getMessage() {
        return null;
    }


    @Override
    public Type type() {
        return Type.DELETE;
    }


    @Override
    public User getUser() {
        return getMessage().getAuthor();
    }
}