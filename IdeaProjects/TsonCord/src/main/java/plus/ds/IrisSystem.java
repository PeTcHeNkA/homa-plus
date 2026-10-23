package plus.ds;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import plus.asyutl.shoulder.TaskShoulder;
import plus.ds.api.IListener;
import plus.ds.api.IMessageEvent;
import plus.ds.msg.menu.MenuController;
import plus.ds.security.RuntimePluginManager;
import plus.te4net.core.bsys.DataProviderBuilder;
import plus.tson.TsonMap;
import plus.tson.utl.Te4HashMap;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import plus.te4net.core.api.IDataProvider;


public class IrisSystem extends ListenerAdapter {
    public static final TaskShoulder shoulder = new TaskShoulder(500);
    public final RuntimePluginManager.ConstructorLib lib = new RuntimePluginManager.ConstructorLib();

    private final GuildProgram defaultProgram = new GuildProgram(0,this);
    private Long2ObjectMap<GuildProgram> guildPrograms = Long2ObjectMaps.singleton(0, defaultProgram);
    private final JDA jda;
    private final IDataProvider dataProvider;

    public IrisSystem(JDA jda, TsonMap cfg) {
        this.jda = jda;
        dataProvider = DataProviderBuilder.ofTson(cfg.get("data_provider"));
    }


    public GuildProgram defaultProgram(){
        return defaultProgram;
    }


    public JDA getJDA(){
        return jda;
    }


    public static class GuildProgram{
        public final RuntimePluginManager manager;
        public final IrisSystem system;
        public final long guildId;
        public final Map<String, IListener<StringSelectInteractionEvent>> menuListeners   = new Te4HashMap<>();
        public final Map<String, IListener<ButtonInteractionEvent>[]>     buttonListeners = new Te4HashMap<>();
        private IListener<IMessageEvent>[]                                baseListeners   = new IListener[0];
        private IDataProvider dataProvider;

        private GuildProgram(long id, IrisSystem system) {
            this.system = system;
            this.guildId = id;
            manager = system.lib.managerById(this);
            this.dataProvider = system.dataProvider;
        }


        public IDataProvider getDataProvider(){
            return dataProvider;
        }


        public GuildProgram clean(){
            menuListeners.clear();
            buttonListeners.clear();
            baseListeners = new IListener[0];
            return this;
        }


        public void addBaseListener(IListener<IMessageEvent> listener){
            baseListeners = expand(baseListeners, listener);
//            IListener<IMessageEvent>[] newListeners = new IListener[baseListeners.length+1];
//            if(baseListeners.length != 0){
//                System.arraycopy(baseListeners,0, newListeners, 1, baseListeners.length);
//                newListeners[0] = listener;
//                baseListeners = sort(newListeners);
//            } else {
//                baseListeners = new IListener[]{listener};
//            }
        }


        public void addMenuListener(MenuController menuController) {
            menuListeners.put(menuController.getId(), menuController);
        }


        public void addButtonListener(String id, IListener<ButtonInteractionEvent> listener){
            buttonListeners.put(id, expand(buttonListeners.get(id), listener));
        }
    }


    private static<T extends IListener<?>> T[] expand(T[] src, T obj){
        if(src == null || src.length == 0)return (T[]) new IListener[]{obj};
        T[] newListeners = (T[]) new IListener[src.length+1];
        System.arraycopy(src,0, newListeners, 1, src.length);
        newListeners[0] = obj;
        return sort(newListeners);
    }


    private static<T extends IListener<?>> T[] sort(T[] t){
        Arrays.sort(t, Comparator.comparingInt(value -> value.priority()));
        return t;
    }


    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        for(IListener<IMessageEvent> l:defaultProgram.baseListeners){
            if(l.handle(event))return;
        }
        if(event.isFromGuild()){
            GuildProgram guildProgram = guildPrograms.get(event.getGuild().getIdLong());
            if(guildProgram != null)
                for(IListener<IMessageEvent> l: guildProgram.baseListeners){
                    if(l.handle(event))return;
                }
        }
    }


    @Override
    public void onMessageUpdate(MessageUpdateEvent event){
        for(IListener<IMessageEvent> l: defaultProgram.baseListeners){
            if(l.handle(event))return;
        }
        if(event.isFromGuild()){
            GuildProgram program = guildPrograms.get(event.getGuild().getIdLong());
            if(program!=null){
                for(IListener<IMessageEvent> l:program.baseListeners){
                    if(l.handle(event))return;
                }
            }
        }
    }


    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        IListener<StringSelectInteractionEvent> listener =
                defaultProgram.menuListeners.get(event.getInteraction().getSelectMenu().getId());
        
        if(listener != null)listener.handle(event);

        if(event.isFromGuild()){
            GuildProgram program = guildPrograms.get(event.getGuild().getIdLong());
            if(program!=null){
                listener = program.menuListeners.get(event.getInteraction().getSelectMenu().getId());
                if(listener != null)listener.handle(event);
            }
        }
    }


    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        IListener<ButtonInteractionEvent>[] listeners =
                defaultProgram.buttonListeners.get(event.getInteraction().getButton().getId());

        if(listeners != null)
            for (IListener<ButtonInteractionEvent> listener:listeners)
                if(listener.handle(event))return;

        if(event.isFromGuild()){
            GuildProgram program = guildPrograms.get(event.getGuild().getIdLong());
            if(program!=null){
                listeners = program.buttonListeners.get(event.getInteraction().getButton().getId());
                if(listeners != null)
                    for (IListener<ButtonInteractionEvent> listener:listeners)
                        if(listener.handle(event))return;
            }
        }
    }


    public GuildProgram getOrDefaultGuildProgram(long id){
        return guildPrograms.getOrDefault(id, defaultProgram);
    }


    public synchronized GuildProgram getOrCreateGuildProgram(long id){
        GuildProgram program = guildPrograms.get(id);
        if(program==null){
            Long2ObjectMap<GuildProgram> programs = new Long2ObjectOpenHashMap<>(guildPrograms);
            programs.put(id, program = new GuildProgram(id,this));
            guildPrograms = Long2ObjectMaps.unmodifiable(programs);
        }
        return program;
    }


    public void addRootEventListeners(Object... listeners){
        ArrayList<IListener<IMessageEvent>> raw = new ArrayList<>(Arrays.asList(defaultProgram.baseListeners));
        for (Object listener:listeners) {
            addRootEventListeners(raw, listener);
        }
        defaultProgram.baseListeners = sort(raw.toArray(new IListener[0]));
    }


    protected void addRootEventListeners(List<IListener<IMessageEvent>> stdListeners, Object listener){
        if(listener instanceof ListenerAdapter) {
            jda.addEventListener(listener);
        } else if(listener instanceof IListener){
            switch (getEvType(listener)){
                case "IMessageEvent": {
                    stdListeners.add((IListener<IMessageEvent>) listener);
                }
                break;
                case "": default:
                    System.out.println("Unknown type: "+listener.getClass());
            }
        }
    }


    private static String getEvType(Object ls) {
        for (Type o : ls.getClass().getGenericInterfaces()) {
            if (o instanceof ParameterizedType) {
                ParameterizedType tp = (ParameterizedType) o;
                if (tp.getRawType().equals(IListener.class)) {
                    return ((Class<?>) tp.getActualTypeArguments()[0]).getSimpleName();
                }
            }
        }
        return "";
    }
}
