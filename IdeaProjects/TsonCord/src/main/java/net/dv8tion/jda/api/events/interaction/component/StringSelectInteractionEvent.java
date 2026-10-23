package net.dv8tion.jda.api.events.interaction.component;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectInteraction;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import plus.ds.api.IMessageEvent;


public class StringSelectInteractionEvent extends GenericSelectMenuInteractionEvent<String, StringSelectMenu> implements StringSelectInteraction, IMessageEvent {
    private final StringSelectInteraction interaction;

    public StringSelectInteractionEvent(JDA api, long responseNumber, StringSelectInteraction interaction) {
        super(api, responseNumber, interaction);
        this.interaction = interaction;
    }


    @Override
    public StringSelectInteraction getInteraction() {
        return interaction;
    }


    @Override
    public Type type() {
        return Type.MENU_CLICK;
    }
}