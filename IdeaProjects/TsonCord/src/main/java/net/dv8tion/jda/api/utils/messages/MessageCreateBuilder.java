package net.dv8tion.jda.api.utils.messages;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.Helpers;
import net.dv8tion.jda.internal.utils.IOUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


public class MessageCreateBuilder extends AbstractMessageBuilder<MessageCreateData, MessageCreateBuilder> implements MessageCreateRequest<MessageCreateBuilder> {
    private final List<FileUpload> files = new ArrayList<>(10);
    private boolean tts;

    public MessageCreateBuilder() {}


    public static MessageCreateBuilder from(MessageCreateData data) {
        return new MessageCreateBuilder().applyData(data);
    }


    public static MessageCreateBuilder fromEditData(MessageEditData data) {
        return new MessageCreateBuilder().applyEditData(data);
    }


    public static MessageCreateBuilder fromMessage(Message message) {
        return new MessageCreateBuilder().applyMessage(message);
    }


    @Override
    public MessageCreateBuilder addContent(String content) {
        Checks.notNull(content, "Content");
        Checks.check(
                Helpers.codePointLength(this.content) + Helpers.codePointLength(content) <= Message.MAX_CONTENT_LENGTH,
                "Cannot have content longer than %d characters", Message.MAX_CONTENT_LENGTH
        );
        this.content.append(content);
        return this;
    }


    @Override
    public MessageCreateBuilder addEmbeds(Collection<? extends MessageEmbed> embeds) {
        Checks.noneNull(embeds, "Embeds");
        Checks.check(
                this.embeds.size() + embeds.size() <= Message.MAX_EMBED_COUNT,
                "Cannot add more than %d embeds", Message.MAX_EMBED_COUNT
        );
        this.embeds.addAll(embeds);
        return this;
    }


    public void addOneEmbed(MessageEmbed embed){
        this.embeds.add(embed);
    }


    public void addOneComponent(LayoutComponent component){
        this.components.add(component);
    }


    @Override
    public MessageCreateBuilder addComponents(Collection<? extends LayoutComponent> components) {
        Checks.noneNull(components, "ComponentLayouts");
        for (LayoutComponent layout : components)
            Checks.check(layout.isMessageCompatible(), "Provided component layout is invalid for messages!");
        Checks.check(
                this.components.size() + components.size() <= Message.MAX_COMPONENT_COUNT,
                "Cannot add more than %d component layouts", Message.MAX_COMPONENT_COUNT
        );
        this.components.addAll(components);
        return this;
    }


    @Override
    public MessageCreateBuilder setFiles(Collection<? extends FileUpload> files) {
        if (files != null) Checks.noneNull(files, "Files");
        this.files.clear();
        if (files != null) this.files.addAll(files);
        return this;
    }


    @Override
    public List<FileUpload> getAttachments() {
        return Collections.unmodifiableList(files);
    }


    @Override
    public MessageCreateBuilder addFiles(Collection<? extends FileUpload> files) {
        Checks.noneNull(files, "Files");
        this.files.addAll(files);
        return this;
    }


    @Override
    public MessageCreateBuilder setTTS(boolean tts) {
        this.tts = tts;
        return this;
    }


    @Override
    public MessageCreateBuilder setSuppressedNotifications(boolean suppressed) {
        if(suppressed)
            messageFlags |= Message.MessageFlag.NOTIFICATIONS_SUPPRESSED.getValue();
        else
            messageFlags &= ~Message.MessageFlag.NOTIFICATIONS_SUPPRESSED.getValue();
        return this;
    }

    @Override
    public boolean isEmpty() {
        return Helpers.isBlank(content) && embeds.isEmpty() && files.isEmpty() && components.isEmpty();
    }


    @Override
    public boolean isValid() {
        return !isEmpty() && embeds.size() <= Message.MAX_EMBED_COUNT
                && components.size() <= Message.MAX_COMPONENT_COUNT
                && Helpers.codePointLength(content) <= Message.MAX_CONTENT_LENGTH;
    }


    public MessageCreateData build() {
        String content = this.content.toString().trim();
        List<MessageEmbed> embeds = new ArrayList<>(this.embeds);
        List<FileUpload> files = new ArrayList<>(this.files);
        List<LayoutComponent> components = new ArrayList<>(this.components);
        AllowedMentionsData mentions = this.mentions.copy();

        if (content.isEmpty() && embeds.isEmpty() && files.isEmpty() && components.isEmpty())
            throw new IllegalStateException("Cannot build an empty message. You need at least one of content, embeds, components, or files");

        int length = Helpers.codePointLength(content);
        if (length > Message.MAX_CONTENT_LENGTH)
            throw new IllegalStateException("Message content is too long! Max length is " + Message.MAX_CONTENT_LENGTH + " characters, provided " + length);

        if (embeds.size() > Message.MAX_EMBED_COUNT)
            throw new IllegalStateException("Cannot build message with over " + Message.MAX_EMBED_COUNT + " embeds, provided " + embeds.size());

        if (components.size() > Message.MAX_COMPONENT_COUNT)
            throw new IllegalStateException("Cannot build message with over " + Message.MAX_COMPONENT_COUNT + " component layouts, provided " + components.size());
        return new MessageCreateData(content, embeds, files, components, mentions, tts, messageFlags);
    }


    public MessageCreateBuilder clear() {
        super.clear();
        this.files.clear();
        this.tts = false;
        return this;
    }


    @Override
    public MessageCreateBuilder closeFiles() {
        files.forEach(IOUtil::silentClose);
        files.clear();
        return this;
    }
}