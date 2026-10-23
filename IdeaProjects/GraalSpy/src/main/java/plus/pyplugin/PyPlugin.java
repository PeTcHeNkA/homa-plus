package plus.pyplugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.graalvm.polyglot.Value;
import plus.graalspy.GraalEventListener;
import plus.graalspy.Main;


public interface PyPlugin {

    default void onEnable(){}


    default void onDisable(){}


    default boolean onCommand(CommandSender sender, Command command, String label, String[] args){
        return false;
    }


    default void register(Class<? extends Event> clazz, Value value){
        Main.INSTANCE.register(clazz, new GraalEventListener(value));
    }
}
