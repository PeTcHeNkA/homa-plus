package plus.ds.dyn;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import plus.asyutl.shoulder.IBasicTask;
import plus.ds.IrisSystem;
import plus.ds.api.IListener;
import plus.ds.api.IMessageEvent;
import plus.ds.security.ComplexityAgent;
import plus.tson.TsonMap;
import plus.tson.TsonObj;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;


public class InstallProgramListener implements IListener<IMessageEvent> {
    private final ComplexityAgent agent;
    private final int priority;

    public InstallProgramListener(IrisSystem system, TsonMap map){
        priority = map.getOrDefaultInt("priority", 0);
        agent = new ComplexityAgent(system.defaultProgram().manager);
        system.addRootEventListeners(this);
    }

    @Override
    public boolean handle(IMessageEvent event) {
        IMessageEvent.Type type = event.type();
        if(type == IMessageEvent.Type.CREATE || type == IMessageEvent.Type.UPDATE){
            Message msg0 = event.getMessage();

            String msg = msg0.getContentDisplay();
            if(!msg.startsWith("IRIS_PROGRAM"))return false;

            if(!event.getMessage().isFromGuild() && type != IMessageEvent.Type.UPDATE){
                event.getMessage().reply("Cant install script form not-guild!").queue();
                return true;
            }
            Session session = sessions.get(msg0.getGuild().getId());

            if(session==null){
                session = new Session();
                sessions.put(msg0.getGuild().getId(), session);
            }

            LinkedMsg callbackId = session.messages.get(msg0.getId());
            if(callbackId!=null){
                handle2(event, session, callbackId);
            } else {
                handle2(event, session, null);
            }
        }
        return false;
    }


    private void handle2(IMessageEvent event, Session session, LinkedMsg callback){
        if(callback==null || !callback.isValid){
            MessageCreateBuilder builder = new MessageCreateBuilder();
            String fullId = "IRIS_PROGRAM_"+event.getMessage().getId();
            builder.setContent("Wait...");
            builder.addActionRow(Button.of(ButtonStyle.SUCCESS, fullId, "APPLY"));

            event.getMessage().reply(builder.build()).queue(
                    message -> {
                        LinkedMsg newCallBack;
                        Message eMsg = event.getMessage();
                        session.messages.put(eMsg.getId(), newCallBack = new LinkedMsg(event, eMsg,
                                eMsg.getId(), message, fullId));
                        processMsg(eMsg, event, message, true);

                        event.getSystem().getOrCreateGuildProgram(event.getMessage().getGuildIdLong())
                                .addButtonListener(fullId, event1 -> {
                            newCallBack.reset();
                            processApplyCfg(newCallBack.code, event1, event1.getMessage());
                            return true;
                        });
                        IrisSystem.shoulder.addTask(newCallBack);
                    }
            );
        } else {
            callback.reset();
            processMsg(callback.code = event.getMessage(), event, callback.button, true);
        }
    }


    private void processApplyCfg(Message code, ButtonInteractionEvent event, Message callBack){
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream print = new PrintStream(out);
        try {
            event.getSystem().getOrCreateGuildProgram(code.getGuildIdLong()).clean();
            TsonMap map = processMsg(code, event, callBack, false);
            if(map == null) return;
        } catch (Exception e){
            e.printStackTrace();
            e.printStackTrace(print);
        } finally {
            print.close();
            try {
                out.close();
            } catch (IOException ignored){}
        }
        if(out.size()==0){
            code.reply("Success!").queue();
        } else {
            event.reply(prepareError(out.toString())).queue();
        }
    }


    private static String prepareError(String result){
        boolean flag = false;
        if(result.length()>1950){
            result = result.substring(0, 1950);
            flag = true;
        }
        result = "CRITICAL ERROR!\n```"+result+"```";
        if(flag){
            result += "\n and "+(result.length()-1950)+" chars.";
        }
        return result;
    }


    private TsonMap processMsg(Message code, IMessageEvent event, Message callback, boolean agent){
        TsonMap map = null;
        MessageEditBuilder builder = new MessageEditBuilder();
        try {
            String codeStr = getTsonProgramFromMsg(code.getContentDisplay()).trim();
            if(codeStr.length()==0)throw new IllegalArgumentException("EMPTY CODE");
            map = new TsonMap(
                    agent?this.agent:
                    event.getSystem().getOrCreateGuildProgram(code.getGuildIdLong()).manager,
                    codeStr
            );
            if(agent) {
                builder.setContent("Configuration complexity: " + calcDifficult(map));
                callback.editMessage(builder.build()).queue();
            } else {
                sessions.get(code.getGuildId()).invalidateAll();
            }
        } catch (Throwable e) {
            try {
                e.printStackTrace();

                builder.setContent(prepareError(error(e)));

                callback.editMessage(builder.build()).queue();
            } catch (Throwable t){
                t.printStackTrace();
            }
        }
        return map;
    }


    private static int calcDifficult(TsonObj obj){
        AtomicInteger count = new AtomicInteger();
        calcDifficult(obj, count);
        return count.get();
    }


    private static void calcDifficult(TsonObj obj, AtomicInteger count){
        if(obj.isBool()){
            count.addAndGet(1);
        } else if (obj.isNumber()) {
            count.addAndGet(4);
        } else if (obj.isString()) {
            count.addAndGet(obj.getStr().length());
        } else if (obj.isList()) {
            count.addAndGet(12+obj.getList().size());
            for (TsonObj obj0: obj.getList()){
                calcDifficult(obj0, count);
            }
        } else if (obj.isMap()) {
            count.addAndGet(14);
            for (Map.Entry<String,TsonObj> entry:obj.getMap().entrySet()){
                count.addAndGet(entry.getKey().length()+2);
                calcDifficult(entry.getValue(), count);
            }
        } else if (obj.isCustom()){
            Object obj2 = obj.getField();
            if(obj2 instanceof TsonObj tsonObj){
                calcDifficult(tsonObj, count);
            } else {
                count.addAndGet(64);
            }
        } else {
            count.addAndGet(64);
        }
    }


    private static String error(Throwable t){
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream print = new PrintStream(out);
        t.printStackTrace(print);
        print.close();
        try {
            out.close();
        } catch (IOException e){}
        return out.toString();
    }


    private static class Session{
        HashMap<String,LinkedMsg> messages = new HashMap<>();
        void invalidateAll(){
            HashMap<String,LinkedMsg> old = messages;
            messages = new HashMap<>();
            for (LinkedMsg msg: old.values()){
                msg.timer = Integer.MAX_VALUE-2;
                try {
                    msg.button.delete().queue();
                } catch (Exception e){}
            }
        }
    }


    private class LinkedMsg implements IBasicTask {
        private Message code;
        private Message button;
        private final String buttonId;
        private boolean isValid = true;
        private final String selfId;
        private final IrisSystem.GuildProgram guild;
        int timer = 0;
        private LinkedMsg(IMessageEvent event, Message code, String selfId, Message message, String buttonId) {
            this.selfId = selfId;
            this.button = message;
            this.buttonId = buttonId;
            this.code = code;
            guild = event.getSystem().getOrCreateGuildProgram(code.getGuildIdLong());
        }
        public void reset(){
            timer = 0;
        }

        @Override
        public boolean needRun() {
            return timer++>(20*60);//12_000;
        }

        @Override
        public void run() {
            if(!isValid)return;
            isValid = false;
            sessions.remove(selfId);
            button.delete().queue();
            guild.buttonListeners.remove(buttonId);
        }
    }


    private HashMap<String, Session> sessions = new HashMap<>();
//
//
//    private void invalidateSessions(){
//        for (Map.Entry<String,Session> entry:sessions.entrySet()){
//            Session session = entry.getValue();
//            for (Map.Entry<String,LinkedMsg> msgEntry:new ArrayList<>(session.messages.entrySet())){
//                msgEntry.getValue().invalidate();
//            }
//        }
//    }


    public static String getTsonProgramFromMsg(String msg){
        char[] chars = msg.toCharArray();
        int start = -1;
        int end = -1;
        boolean isCode = false;
        StringBuilder builder = new StringBuilder();
        for (int i=3;i<chars.length;i++){
            if (chars[i] == '`' && chars[i-1] == '`' && chars[i-2]=='`') {
                if(!isCode){
                    start = i+1;
                    i+=3;
                } else {
                    end = i-2;
                    builder.append(new String(chars, start, end-start));
                }
                isCode = !isCode;
            }
        }
        return builder.toString();
    }


    @Override
    public int priority() {
        return priority;
    }
}