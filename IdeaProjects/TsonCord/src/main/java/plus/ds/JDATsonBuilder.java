package plus.ds;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.internal.entities.channel.concrete.TextChannelImpl;
import plus.ds.api.IMessageEvent;
import plus.ds.api.IMsgBuilder;
import plus.ds.plsys.DiscordPluginManager;
import plus.tson.TsonList;
import plus.tson.TsonMap;
import plus.util.SimpleSrcGetter;
import plus.util.cons.ConsoleCommand;
import plus.util.cons.ConsoleListener;
import plus.util.cons.Logger;


public class JDATsonBuilder implements SimpleSrcGetter {
    private static int consoles = 0;
    private final TsonMap cfg = new TsonMap(getStrSrc("iris_config.tson"));
    private final JDABuilder builder;
    private String[] pluginNames;

    public JDATsonBuilder(String... pluginNames){
        builder = JDABuilder.createLight(
                cfg.getStr("token"),
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.GUILD_MEMBERS,
                GatewayIntent.GUILD_PRESENCES,
                GatewayIntent.MESSAGE_CONTENT
        );
        builder.disableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.VOICE_STATE);
        builder.setBulkDeleteSplittingEnabled(false);
        builder.setActivity(readActivity());
        this.pluginNames = pluginNames;
    }


    public JDABuilder builder(){
        return builder;
    }


    public IrisSystem build(){
        JDA jda = builder.build();
        IrisSystem system = new IrisSystem(jda, cfg);
        IrisEventManager manager = new IrisEventManager(system);
        jda.setEventManager(manager);
        DiscordPluginManager pluginSystem = new DiscordPluginManager(system);
        pluginSystem.load(pluginNames);
        jda.addEventListener(system);

        initConsole(pluginSystem, system);
        return system;
    }


    private void initConsole(DiscordPluginManager pluginSystem, IrisSystem system){
        String name = "iris-ds";
        if(consoles != 0){
            name += consoles;
        }
        ++consoles;
        new ConsoleListener(name).addCommand(
                new ConsoleCommand("/exucate") {
                    private final IrisSystem.GuildProgram program = system.getOrCreateGuildProgram(0);
                    @Override
                    public void onCommand(String... args) {
                        Logger logger = Logger.get("iris-ds");
                        if(args==null || args.length<3){
                            logger.info("Message is empty! use "+help());
                            return;
                        }
                        Guild guild = system.getJDA().getGuildById(args[0]);
                        if(guild==null){
                            logger.info("Guild is null!");return;
                        }
                        long id = Long.parseLong(args[1]);
                        TextChannelImpl chanel = guild.getChannelById(TextChannelImpl.class, id);
                        if(chanel == null){
                            logger.info("Chanel is null!");
                            return;
                        }
                        IMsgBuilder answerBuilder;
                        RestAction<Message> replyMsg = null;
                        if(args[2].equals("-reply")){
                            long msgId = Long.parseLong(args[3]);
                            try {
                                replyMsg = chanel.retrieveMessageById(msgId);
                            } catch (Exception e){
                                logger.info("Reply target is null!");
                                return;
                            }
                            answerBuilder = IMsgBuilder.build(program, new TsonMap(parse(args, 3)));
                        } else {
                            answerBuilder = IMsgBuilder.build(program, new TsonMap(parse(args, 2)));
                        }
                        if(answerBuilder==null){
                            logger.info("Message builder is null!");
                            return;
                        }
                        MessageCreateData answer = answerBuilder.build(new ConsoleMsgEvent(chanel));
                        if(replyMsg != null){
                            replyMsg.queue(message -> message.reply(answer).queue());
                        } else {
                            chanel.sendMessage(answer).queue();
                        }
                    }


                    private String parse(String[] agrs, int delta){
                        if(agrs.length==delta+1)return agrs[delta];
                        StringBuilder builder1 = new StringBuilder(agrs[delta]);
                        for(int i=delta+1;i<agrs.length;i++){
                            builder1.append(" ").append(agrs[i]);
                        }
                        return builder1.toString();
                    }

                    @Override
                    public String help() {
                        return "/exucate (guild) (chanel) (reply) (msg)";
                    }
                }
        );
    }


    private Activity readActivity(){
        if(cfg.containsKey("activity")){
            TsonList l = cfg.getList("activity");
            switch (l.getStr(0)){
                case "watch": return Activity.watching(l.getStr(1));
                case "compete": return Activity.competing(l.getStr(1));
                case "listen": return Activity.listening(l.getStr(1));
                case "stream": return Activity.streaming(l.getStr(1),l.getStr(2));
                case "playing": return Activity.playing(l.getStr(1));
                default: return Activity.playing(l.getStr(0));
            }
        }
        return null;
    }
}


class ConsoleMsgEvent implements IMessageEvent{
    private final MessageChannelUnion chanel;
    ConsoleMsgEvent(MessageChannelUnion chanel) {
        this.chanel = chanel;
    }

    @Override
    public Message getMessage() {
        return null;
    }


    @Override
    public MessageChannelUnion getChannel() {
        return chanel;
    }


    @Override
    public Type type() {
        return Type.CREATE;
    }


    @Override
    public User getUser() {
        return chanel.getJDA().getSelfUser();
    }
}