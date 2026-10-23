package net.dv8tion.jda.api.events.interaction.component;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;
import plus.ds.api.IMessageEvent;


public class ButtonInteractionEvent extends GenericComponentInteractionCreateEvent implements ButtonInteraction, IMessageEvent {
    private final ButtonInteraction interaction;

    public ButtonInteractionEvent(JDA api, long responseNumber, ButtonInteraction interaction) {
        super(api, responseNumber, interaction);
        this.interaction = interaction;
    }


    @Override
    public ButtonInteraction getInteraction() {
        return interaction;
    }


    @Override
    public Button getComponent() {
        return interaction.getComponent();
    }


    @Override
    public Button getButton() {
        return interaction.getButton();
    }


    @Override
    public Type type() {
        return Type.BUTTON_CLICK;
    }
}
