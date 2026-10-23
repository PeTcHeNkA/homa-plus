package plus.ds.api;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.hooks.IEventManager;
import plus.ds.IrisEventManager;
import plus.ds.IrisSystem;


public interface IMessageEvent {
    enum Type{
        CREATE, UPDATE, DELETE, MENU_CLICK, BUTTON_CLICK
    }

    Message getMessage();
    MessageChannelUnion getChannel();
    Type type();
    User getUser();


    default IrisSystem getSystem(){
        return getSystemByJDA(getMessage().getJDA());
    }


    static IrisSystem getSystemByJDA(JDA jda){
        IEventManager manager = jda.getEventManager();
        if(manager instanceof IrisEventManager iris){
            return iris.system;
        } else {
            throw new RuntimeException();
//            IrisSystem sys = IrisSystem.systems.get(jda);
//            if(sys==null){
//                IrisSystem.systems.put(jda, sys = new IrisSystem());
//            }
//            return sys;
        }
    }
}
