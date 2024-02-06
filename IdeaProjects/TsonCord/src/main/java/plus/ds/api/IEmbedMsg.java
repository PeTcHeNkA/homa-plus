package plus.ds.api;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;


@FunctionalInterface
public interface IEmbedMsg extends IMsgAction {
    MessageEmbed buildEmbed(IMessageEvent event);

    @Override
    default void apply(MessageCreateBuilder msfBuilder, IMessageEvent event) {
        msfBuilder.addOneEmbed(buildEmbed(event));
    }
}